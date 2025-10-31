package com.mkumar.repository.impl

import com.mkumar.data.db.dao.OrderItemDao
import com.mkumar.data.db.entities.OrderItemEntity
import com.mkumar.repository.ProductRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProductRepositoryImpl @Inject constructor(
    private val orderItemDao: OrderItemDao
) : ProductRepository {

    override suspend fun upsert(item: OrderItemEntity) = orderItemDao.upsert(item)

    override suspend fun insertAll(items: List<OrderItemEntity>) = orderItemDao.insertAll(items)

    override suspend fun deleteByOrderId(orderId: String) = orderItemDao.deleteByOrderId(orderId)

    override suspend fun deleteProductById(itemId: String) = orderItemDao.deleteProductById(itemId)

    override fun observeItemsForOrder(orderId: String): Flow<List<OrderItemEntity>> =
        orderItemDao.observeItemsForOrder(orderId)

    override suspend fun getItemsForOrder(orderId: String): List<OrderItemEntity> =
        orderItemDao.getItemsForOrder(orderId)
}
