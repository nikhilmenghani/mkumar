package com.mkumar.repository.impl

import com.mkumar.common.search.digitsOnly
import com.mkumar.common.search.foldName
import com.mkumar.data.db.dao.CustomerDao
import com.mkumar.data.db.dao.SearchDao
import com.mkumar.data.db.entities.CustomerEntity
import com.mkumar.data.db.entities.SearchFts
import com.mkumar.data.db.relations.CustomerWithOrders
import com.mkumar.repository.CustomerRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CustomerRepositoryImpl @Inject constructor(
    private val customerDao: CustomerDao,
    private val searchDao: SearchDao
) : CustomerRepository {

    override suspend fun upsert(customer: CustomerEntity) {
        customerDao.upsert(customer)
        reindexCustomerForSearch(customer)
    }

    override suspend fun deleteById(customerId: String) {
        customerDao.deleteById(customerId)
        searchDao.deleteByCustomerId(customerId)
    }

    override fun observeAll(): Flow<List<CustomerEntity>> = customerDao.observeAll()

    override suspend fun getAll(): List<CustomerEntity> = customerDao.getAll()

    override fun observeWithOrders(customerId: String): Flow<CustomerWithOrders?> =
        customerDao.observeWithOrders(customerId)

    override suspend fun getWithOrders(customerId: String): CustomerWithOrders? =
        customerDao.getWithOrders(customerId)

    override suspend fun reindexCustomerForSearch(customer: CustomerEntity) {
        val entry = SearchFts(
            customerId = customer.id,
            name = customer.name,
            phone = customer.phone
        )
        searchDao.upsert(entry)
    }

    /** Upsert Customer AND its FTS row in one transaction (Room @Transaction on DAO is fine too). */
    suspend fun upsertCustomerWithIndex(c: CustomerEntity) = withContext(Dispatchers.IO) {
        customerDao.upsert(c)
        val entry = SearchFts(
            customerId = c.id,
            name = foldName(c.name),
            phone = digitsOnly(c.phone)
        )
        searchDao.upsert(entry)
    }


    /** Delete customer AND remove from FTS. */
    suspend fun deleteCustomerWithIndex(customerId: String) = withContext(Dispatchers.IO) {
        customerDao.deleteById(customerId)
        searchDao.deleteByCustomerId(customerId)
    }


    /** Lightweight projection for search results. */
    override suspend fun searchCustomers(q: String, limit: Int): List<UiCustomerMini> {
        val match = com.mkumar.common.search.buildFtsMatch(q)
        if (match.isBlank()) return emptyList()
        val ids = searchDao.searchCustomerIds(match, limit)
        if (ids.isEmpty()) return emptyList()
        return customerDao.loadMiniByIds(ids)
            .orderByIds(ids) // keep FTS order
    }
}

// Minimal UI projection model (place where you keep other UI models)
data class UiCustomerMini(
    val id: String,
    val name: String,
    val phone: String?
)


// Simple in-memory reorder to preserve id order returned by FTS
private fun List<UiCustomerMini>.orderByIds(ids: List<String>): List<UiCustomerMini> {
    val pos = ids.withIndex().associate { it.value to it.index }
    return this.sortedBy { pos[it.id] ?: Int.MAX_VALUE }
}