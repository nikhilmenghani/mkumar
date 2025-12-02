package com.mkumar.repository.impl

import androidx.sqlite.db.SimpleSQLiteQuery
import com.mkumar.common.extension.nowUtcMillis
import com.mkumar.common.search.buildFtsPrefixMatch
import com.mkumar.common.search.buildFtsTrigramMatch
import com.mkumar.common.search.digitsOnly
import com.mkumar.common.search.foldName
import com.mkumar.common.search.ngrams
import com.mkumar.data.db.dao.CustomerDao
import com.mkumar.data.db.dao.CustomerFtsDao
import com.mkumar.data.db.dao.OrderDao
import com.mkumar.data.db.dao.OrderFtsDao
import com.mkumar.data.db.entities.CustomerEntity
import com.mkumar.data.db.entities.CustomerFts
import com.mkumar.data.db.entities.toSyncDto
import com.mkumar.data.db.relations.CustomerWithOrders
import com.mkumar.model.OrderWithCustomerInfo
import com.mkumar.model.SearchMode
import com.mkumar.model.UiCustomerMini
import com.mkumar.repository.CustomerRepository
import com.mkumar.repository.SyncRepository
import com.mkumar.repository.orderByIds
import com.mkumar.viewmodel.toUiModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CustomerRepositoryImpl @Inject constructor(
    private val customerDao: CustomerDao,
    private val orderDao: OrderDao,
    private val customerFtsDao: CustomerFtsDao,
    private val orderFtsDao: OrderFtsDao,
    private val syncRepository: SyncRepository,
    private val json: Json
) : CustomerRepository {

    override suspend fun upsert(customer: CustomerEntity) {
        val updated = customer.copy(updatedAt = nowUtcMillis())

        // 1) Write to local DB
        customerDao.upsert(updated)

        // 2) Enqueue sync operation (Google Keep-style: full snapshot)
        val dto = updated.toSyncDto()                 // you define this mapper
        val payload = json.encodeToString(dto)

        syncRepository.enqueueOperation(
            type = "CUSTOMER_UPSERT",
            payloadJson = payload,
            entityId = updated.id,
            cloudPath = "customers/${updated.id}/profile.json",
            priority = 5,
            opUpdatedAt = updated.updatedAt
        )

        // 3) Update FTS index (local search)
        reindexCustomerForSearch(updated)
    }

    override suspend fun deleteById(customerId: String) {
        customerDao.deleteById(customerId)
        customerFtsDao.deleteByCustomerId(customerId)
        orderFtsDao.deleteByCustomerId(customerId)

        // NOTE: For now we are NOT enqueuing a delete op to cloud.
        // We can add "CUSTOMER_DELETE" later with a small payload DTO.
    }

    override fun observeAll(): Flow<List<CustomerEntity>> =
        customerDao.observeAll()

    override suspend fun getAll(): List<CustomerEntity> =
        customerDao.getAll()

    override fun observeWithOrders(customerId: String): Flow<CustomerWithOrders?> =
        customerDao.observeWithOrders(customerId)

    override suspend fun getWithOrders(customerId: String): CustomerWithOrders? =
        customerDao.getWithOrders(customerId)

    override fun getRecentCustomers(limit: Int): Flow<List<UiCustomerMini>> =
        customerDao.getRecentCustomers(limit).map { list ->
            list.map { it.toUiModel() }
        }

    override fun getRecentCustomerList(limit: Int): List<UiCustomerMini> =
        customerDao.getRecentCustomerList(limit).map { it.toUiModel() }

    override fun getRecentOrders(
        limit: Int,
        sortBy: String,
        ascending: Boolean
    ): Flow<List<OrderWithCustomerInfo>> {
        return getRecentOrdersWithCustomer(limit, sortBy, ascending)
    }

    fun getRecentOrdersWithCustomer(
        limit: Int,
        sortBy: String,
        ascending: Boolean
    ): Flow<List<OrderWithCustomerInfo>> {
        val order = if (ascending) "ASC" else "DESC"
        val sortColumn = when (sortBy) {
            "Invoice" -> "o.invoiceSeq"
            "UpdatedAt" -> "o.updatedAt"
            "Name" -> "c.name"
            else -> "o.occurredAt"
        }

        val sql = """
        SELECT 
            o.id, 
            o.invoiceSeq AS invoiceNumber, 
            o.createdAt, 
            o.totalAmount, 
            o.adjustedAmount, 
            o.remainingBalance, 
            o.customerId, 
            c.name AS customerName, 
            c.phone AS customerPhone
        FROM orders o
        JOIN customers c ON c.id = o.customerId
        ORDER BY $sortColumn $order
        LIMIT ?
        """.trimIndent()

        val query = SimpleSQLiteQuery(sql, arrayOf(limit))
        return orderDao.getRecentOrdersWithCustomerRaw(query)
    }

    // --------------------------
    // FTS CUSTOMER INDEXING
    // --------------------------
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

            customerFtsDao.upsert(
                CustomerFts(
                    customerId = customer.id,
                    name = foldedName,
                    phone = digits,
                    name3 = name3,
                    phone3 = phone3
                )
            )
        }

    // --------------------------
    // SEARCH
    // --------------------------
    override suspend fun searchCustomers(q: String, mode: SearchMode, limit: Int): List<UiCustomerMini> {
        val query = q.trim()
        if (query.isEmpty()) return emptyList()

        val ids = when (mode) {
            SearchMode.QUICK -> {
                val m = buildFtsPrefixMatch(query)
                if (m.isBlank()) emptyList() else customerFtsDao.searchCustomerIds(m, limit)
            }

            SearchMode.FLEXIBLE -> {
                val trig = buildFtsTrigramMatch(query)
                if (trig != null) {
                    customerFtsDao.searchCustomerIds(trig, limit)
                } else {
                    val pref = buildFtsPrefixMatch(query)
                    val idsPrefix =
                        if (pref.isNotBlank()) customerFtsDao.searchCustomerIds(pref, limit) else emptyList()

                    val idsContains =
                        customerDao.containsCustomerIds(query, query.filter(Char::isDigit), limit)

                    (idsPrefix + idsContains).distinct().take(limit)
                }
            }
        }

        if (ids.isEmpty()) return emptyList()

        val rows = customerDao.loadMiniByIds(ids)
        val minis = rows.map { UiCustomerMini(it.id, it.name, it.phone) }
        return minis.orderByIds(ids) { it.id }
    }

    override suspend fun searchCustomersByInvoice(invoice: String): List<UiCustomerMini> {
        if (invoice.isBlank()) return emptyList()

        val ids = customerDao.findCustomerIdsByInvoice(invoice)
        if (ids.isEmpty()) return emptyList()

        val minis = customerDao.loadMiniByIds(ids).map { UiCustomerMini(it.id, it.name, it.phone) }
        return minis.orderByIds(ids) { it.id }
    }

    override suspend fun searchCustomersWithPendingBalance(): List<UiCustomerMini> {
        val ids = customerDao.findCustomerIdsWithPendingBalance()
        if (ids.isEmpty()) return emptyList()

        val minis = customerDao.loadMiniByIds(ids).map { UiCustomerMini(it.id, it.name, it.phone) }
        return minis.orderByIds(ids) { it.id }
    }

    override suspend fun searchCustomersAdvanced(
        nameOrPhone: String?,
        invoice: String?,
        remainingOnly: Boolean,
        searchMode: SearchMode
    ): List<UiCustomerMini> {

        val idSets = mutableListOf<List<String>>()

        if (!nameOrPhone.isNullOrBlank()) {
            val base = searchCustomers(nameOrPhone, searchMode).map { it.id }
            idSets += base
        }

        if (remainingOnly) {
            idSets += customerDao.findCustomerIdsWithPendingBalance()
        }

        if (idSets.isEmpty()) return emptyList()

        val finalIds = idSets.reduce { acc, list -> acc.intersect(list.toSet()).toList() }
        if (finalIds.isEmpty()) return emptyList()

        val minis = customerDao.loadMiniByIds(finalIds)
            .map { UiCustomerMini(it.id, it.name, it.phone) }

        return minis.orderByIds(finalIds) { it.id }
    }

    override suspend fun searchOrdersAdvanced(invoice: String?): List<OrderWithCustomerInfo> {
        val orders = orderDao.searchOrdersByInvoice(invoice.toString())
        if (orders.isEmpty()) return emptyList()

        val customerIds = orders.map { it.customerId }.distinct()
        val customers = customerDao.loadMiniByIds(customerIds).associateBy { it.id }

        return orders.mapNotNull { order ->
            val c = customers[order.customerId]
            c?.let {
                OrderWithCustomerInfo(
                    id = order.id,
                    invoiceNumber = order.invoiceSeq ?: 0L,
                    createdAt = order.createdAt,
                    totalAmount = order.totalAmount,
                    adjustedAmount = order.adjustedAmount,
                    remainingBalance = order.remainingBalance,
                    customerId = order.customerId,
                    customerName = it.name,
                    customerPhone = it.phone
                )
            }
        }
    }
}
