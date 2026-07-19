package com.mkumar.repository.impl

import androidx.room.withTransaction
import com.mkumar.common.extension.nowUtcMillis
import com.mkumar.data.db.AppDatabase
import com.mkumar.data.db.dao.CustomerDao
import com.mkumar.data.db.dao.OrderDao
import com.mkumar.data.db.dao.OrderFtsDao
import com.mkumar.data.db.dao.OrderItemDao
import com.mkumar.data.db.dao.PaymentDao
import com.mkumar.data.db.entities.PaymentEntity
import com.mkumar.repository.PaymentRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PaymentRepositoryImpl @Inject constructor(
    private val db: AppDatabase,
    private val dao: PaymentDao,
    private val orderDao: OrderDao,
    private val orderItemDao: OrderItemDao,
    private val customerDao: CustomerDao,
    private val orderFtsDao: OrderFtsDao
) : PaymentRepository {

    override suspend fun getPaymentsForOrder(orderId: String): Flow<List<PaymentEntity>> =
        dao.getPayments(orderId)

    override suspend fun addPayment(orderId: String, amount: Int, paymentAt: Long) {
        withContext(Dispatchers.IO) {
            db.withTransaction {
                require(amount > 0) { "Payment amount must be greater than zero" }

                val payment = PaymentEntity(
                    id = java.util.UUID.randomUUID().toString(),
                    orderId = orderId,
                    amountPaid = amount,
                    paymentAt = paymentAt
                )
                dao.insertPayment(payment)

                recomputeOrderTotals(orderId)
            }

        }
    }

    override suspend fun deletePaymentById(id: String) {
        withContext(Dispatchers.IO) {
            db.withTransaction {

                val existing = dao.getPaymentById(id) ?: return@withTransaction

                dao.deletePaymentById(id)

                recomputeOrderTotals(existing.orderId)
            }

        }
    }

    // -----------------------------------------------------------
    // PRIVATE HELPERS (Order recalculation)
    // -----------------------------------------------------------

    private suspend fun recomputeOrderTotals(orderId: String) {
        val order = orderDao.getById(orderId) ?: return

        val payments = dao.getPaymentsList(orderId)
        val items = orderItemDao.getItemsForOrder(orderId)

        val totalAmount = items.sumOf { item ->
            val subtotal = item.unitPrice.coerceAtLeast(0) * item.quantity.coerceAtLeast(0)
            val discount = subtotal * item.discountPercentage.coerceIn(0, 100) / 100
            (subtotal - discount).coerceAtLeast(0)
        }
        val paidTotal = payments.sumOf { it.amountPaid }
        val payableTotal = order.adjustedAmount.takeIf { it > 0 } ?: totalAmount
        val remaining = payableTotal - paidTotal

        val updatedOrder = order.copy(
            paidTotal = paidTotal,
            totalAmount = totalAmount,
            remainingBalance = remaining,
            updatedAt = nowUtcMillis()
        )

        // 1) Update order locally
        orderDao.upsert(updatedOrder)

        // 2) Update customer summary
        updateCustomerSummary(updatedOrder.customerId)

        // 3) Update FTS
        reindexOrderFts(updatedOrder, items)

    }

    private suspend fun updateCustomerSummary(customerId: String) {
        val orders = orderDao.getForCustomer(customerId)
        val outstanding = orders.sumOf { it.remainingBalance }
        val hasPending = orders.any { it.remainingBalance > 0 }

        val customer = customerDao.getById(customerId) ?: return

        customerDao.upsert(
            customer.copy(
                totalOutstanding = outstanding,
                hasPendingOrder = hasPending,
                updatedAt = nowUtcMillis()
            )
        )
    }

    private suspend fun reindexOrderFts(order: com.mkumar.data.db.entities.OrderEntity, items: List<com.mkumar.data.db.entities.OrderItemEntity>) {
        val tokens = buildList {
            add(order.invoiceSeq?.toString().orEmpty())
            addAll(order.productCategories)
            addAll(order.owners)
            addAll(items.map { it.productTypeLabel })
        }

        val cleaned = tokens.map { it.trim() }
            .filter { it.isNotEmpty() }
            .distinct()
            .sorted()

        val content = cleaned.joinToString(" ")

        orderFtsDao.upsert(
            com.mkumar.data.db.entities.OrderFts(
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
