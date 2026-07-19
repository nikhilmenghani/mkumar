package com.mkumar.repository.impl

import com.mkumar.common.extension.nowUtcMillis
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

            // 1) Insert local
            val payment = PaymentEntity(
                id = java.util.UUID.randomUUID().toString(),
                orderId = orderId,
                amountPaid = amount,
                paymentAt = paymentAt
            )
            dao.insertPayment(payment)

            // 2) Update the Order (paidTotal + remainingBalance)
            recomputeOrderTotals(orderId)

        }
    }

    override suspend fun deletePaymentById(id: String) {
        withContext(Dispatchers.IO) {

            val existing = dao.getPaymentById(id) ?: return@withContext

            // 1) Local delete
            dao.deletePaymentById(id)

            // 2) Update Order totals
            recomputeOrderTotals(existing.orderId)

        }
    }

    // -----------------------------------------------------------
    // PRIVATE HELPERS (Order recalculation)
    // -----------------------------------------------------------

    private suspend fun recomputeOrderTotals(orderId: String) {
        val order = orderDao.getById(orderId) ?: return

        val payments = dao.getPaymentsList(orderId)
        val items = orderItemDao.getItemsForOrder(orderId)

        val paidTotal = payments.sumOf { it.amountPaid }
        val remaining = order.totalAmount - paidTotal

        val updatedOrder = order.copy(
            paidTotal = paidTotal,
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
