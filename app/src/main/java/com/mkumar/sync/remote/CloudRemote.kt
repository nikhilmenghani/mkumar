package com.mkumar.sync.remote

interface CloudRemote {

    /**
     * List all file paths under a folder.
     *
     * Example return:
     * [
     *   "customers/abc/profile.json",
     *   "customers/abc/orders/ord123.json",
     *   "payments/pay1.json"
     * ]
     */
    suspend fun list(folder: String): List<String>

    /**
     * Download a file as raw JSON text.
     * Returns null if file does not exist.
     */
    suspend fun get(path: String): String?

    /**
     * Create or update a JSON file at exact path.
     *
     * The path must include folder + filename, for example:
     *   "customers/abc/profile.json"
     */
    suspend fun putJson(path: String, content: String)

    /**
     * Delete a cloud file by exact path.
     */
    suspend fun delete(path: String)
}
