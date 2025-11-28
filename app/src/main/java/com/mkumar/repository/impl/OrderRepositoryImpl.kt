package com.mkumar.repository.impl

import androidx.room.withTransaction
import com.mkumar.common.extension.nowUtcMillis
import com.mkumar.data.db.AppDatabase
import com.mkumar.data.db.dao.CustomerDao
import com.mkumar.data.db.dao.OrderDao
import com.mkumar.data.db.dao.OrderItemDao
import com.mkumar.data.db.dao.SearchDao
import com.mkumar.data.db.entities.OrderEntity
import com.mkumar.data.services.InvoiceNumberService
import com.mkumar.model.UiCustomerMini
import com.mkumar.repository.OrderRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OrderRepositoryImpl @Inject constructor(
    private val db: AppDatabase,
    private val orderDao: OrderDao,
    private val orderItemDao: OrderItemDao,
    private val customerDao: CustomerDao,
    private val searchDao: SearchDao,
    private val invoiceNumberService: InvoiceNumberService
) : OrderRepository {

    override suspend fun upsert(order: OrderEntity) = db.withTransaction {
        val now = nowUtcMillis()

        val enriched = order.copy(
            productCategories = orderItemDao.getCategoriesForOrder(order.id),
            owners = orderItemDao.getOwnersForOrder(order.id)
        )

        orderDao.upsert(enriched)

        // update customer outstanding
        updateCustomerSummary(enriched.customerId)
    }

    override suspend fun delete(orderId: String) = db.withTransaction {
        val order = orderDao.getById(orderId) ?: return@withTransaction

        orderDao.deleteById(orderId)

        // Delete FTS entries
        searchDao.deleteByCustomerId(order.customerId)

        // Reindex customer ONLY (we will rebuild new FTS)
        updateCustomerSummary(order.customerId)
    }


    override fun observeOrdersForCustomer(customerId: String): Flow<List<OrderEntity>> =
        orderDao.observeOrdersForCustomer(customerId)

    override fun observeOrder(orderId: String): Flow<OrderEntity?> =
        orderDao.observeOrder(orderId)

    override suspend fun getOrder(orderId: String): OrderEntity? =
        orderDao.getById(orderId)

    override suspend fun createOrderWithItems(order: OrderEntity): OrderEntity = db.withTransaction {
        val now = nowUtcMillis()
        val invoiceSeq = invoiceNumberService.takeNextInvoiceNumberInCurrentTx()

        // Aggregate categories + owners
        val categories = orderItemDao.getCategoriesForOrder(order.id)
        val owners = orderItemDao.getOwnersForOrder(order.id)

        val stamped = order.copy(
            invoiceSeq = invoiceSeq,
            updatedAt = now,
            productCategories = categories,
            owners = owners
        )

        orderDao.upsert(stamped)

        // update customer summary
        updateCustomerSummary(order.customerId)

        stamped
    }

    override suspend fun searchOrders(
        customerId: String,
        invoice: String?,
        category: String?,
        owner: String?,
        remainingOnly: Boolean
    ): List<OrderEntity> {

        // We use the DAO-level dynamic filter
        return orderDao.filterOrders(
            customerId = customerId,
            remainingOnly = if (remainingOnly) 1 else 0,
            category = category,
            owner = owner
        )
    }

    override suspend fun getCustomerMiniForOrder(customerId: String): UiCustomerMini {
        val customer = customerDao.getById(customerId)
            ?: throw IllegalArgumentException("Customer not found for customer: $customerId")

        return UiCustomerMini(
            id = customer.id,
            name = customer.name,
            phone = customer.phone,
        )
    }

    private suspend fun updateCustomerSummary(customerId: String) {
        val orders = orderDao.getForCustomer(customerId)
        val totalOutstanding = orders.sumOf { it.remainingBalance }
        val hasPending = orders.any { it.remainingBalance > 0 }

        val customer = customerDao.getById(customerId) ?: return

        customerDao.upsert(
            customer.copy(
                totalOutstanding = totalOutstanding,
                hasPendingOrder = hasPending,
                updatedAt = System.currentTimeMillis()
            )
        )
    }
}
