package com.mkumar.data.repository

import com.mkumar.data.OrderSummaryDomain
import com.mkumar.data.ProductEntry
import com.mkumar.data.local.MKumarDatabase
import com.mkumar.data.local.dao.CustomerDao
import com.mkumar.data.local.dao.OrderDao
import com.mkumar.data.local.dao.OrderItemDao
import com.mkumar.data.local.entities.OrderEntity
import com.mkumar.data.local.entities.OrderItemEntity
import kotlinx.coroutines.flow.first
import java.time.Clock
import java.time.Instant
import java.util.UUID
import javax.inject.Inject

class OrderRepository @Inject constructor(
    private val db: MKumarDatabase,
    private val customerDao: CustomerDao,
    private val orderDao: OrderDao,
    private val orderItemDao: OrderItemDao,
    private val clock: Clock
) {

    suspend fun ordersForCustomer(customerId: String): List<OrderSummaryDomain> {
        val orderEntities = orderDao.observeForCustomer(customerId).first()
        return orderEntities.map { entity ->
            OrderSummaryDomain(
                id = entity.id,
                occurredAt = Instant.ofEpochMilli(entity.occurredAt),
                isDraft = true,
                subtitle = "",
                totalFormatted = "0"
            )
        }
    }

    suspend fun createDraftOrder(customerId: String): String {
        // Create new draft order entity
        val newOrder = OrderEntity(
            id = UUID.randomUUID().toString(),
            customerId = customerId,
        )
        orderDao.insert(newOrder)
        return newOrder.id
    }

    suspend fun addProductToOrder(orderId: String, productEntry: ProductEntry): String {
        val newOrderItem = OrderItemEntity(
            id = productEntry.id,
            orderId = orderId,
            productTypeLabel = productEntry.productType.label,
            productOwnerName = productEntry.productOwnerName,
            formDataJson = productEntry.serializeFormData(),
            unitPrice = 0L,
            quantity = 1,
            subtotal = 0L
        )

        orderItemDao.insert(newOrderItem)
        return newOrderItem.id
    }

}