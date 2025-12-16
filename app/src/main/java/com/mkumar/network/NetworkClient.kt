package com.mkumar.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NetworkClient @Inject constructor() {

    private val client = OkHttpClient()

    private val JSON = "application/json; charset=utf-8".toMediaType()

    // --------------------------------------------------
    // Core executor (kept for compatibility)
    // --------------------------------------------------
    suspend fun executeRequest(request: Request): Response {
        return withContext(Dispatchers.IO) {
            client.newCall(request).execute()
        }
    }

    // --------------------------------------------------
    // GET
    // --------------------------------------------------
    suspend fun get(
        url: String,
        authHeader: String
    ): String? {
        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", authHeader)
            .addHeader("Accept", "application/vnd.github+json")
            .get()
            .build()

        return execute(request)
    }

    // --------------------------------------------------
    // PUT (create/update JSON file)
    // --------------------------------------------------
    suspend fun put(
        url: String,
        authHeader: String,
        bodyJson: String
    ) {
        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", authHeader)
            .addHeader("Accept", "application/vnd.github+json")
            .put(bodyJson.toRequestBody(JSON))
            .build()

        execute(request)
    }

    // --------------------------------------------------
    // DELETE (GitHub requires JSON body with sha)
    // --------------------------------------------------
    suspend fun delete(
        url: String,
        authHeader: String,
        bodyJson: String
    ) {
        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", authHeader)
            .addHeader("Accept", "application/vnd.github+json")
            .delete(bodyJson.toRequestBody(JSON))
            .build()

        execute(request)
    }

    // --------------------------------------------------
    // Internal executor with validation
    // --------------------------------------------------
    private suspend fun execute(request: Request): String? {
        return withContext(Dispatchers.IO) {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    throw IllegalStateException(
                        "HTTP ${response.code}: ${response.message}"
                    )
                }
                response.body?.string()
            }
        }
    }
}
