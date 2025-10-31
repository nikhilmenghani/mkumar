package com.mkumar.repository

import kotlinx.coroutines.flow.Flow

interface SearchRepository {
    suspend fun searchCustomerIds(match: String, limit: Int = 50): List<String>
    fun observeSearchCustomerIds(match: String, limit: Int = 50): Flow<List<String>>
}
