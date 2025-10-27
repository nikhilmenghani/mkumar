package com.mkumar.data.repository

import com.mkumar.data.OrderSummaryDomain
import com.mkumar.data.ProductEntry
import com.mkumar.data.ProductType
import com.mkumar.data.local.MKumarDatabase
import com.mkumar.data.local.dao.CustomerDao
import com.mkumar.data.local.dao.OrderDao
import com.mkumar.data.local.dao.OrderItemDao
import com.mkumar.data.local.entities.OrderEntity
import com.mkumar.data.local.entities.OrderItemEntity
import kotlinx.coroutines.flow.first
import java.time.Clock
import java.time.Instant
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
            val orderItems = orderItemDao.getItemsForOrder(entity.id)
            val productsOfOrder = mutableListOf<ProductEntry>()
            for (product in orderItems) {
                val entry = ProductEntry(
                    id = product.id,
                    productType = ProductType.fromLabel(product.productTypeLabel),
                    productOwnerName = product.productOwnerName,
                    formData = ProductEntry.deserializeFormData(product.formDataJson),
                    unitPrice = product.unitPrice,
                    quantity = product.quantity,
                    discountPercentage = product.discountPercentage,
                    finalTotal = product.subtotal,
                )
                productsOfOrder.add(entry)
            }
            OrderSummaryDomain(
                id = entity.id,
                occurredAt = Instant.ofEpochMilli(entity.occurredAt),
                isDraft = true,
                subtitle = "",
                advanceTotal = entity.advanceTotal,
                remainingBalance = entity.remainingBalance,
                totalAmount = entity.totalAmount,
                adjustedAmount = entity.adjustedAmount,
                products = productsOfOrder
            )
        }
    }

    suspend fun createDraftOrder(customerId: String, orderId: String): String {
        val newOrder = OrderEntity(
            id = orderId,
            customerId = customerId,
        )
        orderDao.upsert(newOrder)
        return newOrder.id
    }

    suspend fun addProductToOrder(orderId: String, productEntry: ProductEntry): String {
        val newOrderItem = OrderItemEntity(
            id = productEntry.id,
            orderId = orderId,
            productTypeLabel = productEntry.productType.label,
            productOwnerName = productEntry.productOwnerName,
            formDataJson = productEntry.serializeFormData(),
            unitPrice = productEntry.formData?.unitPrice ?: 0,
            quantity = productEntry.formData?.quantity ?: 1,
            subtotal = productEntry.formData?.total ?: 0,
            discountPercentage = productEntry.formData?.discountPct ?: 0,
        )

        orderItemDao.upsert(newOrderItem)
        return newOrderItem.id
    }

    suspend fun deleteOrder(orderId: String) {
        orderDao.deleteById(orderId)
        orderItemDao.deleteByOrderId(orderId)
    }

    suspend fun deleteOrderItem(orderItemId: String) {
        orderItemDao.deleteProductById(orderItemId)
    }

}