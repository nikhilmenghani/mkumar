package com.mkumar.repository.impl

import androidx.sqlite.db.SimpleSQLiteQuery
import com.mkumar.common.extension.nowUtcMillis
import com.mkumar.common.search.buildFtsPrefixMatch
import com.mkumar.common.search.buildFtsTrigramMatch
import com.mkumar.common.search.digitsOnly
import com.mkumar.common.search.foldName
import com.mkumar.common.search.ngrams
import com.mkumar.data.db.dao.CustomerDao
import com.mkumar.data.db.dao.OrderDao
import com.mkumar.data.db.dao.SearchDao
import com.mkumar.data.db.entities.CustomerEntity
import com.mkumar.data.db.relations.CustomerWithOrders
import com.mkumar.model.OrderWithCustomerInfo
import com.mkumar.model.SearchMode
import com.mkumar.model.UiCustomerMini
import com.mkumar.repository.CustomerRepository
import com.mkumar.viewmodel.toUiModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CustomerRepositoryImpl @Inject constructor(
    private val customerDao: CustomerDao,
    private val orderDao: OrderDao,
    private val searchDao: SearchDao
) : CustomerRepository {

    override suspend fun upsert(customer: CustomerEntity) {
        val updated = customer.copy(updatedAt = nowUtcMillis())
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

    override fun getRecentCustomers(limit: Int): Flow<List<UiCustomerMini>> =
        customerDao.getRecentCustomers(limit).map { list ->
            list.map { it.toUiModel() }   // same mapper used everywhere else
        }

    override fun getRecentCustomerList(limit: Int): List<UiCustomerMini> =
        customerDao.getRecentCustomerList(limit).map { it.toUiModel() }


    override fun getRecentOrders(limit: Int, sortBy: String, ascending: Boolean): Flow<List<OrderWithCustomerInfo>> {
//        orderDao.getRecentOrdersWithCustomer(limit, sortBy, ascending)
        return getRecentOrdersWithCustomer(limit, sortBy, ascending)
    }

    fun getRecentOrdersWithCustomer(limit: Int, sortBy: String, ascending: Boolean): Flow<List<OrderWithCustomerInfo>> {
        val order = if (ascending) "ASC" else "DESC"
        val validSortColumns = setOf("invoiceSeq", "updatedAt", "name") // whitelist allowed columns
        val sortColumn = when (sortBy) {
            "Invoice" -> "o.invoiceSeq"
            "UpdatedAt" -> "o.updatedAt"
            "Name" -> "c.name"
            else -> "o.occurredAt"
        }
        val sql = """
        SELECT o.id, o.invoiceSeq AS invoiceNumber, o.createdAt, o.totalAmount, o.remainingBalance, o.customerId, c.name AS customerName, c.phone AS customerPhone
        FROM orders o
        JOIN customers c ON c.id = o.customerId
        ORDER BY $sortColumn $order
        LIMIT ?
    """.trimIndent()
        val query = SimpleSQLiteQuery(sql, arrayOf(limit))
        return orderDao.getRecentOrdersWithCustomerRaw(query)
    }

    override suspend fun reindexCustomerForSearch(customer: CustomerEntity) =
        withContext(Dispatchers.IO) {

            val foldedName = foldName(customer.name)
            val digits = digitsOnly(customer.phone)

            val name3 = ngrams(foldedName.replace(" ", ""), 3)
                .joinToString(" ")
                .ifBlank { null }

            val phone3 = digits?.let {
                ngrams(it, 3).joinToString(" ").ifBlank { null }
            }

            // UPDATE ONLY CUSTOMER FIELDS — do NOT touch content/orderId
            searchDao.upsertCustomerFields(
                customerId = customer.id,
                name = foldedName,
                phone = digits,
                name3 = name3,
                phone3 = phone3
            )
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

    override suspend fun searchCustomersByInvoice(invoice: String): List<UiCustomerMini> {
        if (invoice.isBlank()) return emptyList()

        val ids = customerDao.findCustomerIdsByInvoice(invoice)

        if (ids.isEmpty()) return emptyList()

        val minis = customerDao.loadMiniByIds(ids)
            .map { UiCustomerMini(it.id, it.name, it.phone) }

        return minis.orderByIds(ids)
    }

    override suspend fun searchCustomersWithPendingBalance(): List<UiCustomerMini> {
        val ids = customerDao.findCustomerIdsWithPendingBalance()

        if (ids.isEmpty()) return emptyList()

        val minis = customerDao.loadMiniByIds(ids)
            .map { UiCustomerMini(it.id, it.name, it.phone) }

        return minis.orderByIds(ids)
    }

    override suspend fun searchCustomersAdvanced(
        nameOrPhone: String?,
        invoice: String?,
        remainingOnly: Boolean,
        searchMode: SearchMode
    ): List<UiCustomerMini> {

        val idSets = mutableListOf<List<String>>()

        // 1. Basic search box
        if (!nameOrPhone.isNullOrBlank()) {
            val base = searchCustomers(nameOrPhone, searchMode).map { it.id }
            idSets += base
        }

        // 2. Invoice filter
//        if (!invoice.isNullOrBlank()) {
//            idSets += customerDao.findCustomerIdsByInvoice(invoice)
//        }

        // 3. Remaining > 0 filter
        if (remainingOnly) {
            idSets += customerDao.findCustomerIdsWithPendingBalance()
        }

        if (idSets.isEmpty()) return emptyList()

        // Intersection of all filters (AND logic)
        val finalIds = idSets.reduce { acc, list -> acc.intersect(list.toSet()).toList() }

        if (finalIds.isEmpty()) return emptyList()

        val minis = customerDao.loadMiniByIds(finalIds)
            .map { UiCustomerMini(it.id, it.name, it.phone) }

        return minis.orderByIds(finalIds)
    }

    override suspend fun searchOrdersAdvanced(
        invoice: String?
    ): List<OrderWithCustomerInfo> {
        val orders = orderDao.searchOrdersByInvoice(invoice.toString())
        if (orders.isEmpty()) return emptyList()

        val customerIds = orders.map { it.customerId }.distinct()
        val customers = customerDao.loadMiniByIds(customerIds)
            .associateBy { it.id }

        val result = orders.mapNotNull { order ->
            val customer = customers[order.customerId]
            customer?.let {
                OrderWithCustomerInfo(
                    id = order.id,
                    invoiceNumber = order.invoiceSeq ?: 0L,
                    createdAt = order.createdAt,
                    totalAmount = order.totalAmount,
                    remainingBalance = order.remainingBalance,
                    customerId = order.customerId,
                    customerName = it.name,
                    customerPhone = it.phone
                )
            }
        }
        return result
    }

    // Preserve FTS/combined order
    private fun List<UiCustomerMini>.orderByIds(ids: List<String>): List<UiCustomerMini> {
        val pos = ids.withIndex().associate { it.value to it.index }
        return this.sortedBy { pos[it.id] ?: Int.MAX_VALUE }
    }
}

// Simple in-memory reorder to preserve id order returned by FTS
private fun List<UiCustomerMini>.orderByIds(ids: List<String>): List<UiCustomerMini> {
    val pos = ids.withIndex().associate { it.value to it.index }
    return this.sortedBy { pos[it.id] ?: Int.MAX_VALUE }
}