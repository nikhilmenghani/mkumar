package com.mkumar.network

import android.content.Context
import com.mkumar.common.constant.AppConstants.latestVersionUrl
import com.mkumar.common.version.SemanticVersion
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.Request
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VersionFetcher @Inject constructor(
    @ApplicationContext private val context: Context,
    private val networkClient: NetworkClient,
    private val json: Json
) {

    suspend fun fetchLatestRelease(): AppRelease? {
        return try {
            val isDebug = context.packageName == "com.mkumar.debug"
            val request = Request.Builder()
                .url(if (isDebug) DEV_RELEASES_URL else latestVersionUrl)
                .header("Accept", "application/vnd.github+json")
                .header("X-GitHub-Api-Version", "2022-11-28")
                .build()

            networkClient.executeRequest(request).use { response ->
                if (!response.isSuccessful) return null
                val body = response.body?.string() ?: return null
                val release = if (isDebug) {
                    json.parseToJsonElement(body).jsonArray
                        .mapNotNull { item ->
                            val obj = item.jsonObject
                            val tag = obj["tag_name"]?.jsonPrimitive?.content.orEmpty()
                            if (obj["prerelease"]?.jsonPrimitive?.content != "true" ||
                                !tag.startsWith(DEV_TAG_PREFIX) ||
                                SemanticVersion.parse(tag) == null
                            ) null else obj
                        }
                        .maxWithOrNull { left, right ->
                            val leftVersion = SemanticVersion.parse(
                                left.getValue("tag_name").jsonPrimitive.content
                            )!!
                            val rightVersion = SemanticVersion.parse(
                                right.getValue("tag_name").jsonPrimitive.content
                            )!!
                            leftVersion.compareTo(rightVersion)
                        }
                } else {
                    json.parseToJsonElement(body).jsonObject
                } ?: return null

                val tag = release["tag_name"]?.jsonPrimitive?.content ?: return null
                val expectedAssetPrefix = if (isDebug) "MKumar-debug-" else "MKumar-"
                val asset = release["assets"]?.jsonArray
                    ?.firstOrNull { item ->
                        val name = item.jsonObject["name"]?.jsonPrimitive?.content.orEmpty()
                        name.startsWith(expectedAssetPrefix) && name.endsWith(".apk")
                    }?.jsonObject ?: return null
                val url = asset["browser_download_url"]?.jsonPrimitive?.content ?: return null
                AppRelease(
                    version = tag.removePrefix(if (isDebug) DEV_TAG_PREFIX else "v"),
                    downloadUrl = url
                )
            }
        } catch (_: Exception) {
            null
        }
    }

    companion object {
        private const val DEV_RELEASES_URL =
            "https://api.github.com/repos/nikhilmenghani/mkumar/releases?per_page=30"
        private const val DEV_TAG_PREFIX = "dev-v"
    }
}

data class AppRelease(val version: String, val downloadUrl: String)
