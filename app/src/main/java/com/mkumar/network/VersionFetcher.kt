package com.mkumar.network

import com.mkumar.common.constant.AppConstants.latestVersionUrl
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.Request
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VersionFetcher @Inject constructor(
    private val networkClient: NetworkClient,
    private val json: Json
) {

    suspend fun fetchLatestVersion(): String {
        return try {
            val request = Request.Builder()
                .url(latestVersionUrl)
                .build()

            val response = networkClient.executeRequest(request)

            if (!response.isSuccessful) return "Unknown"

            response.body?.string()?.let { body ->
                val element: JsonElement =
                    json.decodeFromString(JsonElement.serializer(), body)

                (element as? JsonObject)
                    ?.get("name")
                    ?.jsonPrimitive
                    ?.content
                    ?.removePrefix("v")
                    ?: "Unknown"
            } ?: "Unknown"

        } catch (_: Exception) {
            "Unknown"
        }
    }
}
