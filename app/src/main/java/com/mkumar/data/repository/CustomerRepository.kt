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

    fun getCustomerWithOrdersFlow(customerId: String) =
        customerDao.observeWithOrders(customerId)

    /**
     * Persist a customer and (optionally) an order with items in a SINGLE transaction.
     * No pricing math here—pass precomputed totals via [OrderDraft] if you want to save an order.
     */
    suspend fun saveCustomer(
        form: CustomerFormState,
        orderDraft: OrderDraft? = null
    ): String = db.withTransaction {
        val now = Instant.now(clock)

        val customer = CustomerEntity(
            id = form.id,
            name = form.name.trim(),
            phone = form.phone.trim()
        )
        customerDao.upsert(customer)

        if (orderDraft != null) {
            val order = OrderEntity(
                id = orderDraft.orderId ?: generateOrderId(),
                customerId = customer.id
            )
            orderDao.upsert(order)

            // Replace items for this order
            orderItemDao.deleteByOrderId(order.id)
            val items = orderDraft.items.mapIndexed { idx, it ->
                OrderItemEntity(
                    id = generateItemId(),
                    orderId = order.id
                )
            }
            orderItemDao.insertAll(items)
        }

        customer.id
    }

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

    suspend fun createOrder(
        customerId: String,
        draft: OrderDraft
    ): String = db.withTransaction {
        val order = OrderEntity(
            id = draft.orderId ?: generateOrderId(),
            customerId = customerId
        )
        orderDao.insert(order)

        val items = draft.items.mapIndexed { idx, it ->
            OrderItemEntity(
                id = generateItemId(),
                orderId = order.id,
                productTypeLabel = ""
            )
        }
        orderItemDao.insertAll(items)
        order.id
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

    private fun generateOrderId() = UUID.randomUUID().toString()
    private fun generateItemId() = UUID.randomUUID().toString()
}

/** Lightweight inputs passed from UI/mappers when saving an order (no pricing logic here). */
data class OrderDraft(
    val orderId: String? = null,
    val occurredAt: Long? = null,
    val subTotal: Double = 0.0,
    val discount: Double = 0.0,
    val tax: Double = 0.0,
    val grandTotal: Double = 0.0,
    val note: String? = null,
    val items: List<OrderItemInput>
)

data class OrderItemInput(
    val sku: String?,
    val name: String,
    val quantity: Int,
    val unitPrice: Double,
    val lineTotal: Double
)
