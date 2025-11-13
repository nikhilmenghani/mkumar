package com.mkumar.repository.impl

import androidx.room.withTransaction
import com.mkumar.data.db.AppDatabase
import com.mkumar.data.db.dao.OrderDao
import com.mkumar.data.db.entities.OrderEntity
import com.mkumar.data.services.InvoiceNumberService
import com.mkumar.repository.OrderRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OrderRepositoryImpl @Inject constructor(
    private val db: AppDatabase,
    private val orderDao: OrderDao,
    private val invoiceNumberService: InvoiceNumberService
) : OrderRepository {

    override suspend fun upsert(order: OrderEntity) = orderDao.upsert(order)

    override suspend fun delete(orderId: String) = orderDao.deleteById(orderId)

    override fun observeOrdersForCustomer(customerId: String): Flow<List<OrderEntity>> =
        orderDao.observeOrdersForCustomer(customerId)

    override fun observeOrder(orderId: String): Flow<OrderEntity?> =
        orderDao.observeOrder(orderId)

    override suspend fun getOrder(orderId: String): OrderEntity? =
        orderDao.getById(orderId)

    override suspend fun createOrderWithItems(
        order: OrderEntity
    ): OrderEntity = db.withTransaction {
        val invoiceSeq = invoiceNumberService.takeNextInvoiceNumberInCurrentTx()
        val orderWithInvoice = order.copy(invoiceSeq = invoiceSeq)
        orderDao.upsert(orderWithInvoice)

        orderWithInvoice
    }
}
