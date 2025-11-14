package com.mkumar.repository.impl

import com.mkumar.common.search.buildFtsPrefixMatch
import com.mkumar.common.search.buildFtsTrigramMatch
import com.mkumar.common.search.digitsOnly
import com.mkumar.common.search.foldName
import com.mkumar.common.search.ngrams
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
        val updated = customer.copy(updatedAt = System.currentTimeMillis())
        customerDao.upsert(updated)
        reindexCustomerForSearch(updated)
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

    override suspend fun reindexCustomerForSearch(customer: CustomerEntity) = withContext(Dispatchers.IO) {
        val folded = foldName(customer.name).replace(" ", "")
        val digits = digitsOnly(customer.phone)
        val name3 = ngrams(folded, 3).joinToString(" ").ifBlank { null }
        val phone3 = digits?.let { d -> ngrams(d, 3).joinToString(" ").ifBlank { null } }
        val entry = SearchFts(
            customerId = customer.id,
            name = foldName(customer.name),
            phone = digits,
            name3 = name3,
            phone3 = phone3
        )
        searchDao.upsert(entry)
    }

    override suspend fun searchCustomers(q: String, mode: SearchMode, limit: Int): List<UiCustomerMini> {
        val query = q.trim()
        if (query.isEmpty()) return emptyList()


        val ids: List<String> = when (mode) {
            SearchMode.QUICK -> {
                val m = buildFtsPrefixMatch(query)
                if (m.isBlank()) emptyList() else searchDao.searchCustomerIds(m, limit)
            }
            SearchMode.FLEXIBLE -> {
                val trig = buildFtsTrigramMatch(query)
                if (trig != null) {
                    searchDao.searchCustomerIds(trig, limit)
                } else {
                    // Short query (<3): prefix + LIKE fallback
                    val pref = buildFtsPrefixMatch(query)
                    val idsPrefix = if (pref.isNotBlank()) searchDao.searchCustomerIds(pref, limit) else emptyList()
                    val idsContains = customerDao.containsCustomerIds(query, query.filter(Char::isDigit), limit)
                    (idsPrefix + idsContains).distinct().take(limit)
                }
            }
        }
        if (ids.isEmpty()) return emptyList()
        val rows = customerDao.loadMiniByIds(ids)
        val minis = rows.map { UiCustomerMini(it.id, it.name, it.phone) }
        return minis.orderByIds(ids)
    }

    // Preserve FTS/combined order
    private fun List<UiCustomerMini>.orderByIds(ids: List<String>): List<UiCustomerMini> {
        val pos = ids.withIndex().associate { it.value to it.index }
        return this.sortedBy { pos[it.id] ?: Int.MAX_VALUE }
    }
}

enum class SearchMode { QUICK, FLEXIBLE }

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