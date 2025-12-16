package com.mkumar.sync.remote

import com.mkumar.data.PreferencesManager
import com.mkumar.network.NetworkClient
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GithubRemoteImpl @Inject constructor(
    private val prefs: PreferencesManager,
    private val network: NetworkClient,
    private val json: Json
) : CloudRemote {

    private fun baseUrl(): String {
        return "https://api.github.com/repos/${prefs.githubPrefs.githubOwner}/${prefs.githubPrefs.githubRepo}/contents"
    }

    private fun authHeader(): String =
        "Bearer ${prefs.githubPrefs.token}"

    override suspend fun list(folder: String): List<String> {
        val url = "${baseUrl()}/$folder"
        val response = network.get(url, authHeader())
        if (response.isNullOrBlank()) return emptyList()

        val arr = json.parseToJsonElement(response).jsonArray
        return arr.mapNotNull { it.jsonObject["path"]?.jsonPrimitive?.content }
    }

    override suspend fun get(path: String): String? {
        val url = "${baseUrl()}/$path"
        val response = network.get(url, authHeader()) ?: return null

        val obj = json.parseToJsonElement(response).jsonObject
        val encoded = obj["content"]?.jsonPrimitive?.content ?: return null
        return String(android.util.Base64.decode(encoded, android.util.Base64.DEFAULT))
    }

    override suspend fun putJson(path: String, content: String) {
        val url = "${baseUrl()}/$path"

        val body = mapOf(
            "message" to "MKumar sync update",
            "content" to android.util.Base64.encodeToString(
                content.toByteArray(),
                android.util.Base64.NO_WRAP
            ),
            "branch" to "main"
        )

        network.put(url, authHeader(), json.encodeToString(body))
    }

    override suspend fun delete(path: String) {
        val url = "${baseUrl()}/$path"

        // GitHub requires sha for delete; fetch first
        val meta = network.get(url, authHeader()) ?: return
        val sha = json.parseToJsonElement(meta)
            .jsonObject["sha"]?.jsonPrimitive?.content ?: return

        val body = mapOf(
            "message" to "MKumar delete",
            "sha" to sha,
            "branch" to "main"
        )

        network.delete(url, authHeader(), json.encodeToString(body))
    }
}
