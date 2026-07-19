package com.mkumar.repository.impl

import androidx.room.withTransaction
import com.mkumar.common.extension.nowUtcMillis
import com.mkumar.backup.BackupScheduler
import com.mkumar.data.db.AppDatabase
import com.mkumar.data.db.dao.CustomerDao
import com.mkumar.data.db.dao.CustomerFtsDao
import com.mkumar.data.db.dao.OrderDao
import com.mkumar.data.db.dao.OrderFtsDao
import com.mkumar.data.db.dao.OrderItemDao
import com.mkumar.data.db.entities.OrderEntity
import com.mkumar.data.db.entities.OrderFts
import com.mkumar.data.services.InvoiceNumberService
import com.mkumar.model.OrderStatus
import com.mkumar.model.UiCustomerMini
import com.mkumar.repository.OrderRepository
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OrderRepositoryImpl @Inject constructor(
    private val db: AppDatabase,
    private val orderDao: OrderDao,
    private val orderItemDao: OrderItemDao,
    private val customerDao: CustomerDao,
    private val customerFtsDao: CustomerFtsDao,
    private val orderFtsDao: OrderFtsDao,
    private val invoiceNumberService: InvoiceNumberService,
    private val backupScheduler: BackupScheduler
) : OrderRepository {

    // ---------------------------------------------------------------------
    // UPSERT
    // ---------------------------------------------------------------------
    override suspend fun upsert(order: OrderEntity) {
        val previousStatus = orderDao.getById(order.id)?.orderStatus
        db.withTransaction {
            val now = nowUtcMillis()

            val enriched = order.copy(
                productCategories = orderItemDao.getCategoriesForOrder(order.id),
                owners = orderItemDao.getOwnersForOrder(order.id),
                updatedAt = now
            )

            orderDao.upsert(enriched)

            // Reindex FTS
            reindexOrderFts(enriched)

            // Update customer summary
            updateCustomerSummary(enriched.customerId)

        }
        if (previousStatus != OrderStatus.COMPLETED.value && order.orderStatus == OrderStatus.COMPLETED.value) {
            backupScheduler.enqueueOrderCompleted()
        }
    }

    // ---------------------------------------------------------------------
    // DELETE  (Consistent with CustomerRepository.deleteById)
    // ---------------------------------------------------------------------
    override suspend fun delete(orderId: String) = db.withTransaction {

        val order = orderDao.getById(orderId) ?: return@withTransaction

        // Local DB delete
        orderDao.deleteById(orderId)
        orderItemDao.deleteItemsForOrder(orderId)
        orderFtsDao.deleteByOrderId(orderId)

        updateCustomerSummary(order.customerId)

    }

    override suspend fun createDraftOrder(customerId: String): String {
        val orderId = UUID.randomUUID().toString()

        val entity = OrderEntity(
            id = orderId,
            customerId = customerId,
            orderStatus = OrderStatus.DRAFT.value,
            createdAt = nowUtcMillis(),
            receivedAt = nowUtcMillis()
        )

        createOrderWithItems(entity)
        return orderId
    }


    // ---------------------------------------------------------------------
    // OBSERVERS
    // ---------------------------------------------------------------------
    override fun observeOrdersForCustomer(customerId: String): Flow<List<OrderEntity>> =
        orderDao.observeOrdersForCustomer(customerId)

    override fun observeOrder(orderId: String): Flow<OrderEntity?> =
        orderDao.observeOrder(orderId)

    override suspend fun getOrder(orderId: String): OrderEntity? =
        orderDao.getById(orderId)

    override suspend fun updateReceivedAt(orderId: String, receivedAt: Long) {
        orderDao.updateReceivedAt(orderId, receivedAt, nowUtcMillis())
    }

    // ---------------------------------------------------------------------
    // CREATE ORDER
    // ---------------------------------------------------------------------
    override suspend fun createOrderWithItems(order: OrderEntity): OrderEntity = db.withTransaction {
        val now = nowUtcMillis()
        val invoiceSeq = invoiceNumberService.takeNextInvoiceNumberInCurrentTx()

        val categories = orderItemDao.getCategoriesForOrder(order.id)
        val owners = orderItemDao.getOwnersForOrder(order.id)

        val stamped = order.copy(
            invoiceSeq = invoiceSeq,
            updatedAt = now,
            productCategories = categories,
            owners = owners
        )

        orderDao.upsert(stamped)

        reindexOrderFts(stamped)
        updateCustomerSummary(stamped.customerId)

        stamped
    }

    // ---------------------------------------------------------------------
    // SEARCH + HELPERS
    // ---------------------------------------------------------------------
    override suspend fun searchOrders(
        customerId: String,
        invoice: String?,
        category: String?,
        owner: String?,
        remainingOnly: Boolean
    ): List<OrderEntity> =
        orderDao.filterOrders(
            customerId = customerId,
            remainingOnly = if (remainingOnly) 1 else 0,
            category = category,
            owner = owner
        )

    override suspend fun getCustomerMiniForOrder(customerId: String): UiCustomerMini {
        val customer = customerDao.getById(customerId)
            ?: error("Customer not found: $customerId")

        return UiCustomerMini(
            id = customer.id,
            name = customer.name,
            phone = customer.phone
        )
    }

    // ---------------------------------------------------------------------
    // ORDER FTS INDEX
    // ---------------------------------------------------------------------
    private suspend fun reindexOrderFts(order: OrderEntity) {
        val tokens = buildList {
            add(order.invoiceSeq?.toString().orEmpty())
            addAll(order.productCategories)
            addAll(order.owners)
        }

        val cleanedTokens = tokens
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .distinct()
            .sorted()

        val content = cleanedTokens.joinToString(" ")

        orderFtsDao.upsert(
            OrderFts(
                customerId = order.customerId,
                orderId = order.id,
                invoiceSeq = order.invoiceSeq?.toString(),
                productCategories = order.productCategories.joinToString(" "),
                owners = order.owners.joinToString(" "),
                productTypes = "",
                content = content
            )
        )
    }

    // ---------------------------------------------------------------------
    // CUSTOMER SUMMARY UPDATE
    // ---------------------------------------------------------------------
    private suspend fun updateCustomerSummary(customerId: String) {
        val orders = orderDao.getForCustomer(customerId)
        val outstanding = orders.sumOf { it.remainingBalance }
        val hasPending = orders.any { it.remainingBalance > 0 }

        val c = customerDao.getById(customerId) ?: return

        customerDao.upsert(
            c.copy(
                totalOutstanding = outstanding,
                hasPendingOrder = hasPending,
                updatedAt = nowUtcMillis()
            )
        )
    }

}
