package com.mkumar.backup.github

import android.util.Base64
import com.mkumar.backup.BackupManifest
import com.mkumar.backup.BackupEntry
import com.mkumar.backup.BackupProvider
import com.mkumar.backup.RemoteBackup
import com.mkumar.data.PreferencesManager
import com.mkumar.network.NetworkClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GithubBackupProvider @Inject constructor(
    private val preferences: PreferencesManager,
    private val network: NetworkClient,
    private val json: Json
) : BackupProvider {

    companion object {
        private const val API = "https://api.github.com"
        private const val MANIFEST_PATH = ".mkumar-backup/manifest.json"
        private const val MAX_CONTENTS_API_BYTES = 100L * 1024L * 1024L
        private const val MAX_BACKUPS = 100
    }

    override suspend fun discoverBackup(): RemoteBackup? {
        requireToken()
        val configuredOwner = preferences.githubPrefs.githubOwner.trim()
        val configuredRepo = preferences.githubPrefs.githubRepo.trim()

        if (configuredOwner.isNotEmpty() && configuredRepo.isNotEmpty()) {
            readRemoteBackup(configuredOwner, configuredRepo)?.let { return it }
        }

        for (repo in accessibleRepositories()) {
            readRemoteBackup(repo.owner, repo.name, repo.defaultBranch)?.let { backup ->
                preferences.githubPrefs.githubOwner = backup.owner
                preferences.githubPrefs.githubRepo = backup.repository
                return backup
            }
        }
        return null
    }

    override suspend fun upload(snapshot: File, manifest: BackupManifest): RemoteBackup {
        require(snapshot.length() < MAX_CONTENTS_API_BYTES) {
            "The database is too large for GitHub's 100 MB Contents API limit"
        }
        val repository = resolveUploadRepository()
        val newEntry = manifest.availableBackups().firstOrNull()
            ?: error("Backup manifest does not contain a snapshot")
        val previous = readRemoteBackup(repository.owner, repository.name, repository.defaultBranch)
        val previousEntries = previous?.manifest?.availableBackups().orEmpty()
        val retained = (listOf(newEntry) + previousEntries.filter { it.backupPath != newEntry.backupPath })
            .sortedByDescending { it.createdAtUtc }
            .take(preferences.backupPrefs.retentionCount.coerceIn(3, MAX_BACKUPS))
        val updatedManifest = BackupManifest(backups = retained)

        putFile(
            repository = repository,
            path = newEntry.backupPath,
            bytes = snapshot.readBytes(),
            commitMessage = "Backup database (${newEntry.trigger.lowercase()}) at ${newEntry.createdAtUtc}"
        )
        putFile(
            repository = repository,
            path = MANIFEST_PATH,
            bytes = json.encodeToString(updatedManifest).toByteArray(),
            commitMessage = "Update backup catalog (${retained.size} snapshot${if (retained.size == 1) "" else "s"})"
        )

        previousEntries.filter { old -> retained.none { it.backupPath == old.backupPath } }
            .forEach { old -> deleteFile(repository, old) }
        return RemoteBackup(repository.owner, repository.name, repository.defaultBranch, updatedManifest)
    }

    override suspend fun download(backup: RemoteBackup, entry: BackupEntry, destination: File) {
        withContext(Dispatchers.IO) {
            val request = requestBuilder(
                "$API/repos/${backup.owner}/${backup.repository}/contents/${entry.backupPath}?ref=${backup.branch}"
            )
                .header("Accept", "application/vnd.github.raw+json")
                .get()
                .build()
            network.executeRequest(request).use { response ->
                check(response.isSuccessful) { githubError(response.code, response.message) }
                val body = response.body ?: error("GitHub returned an empty backup")
                destination.outputStream().use { body.byteStream().copyTo(it) }
            }
        }
    }

    override suspend fun delete(backup: RemoteBackup, entry: BackupEntry): RemoteBackup {
        val repository = RepositoryInfo(backup.owner, backup.repository, backup.branch)
        val retained = backup.manifest.availableBackups()
            .filterNot { it.backupPath == entry.backupPath }
        val updatedManifest = BackupManifest(backups = retained)
        putFile(
            repository = repository,
            path = MANIFEST_PATH,
            bytes = json.encodeToString(updatedManifest).toByteArray(),
            commitMessage = "Remove backup from catalog (${entry.createdAtUtc})"
        )
        deleteFile(repository, entry)
        return RemoteBackup(backup.owner, backup.repository, backup.branch, updatedManifest)
    }

    private suspend fun resolveUploadRepository(): RepositoryInfo {
        val owner = preferences.githubPrefs.githubOwner.trim()
        val repo = preferences.githubPrefs.githubRepo.trim()
        if (owner.isNotEmpty() && repo.isNotEmpty()) return repositoryInfo(owner, repo)

        discoverBackup()?.let {
            return RepositoryInfo(it.owner, it.repository, it.branch)
        }
        error("No backup repository was found. Enter the GitHub owner and repository once to initialize it.")
    }

    private suspend fun accessibleRepositories(): List<RepositoryInfo> {
        val result = mutableListOf<RepositoryInfo>()
        var page = 1
        while (true) {
            val element = getJson("$API/user/repos?per_page=100&page=$page&affiliation=owner,collaborator,organization_member")
            val items = element.jsonArray
            if (items.isEmpty()) break
            result += items.map { item ->
                val obj = item.jsonObject
                RepositoryInfo(
                    owner = obj.getValue("owner").jsonObject.getValue("login").jsonPrimitive.content,
                    name = obj.getValue("name").jsonPrimitive.content,
                    defaultBranch = obj["default_branch"]?.jsonPrimitive?.content ?: "main"
                )
            }
            if (items.size < 100) break
            page++
        }
        return result
    }

    private suspend fun repositoryInfo(owner: String, repo: String): RepositoryInfo {
        val obj = getJson("$API/repos/$owner/$repo").jsonObject
        return RepositoryInfo(owner, repo, obj["default_branch"]?.jsonPrimitive?.content ?: "main")
    }

    private suspend fun readRemoteBackup(
        owner: String,
        repo: String,
        knownBranch: String? = null
    ): RemoteBackup? {
        val branch = knownBranch ?: runCatching { repositoryInfo(owner, repo).defaultBranch }.getOrNull()
            ?: return null
        val url = "$API/repos/$owner/$repo/contents/$MANIFEST_PATH?ref=$branch"
        val request = requestBuilder(url).get().build()
        return network.executeRequest(request).use { response ->
            if (response.code == 404) return@use null
            check(response.isSuccessful) { githubError(response.code, response.message) }
            val objectJson = json.parseToJsonElement(response.body?.string().orEmpty()).jsonObject
            val encoded = objectJson["content"]?.jsonPrimitive?.content ?: return@use null
            val bytes = Base64.decode(encoded, Base64.DEFAULT)
            val manifest = json.decodeFromString<BackupManifest>(bytes.decodeToString())
            if (manifest.applicationId != "com.mkumar") return@use null
            RemoteBackup(owner, repo, branch, manifest)
        }
    }

    private suspend fun putFile(
        repository: RepositoryInfo,
        path: String,
        bytes: ByteArray,
        commitMessage: String
    ) {
        val existingSha = getExistingSha(repository, path)
        val body = buildJsonObject {
            put("message", commitMessage)
            put("content", Base64.encodeToString(bytes, Base64.NO_WRAP))
            put("branch", repository.defaultBranch)
            existingSha?.let { put("sha", it) }
        }
        val request = requestBuilder("$API/repos/${repository.owner}/${repository.name}/contents/$path")
            .put(body.toString().toRequestBody("application/json; charset=utf-8".toMediaType()))
            .build()
        network.executeRequest(request).use { response ->
            check(response.isSuccessful) { githubError(response.code, response.message) }
        }
    }

    private suspend fun getExistingSha(repository: RepositoryInfo, path: String): String? {
        val request = requestBuilder(
            "$API/repos/${repository.owner}/${repository.name}/contents/$path?ref=${repository.defaultBranch}"
        ).get().build()
        return network.executeRequest(request).use { response ->
            if (response.code == 404) return@use null
            check(response.isSuccessful) { githubError(response.code, response.message) }
            json.parseToJsonElement(response.body?.string().orEmpty())
                .jsonObject["sha"]?.jsonPrimitive?.content
        }
    }

    private suspend fun deleteFile(repository: RepositoryInfo, entry: BackupEntry) {
        val sha = getExistingSha(repository, entry.backupPath) ?: return
        val body = buildJsonObject {
            put("message", "Prune database backup from ${entry.createdAtUtc}")
            put("sha", sha)
            put("branch", repository.defaultBranch)
        }
        val request = requestBuilder("$API/repos/${repository.owner}/${repository.name}/contents/${entry.backupPath}")
            .delete(body.toString().toRequestBody("application/json; charset=utf-8".toMediaType()))
            .build()
        network.executeRequest(request).use { response ->
            check(response.isSuccessful) { githubError(response.code, response.message) }
        }
    }

    private suspend fun getJson(url: String) = network.executeRequest(requestBuilder(url).get().build()).use { response ->
        check(response.isSuccessful) { githubError(response.code, response.message) }
        json.parseToJsonElement(response.body?.string().orEmpty())
    }

    private fun requestBuilder(url: String): Request.Builder = Request.Builder()
        .url(url)
        .header("Authorization", "Bearer ${requireToken()}")
        .header("Accept", "application/vnd.github+json")
        .header("X-GitHub-Api-Version", "2022-11-28")

    private fun requireToken(): String = preferences.githubPrefs.token.trim().also {
        require(it.isNotEmpty()) { "Enter a GitHub token in Backup settings" }
    }

    private fun githubError(code: Int, message: String): String = when (code) {
        401 -> "GitHub rejected the token"
        403 -> "The token does not have access to the backup repository"
        404 -> "The GitHub repository or backup file was not found"
        409 -> "GitHub reported a conflicting backup update; please retry"
        422 -> "GitHub rejected the backup request"
        else -> "GitHub request failed ($code): $message"
    }

    private data class RepositoryInfo(
        val owner: String,
        val name: String,
        val defaultBranch: String
    )

}
