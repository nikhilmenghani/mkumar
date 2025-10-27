package com.mkumar.data.repository

import androidx.room.withTransaction
import com.mkumar.data.CustomerFormState
import com.mkumar.data.CustomerHeaderDomain
import com.mkumar.data.local.MKumarDatabase
import com.mkumar.data.local.dao.CustomerDao
import com.mkumar.data.local.dao.OrderDao
import com.mkumar.data.local.dao.OrderItemDao
import com.mkumar.data.local.entities.CustomerEntity
import com.mkumar.data.local.entities.OrderEntity
import com.mkumar.data.local.entities.OrderItemEntity
import kotlinx.coroutines.flow.Flow
import java.time.Clock
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CustomerRepository @Inject constructor(
    private val db: MKumarDatabase,
    private val customerDao: CustomerDao,
    private val orderDao: OrderDao,
    private val orderItemDao: OrderItemDao,
    private val clock: Clock
) {

    fun getAllCustomersFlow(): Flow<List<CustomerEntity>> =
        customerDao.observeAll()

    suspend fun upsertCustomerOnly(customer: CustomerEntity): String = db.withTransaction {
        customerDao.upsert(customer)
        customer.id
    }

    suspend fun deleteCustomer(customerId: String) = db.withTransaction {
        val orderList: List<OrderEntity> = orderDao.getForCustomer(customerId)
        for (order in orderList) {
            orderItemDao.deleteByOrderId(order.id)
            orderDao.deleteById(order.id)
        }
        customerDao.deleteById(customerId)
    }

    suspend fun customerHeader(id: String): CustomerHeaderDomain {
        val row = customerDao.getWithOrders(id)  // returns a DB projection
        val orders = row?.orders
        return CustomerHeaderDomain(
            id = row!!.customer.id,
            displayName = row.customer.name,
            phoneFormatted = row.customer.phone, // or format here
            totalOrders = orders?.size,       // nullable in Phase 1 if expensive
            lifetimeValueFormatted = "", // nullable ok
            lastVisitFormatted = ""
        )
    }
}
