package com.mkumar.sync.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.mkumar.data.db.AppDatabase
import com.mkumar.data.db.entities.CustomerDto
import com.mkumar.data.db.entities.CustomerEntity
import com.mkumar.data.db.entities.OrderDto
import com.mkumar.data.db.entities.OrderEntity
import com.mkumar.data.db.entities.OrderItemEntity
import com.mkumar.data.db.entities.PaymentDto
import com.mkumar.data.db.entities.PaymentEntity
import com.mkumar.sync.remote.CloudRemote
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import javax.inject.Inject

class PullFromCloudWorker @Inject constructor(
    context: Context,
    params: WorkerParameters,
    private val cloud: CloudRemote,
    private val db: AppDatabase,
    private val json: Json
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            syncCustomers()
            syncOrders()
            syncPayments()

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }

    // =====================================================================
    // CUSTOMERS
    // =====================================================================

    private suspend fun syncCustomers() {
        val cloudFiles = cloud.list("customers")   // returns file paths
        val localIds = db.customerDao().getAll().map { it.id }.toSet()

        val cloudCustomerIds = cloudFiles
            .filter { it.endsWith("/profile.json") }
            .map { extractCustomerId(it) }
            .toSet()

        // ---------------------------
        // CREATE / UPDATE from cloud
        // ---------------------------
        for (path in cloudFiles) {
            if (!path.endsWith("/profile.json")) continue

            val cloudJson = cloud.get(path) ?: continue
            val dto = json.decodeFromString<CustomerDto>(cloudJson)

            val local = db.customerDao().getById(dto.id)

            if (local == null || dto.updatedAt > local.updatedAt) {
                db.customerDao().upsert(
                    CustomerEntity(
                        id = dto.id,
                        name = dto.name,
                        phone = dto.phone ?: "",
                        createdAt = dto.createdAt,
                        updatedAt = dto.updatedAt,
                        totalOutstanding = dto.totalOutstanding,
                        hasPendingOrder = dto.hasPendingOrder
                    )
                )
            }
        }

        // ---------------------------
        // DELETE (missing in cloud)
        // ---------------------------
        for (localId in localIds) {
            if (localId !in cloudCustomerIds) {
                db.customerDao().deleteById(localId)
                db.customerFtsDao().deleteByCustomerId(localId)
                db.orderFtsDao().deleteByCustomerId(localId)

                // All orders will be automatically removed because of FK cascade,
                // but FTS + items must be cleaned manually.
                val orderIds = db.orderDao().getForCustomer(localId).map { it.id }
                orderIds.forEach { orderId ->
                    db.orderItemDao().deleteItemsForOrder(orderId)
                    db.orderFtsDao().deleteByOrderId(orderId)
                }
            }
        }
    }

    // =====================================================================
    // ORDERS
    // =====================================================================

    private suspend fun syncOrders() {
        val cloudFiles = cloud.list("customers")
        val localOrders = db.orderDao().getAll().map { it.id }.toSet()

        val cloudOrderFiles = cloudFiles.filter { it.contains("/orders/") && it.endsWith(".json") }
        val cloudOrderIds = cloudOrderFiles.map { extractOrderId(it) }.toSet()

        // ---------------------------
        // CREATE / UPDATE
        // ---------------------------
        for (path in cloudOrderFiles) {
            val cloudJson = cloud.get(path) ?: continue

            val dto = json.decodeFromString<OrderDto>(cloudJson)
            val local = db.orderDao().getById(dto.id)

            if (local == null || dto.updatedAt > local.updatedAt) {
                val entity = OrderEntity(
                    id = dto.id,
                    customerId = dto.customerId,
                    invoiceSeq = dto.invoiceSeq,
                    receivedAt = dto.receivedAt,
                    createdAt = dto.createdAt,
                    updatedAt = dto.updatedAt,
                    adjustedAmount = dto.adjustedAmount,
                    totalAmount = dto.totalAmount,
                    remainingBalance = dto.remainingBalance,
                    paidTotal = dto.paidTotal,
                    productCategories = dto.productCategories,
                    owners = dto.owners,
                    orderStatus = dto.orderStatus,
                    deliveryDate = dto.deliveryDate,
                    warrantyMonths = dto.warrantyMonths
                )

                db.orderDao().upsert(entity)

                // Replace items
                db.orderItemDao().deleteItemsForOrder(dto.id)
                for (item in dto.items) {
                    db.orderItemDao().upsert(
                        OrderItemEntity(
                            id = item.id,
                            orderId = item.orderId,
                            productTypeLabel = item.productTypeLabel,
                            productOwnerName = item.productOwnerName,
                            formDataJson = item.formDataJson,
                            unitPrice = item.unitPrice,
                            quantity = item.quantity,
                            discountPercentage = item.discountPercentage,
                            subtotal = item.subtotal,
                            finalTotal = item.finalTotal,
                            updatedAt = item.updatedAt
                        )
                    )
                }
            }
        }

        // ---------------------------
        // DELETE (missing in cloud)
        // ---------------------------
        for (localId in localOrders) {
            if (localId !in cloudOrderIds) {
                db.orderDao().deleteById(localId)
                db.orderItemDao().deleteItemsForOrder(localId)
                db.orderFtsDao().deleteByOrderId(localId)
            }
        }
    }

    // =====================================================================
    // PAYMENTS
    // =====================================================================

    private suspend fun syncPayments() {
        val cloudFiles = cloud.list("payments")
        val localPaymentIds = db.paymentDao().getAllIds().toSet()
        val cloudPaymentIds = cloudFiles.map { extractPaymentId(it) }.toSet()

        // ---------------------------
        // CREATE / UPDATE
        // ---------------------------
        for (path in cloudFiles) {
            val cloudJson = cloud.get(path) ?: continue

            val dto = json.decodeFromString<PaymentDto>(cloudJson)
            val local = db.paymentDao().getPaymentById(dto.id)

            if (local == null || dto.paymentAt > local.paymentAt) {
                db.paymentDao().insertPayment(
                    PaymentEntity(
                        id = dto.id,
                        orderId = dto.orderId,
                        amountPaid = dto.amountPaid,
                        paymentAt = dto.paymentAt
                    )
                )
            }
        }

        // ---------------------------
        // DELETE (missing in cloud)
        // ---------------------------
        for (localId in localPaymentIds) {
            if (localId !in cloudPaymentIds) {
                db.paymentDao().deletePaymentById(localId)
            }
        }
    }

    // =====================================================================
    // HELPERS
    // =====================================================================

    private fun extractCustomerId(path: String): String {
        // customers/<id>/profile.json
        return path.split("/")[1]
    }

    private fun extractOrderId(path: String): String {
        // customers/<custId>/orders/<orderId>.json
        return path.substringAfter("orders/").substringBefore(".json")
    }

    private fun extractPaymentId(path: String): String {
        // payments/<paymentId>.json
        return path.substringAfter("payments/").substringBefore(".json")
    }
}
