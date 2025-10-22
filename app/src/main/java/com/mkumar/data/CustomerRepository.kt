// com/mkumar/data/CustomerRepository.kt
package com.mkumar.data

import com.mkumar.data.local.MKumarDatabase
import com.mkumar.data.local.dao.CustomerDao
import com.mkumar.data.local.entity.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject

interface CustomerRepository {
    fun observeCustomers(): Flow<List<CustomerEntity>>
    suspend fun upsertCustomerWithOrderSnapshot(
        form: CustomerFormState,
        occurredAt: Long = System.currentTimeMillis(),
        discountAmountMinor: Long = 0L
    ): String
}

class RoomCustomerRepository @Inject constructor(
    private val db: MKumarDatabase,
    private val json: Json
) : CustomerRepository {

    override fun observeCustomers(): Flow<List<CustomerEntity>> =
        db.customerDao().observeAllCustomers()

    override suspend fun upsertCustomerWithOrderSnapshot(
        form: CustomerFormState,
        occurredAt: Long,
        discountAmountMinor: Long
    ): String = withContext(Dispatchers.IO) {
        val (customer, order, items) = form.toEntitiesForNewOrder(
            occurredAt = occurredAt,
            discountAmountMinor = discountAmountMinor,
            json = json
        )
        db.customerDao().insertCustomerWithOrder(customer, order, items)
        order.id
    }
}

// --- Mapper (same as earlier) ---
private fun CustomerFormState.toEntitiesForNewOrder(
    occurredAt: Long,
    discountAmountMinor: Long,
    json: Json
): Triple<CustomerEntity, OrderEntity, List<OrderItemEntity>> {
    val customer = CustomerEntity(
        id = id, name = name, phone = phone,
        createdAt = System.currentTimeMillis(), updatedAt = System.currentTimeMillis()
    )

    val items = products.map { p ->
        val formJson = p.formData?.let { json.encodeToString(it) }
        val label = p.productType.label
        val unitPrice = 0L
        val qty = 1
        OrderItemEntity(
            id = p.id,
            orderId = "", // temp, fix after order created
            productTypeLabel = label,
            productOwnerName = p.productOwnerName,
            formDataJson = formJson,
            unitPrice = unitPrice,
            quantity = qty,
            subtotal = unitPrice * qty
        )
    }
    val subtotal = items.sumOf { it.subtotal }
    val orderId = java.util.UUID.randomUUID().toString()
    val order = OrderEntity(
        id = orderId,
        customerId = customer.id,
        occurredAt = occurredAt,
        subtotal = subtotal,
        discountAmount = discountAmountMinor,
        grandTotal = (subtotal - discountAmountMinor).coerceAtLeast(0L)
    )
    val fixedItems = items.map { it.copy(orderId = orderId) }
    return Triple(customer, order, fixedItems)
}
