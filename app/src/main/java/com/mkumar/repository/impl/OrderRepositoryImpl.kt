package com.mkumar.repository.impl

import com.mkumar.data.db.dao.OrderDao
import com.mkumar.data.db.entities.OrderEntity
import com.mkumar.repository.OrderRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OrderRepositoryImpl @Inject constructor(
    private val orderDao: OrderDao
) : OrderRepository {

    override suspend fun upsert(order: OrderEntity) = orderDao.upsert(order)

    override suspend fun delete(orderId: String) = orderDao.deleteById(orderId)

    override fun observeOrdersForCustomer(customerId: String): Flow<List<OrderEntity>> =
        orderDao.observeOrdersForCustomer(customerId)

    override fun observeOrder(orderId: String): Flow<OrderEntity?> =
        orderDao.observeOrder(orderId)

    override suspend fun getOrder(orderId: String): OrderEntity? =
        orderDao.getById(orderId)
}
