package com.mkumar.repository

import kotlinx.coroutines.flow.Flow

interface SearchRepository {

    // --- Customer-level FTS search ---
    suspend fun searchCustomerIds(match: String, limit: Int = 50): List<String>
    fun observeSearchCustomerIds(match: String, limit: Int = 50): Flow<List<String>>

    // --- Order-level FTS search (for inside customer detail screen) ---
    suspend fun searchOrderIds(match: String, limit: Int = 50): List<String>

    // --- Combined search (returns pair of customerIds + orderIds) ---
    suspend fun search(match: String, limit: Int = 100): SearchResult
}

data class SearchResult(
    val customerIds: List<String>,
    val orderIds: List<String>
)
