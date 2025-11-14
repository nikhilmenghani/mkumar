package com.mkumar.repository.impl

import androidx.room.withTransaction
import com.mkumar.data.db.AppDatabase
import com.mkumar.data.db.dao.OrderDao
import com.mkumar.data.db.dao.OrderItemDao
import com.mkumar.data.db.entities.OrderItemEntity
import com.mkumar.repository.ProductRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProductRepositoryImpl @Inject constructor(
    private val db: AppDatabase,
    private val orderItemDao: OrderItemDao,
    private val orderDao: OrderDao
) : ProductRepository {

    override suspend fun upsert(item: OrderItemEntity) {
        db.withTransaction {
            val now = System.currentTimeMillis()
            orderItemDao.upsert(item.copy(updatedAt = now))
            orderDao.touchUpdatedAt(item.orderId, now)
        }
    }


    override suspend fun insertAll(items: List<OrderItemEntity>) {
        if (items.isEmpty()) return
        db.withTransaction {
            val now = System.currentTimeMillis()
            val stamped = items.map { it.copy(updatedAt = now) }
            orderItemDao.insertAll(stamped)
            stamped.map { it.orderId }.distinct().forEach { orderId ->
                orderDao.touchUpdatedAt(orderId, now)
            }
        }
    }

    override suspend fun deleteByOrderId(orderId: String) {
        db.withTransaction {
            orderItemDao.deleteByOrderId(orderId)
            orderDao.touchUpdatedAt(orderId, System.currentTimeMillis())
        }
    }

    override suspend fun deleteProductById(itemId: String) {
        db.withTransaction {
            val orderId = orderItemDao.getOrderIdByItemId(itemId)
            orderItemDao.deleteProductById(itemId)
            orderId?.let { orderDao.touchUpdatedAt(it, System.currentTimeMillis()) }
        }
    }

    override fun observeItemsForOrder(orderId: String): Flow<List<OrderItemEntity>> =
        orderItemDao.observeItemsForOrder(orderId)

    override suspend fun getItemsForOrder(orderId: String): List<OrderItemEntity> =
        orderItemDao.getItemsForOrder(orderId)

    override fun countItemsForOrder(orderId: String): Int =
        orderItemDao.countForOrder(orderId)
}
