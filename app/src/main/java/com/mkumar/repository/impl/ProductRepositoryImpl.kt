package com.mkumar.repository.impl

import androidx.room.withTransaction
import com.mkumar.common.extension.nowUtcMillis
import com.mkumar.data.db.AppDatabase
import com.mkumar.data.db.dao.CustomerDao
import com.mkumar.data.db.dao.OrderDao
import com.mkumar.data.db.dao.OrderItemDao
import com.mkumar.data.db.dao.SearchDao
import com.mkumar.data.db.entities.OrderEntity
import com.mkumar.data.db.entities.OrderItemEntity
import com.mkumar.repository.ProductRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProductRepositoryImpl @Inject constructor(
    private val db: AppDatabase,
    private val orderItemDao: OrderItemDao,
    private val orderDao: OrderDao,
    private val customerDao: CustomerDao,
    private val searchDao: SearchDao,
) : ProductRepository {

    override suspend fun upsert(item: OrderItemEntity) {
        db.withTransaction {
            val now = nowUtcMillis()

            // 1. Update item
            orderItemDao.upsert(item.copy(updatedAt = now))

            // 2. Rebuild order aggregates
            recomputeOrderAggregates(item.orderId, now)
        }
    }


    override suspend fun insertAll(items: List<OrderItemEntity>) {
        if (items.isEmpty()) return

        db.withTransaction {
            val now = nowUtcMillis()
            val stamped = items.map { it.copy(updatedAt = now) }

            // 1. Insert all items
            orderItemDao.insertAll(stamped)

            // 2. For each affected order, recompute aggregates
            stamped.map { it.orderId }
                .distinct()
                .forEach { orderId ->
                    recomputeOrderAggregates(orderId, now)
                }
        }
    }

    override suspend fun deleteByOrderId(orderId: String) {
        db.withTransaction {
            orderItemDao.deleteByOrderId(orderId)

            recomputeOrderAggregates(orderId, nowUtcMillis())
        }
    }

    override suspend fun deleteProductById(itemId: String) {
        db.withTransaction {
            val orderId = orderItemDao.getOrderIdByItemId(itemId) ?: return@withTransaction

            orderItemDao.deleteProductById(itemId)

            recomputeOrderAggregates(orderId, nowUtcMillis())
        }
    }

    override fun observeItemsForOrder(orderId: String): Flow<List<OrderItemEntity>> =
        orderItemDao.observeItemsForOrder(orderId)

    override suspend fun getItemsForOrder(orderId: String): List<OrderItemEntity> =
        orderItemDao.getItemsForOrder(orderId)

    override fun countItemsForOrder(orderId: String): Int =
        orderItemDao.countForOrder(orderId)

    private suspend fun recomputeOrderAggregates(orderId: String, now: Long) {
        // Load order
        val order = orderDao.getById(orderId) ?: return

        val items = orderItemDao.getItemsForOrder(orderId)

        // Rebuild category + owner lists
        val categories = items.map { it.productTypeLabel }.filter { it.isNotBlank() }
        val owners = items.map { it.productOwnerName }.filter { it.isNotBlank() }

        // Update order entity
        val updatedOrder = order.copy(
            productCategories = categories,
            owners = owners,
            updatedAt = now
        )
        orderDao.upsert(updatedOrder)

        // Update customer summary
        updateCustomerSummary(order.customerId)

        // Rebuild FTS entry
        reindexOrderFts(updatedOrder, items)
    }

    private suspend fun updateCustomerSummary(customerId: String) {
        val orders = orderDao.getForCustomer(customerId)
        val totalOutstanding = orders.sumOf { it.remainingBalance }
        val hasPending = orders.any { it.remainingBalance > 0 }

        val c = customerDao.getById(customerId) ?: return

        customerDao.upsert(
            c.copy(
                totalOutstanding = totalOutstanding,
                hasPendingOrder = hasPending,
                updatedAt = nowUtcMillis()
            )
        )
    }

    private suspend fun reindexOrderFts(order: OrderEntity, items: List<OrderItemEntity>) {

        // Raw tokens (as before)
        val tokens = buildList {
            add(order.invoiceSeq?.toString().orEmpty())
            addAll(order.productCategories)
            addAll(order.owners)
            addAll(items.mapNotNull { it.productTypeLabel.takeIf { lbl -> lbl.isNotBlank() } })
        }

        // Clean: trim → drop empty → distinct → sorted
        val cleanedTokens = tokens
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .distinct()
            .sorted()

        val content = cleanedTokens.joinToString(" ")

        searchDao.updateOrderContent(order.id, content)
    }

}
