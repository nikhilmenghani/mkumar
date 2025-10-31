package com.mkumar.repository.impl

import com.mkumar.data.db.dao.SearchDao
import com.mkumar.repository.SearchRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SearchRepositoryImpl @Inject constructor(
    private val searchDao: SearchDao
) : SearchRepository {

    override suspend fun searchCustomerIds(match: String, limit: Int): List<String> =
        searchDao.searchCustomerIds(match, limit)

    override fun observeSearchCustomerIds(match: String, limit: Int): Flow<List<String>> =
        searchDao.observeSearchCustomerIds(match, limit)
}
