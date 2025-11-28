package com.mkumar.repository.impl

import androidx.room.withTransaction
import com.mkumar.common.extension.nowUtcMillis
import com.mkumar.data.db.AppDatabase
import com.mkumar.data.db.dao.CustomerDao
import com.mkumar.data.db.dao.CustomerFtsDao
import com.mkumar.data.db.dao.OrderDao
import com.mkumar.data.db.dao.OrderFtsDao
import com.mkumar.data.db.dao.OrderItemDao
import com.mkumar.data.db.entities.OrderEntity
import com.mkumar.data.db.entities.OrderFts
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
    private val customerFtsDao: CustomerFtsDao,
    private val orderFtsDao: OrderFtsDao
) : ProductRepository {

    override suspend fun upsert(item: OrderItemEntity) {
        db.withTransaction {
            val now = nowUtcMillis()
            orderItemDao.upsert(item.copy(updatedAt = now))
            recomputeOrderAggregates(item.orderId, now)
        }
    }

    override suspend fun insertAll(items: List<OrderItemEntity>) {
        if (items.isEmpty()) return

        db.withTransaction {
            val now = nowUtcMillis()
            val stamped = items.map { it.copy(updatedAt = now) }

            orderItemDao.insertAll(stamped)

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
        val order = orderDao.getById(orderId) ?: return
        val items = orderItemDao.getItemsForOrder(orderId)

        val categories = items.map { it.productTypeLabel }.filter { it.isNotBlank() }
        val owners = items.map { it.productOwnerName }.filter { it.isNotBlank() }

        val updated = order.copy(
            productCategories = categories,
            owners = owners,
            updatedAt = now
        )

        orderDao.upsert(updated)
        updateCustomerSummary(order.customerId)
        reindexOrderFts(updated, items)
    }

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


    // --------------------------
    // ORDER FTS INDEXING
    // --------------------------
    private suspend fun reindexOrderFts(order: OrderEntity, items: List<OrderItemEntity>) {
        val tokens = buildList {
            add(order.invoiceSeq?.toString().orEmpty())
            addAll(order.productCategories)
            addAll(order.owners)
            addAll(items.mapNotNull { it.productTypeLabel.takeIf { it.isNotBlank() } })
        }

        val cleaned = tokens.map { it.trim() }
            .filter { it.isNotEmpty() }
            .distinct()
            .sorted()

        val content = cleaned.joinToString(" ")

        orderFtsDao.upsert(
            OrderFts(
                customerId = order.customerId,
                orderId = order.id,
                invoiceSeq = order.invoiceSeq?.toString(),
                productCategories = order.productCategories.joinToString(" "),
                owners = order.owners.joinToString(" "),
                productTypes = items.joinToString(" ") { it.productTypeLabel },
                content = content
            )
        )
    }
}
