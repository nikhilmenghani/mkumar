package com.mkumar.repository.impl

import com.mkumar.data.db.dao.PaymentDao
import com.mkumar.data.db.entities.PaymentEntity
import com.mkumar.data.db.entities.toSyncDto
import com.mkumar.repository.PaymentRepository
import com.mkumar.repository.SyncRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PaymentRepositoryImpl @Inject constructor(
    private val dao: PaymentDao,
    private val syncRepository: SyncRepository,
    private val json: Json
) : PaymentRepository {

    override suspend fun getPaymentsForOrder(orderId: String): Flow<List<PaymentEntity>> =
        dao.getPayments(orderId)

    override suspend fun addPayment(orderId: String, amount: Int, paymentAt: Long) {
        withContext(Dispatchers.IO) {
            val payment = PaymentEntity(
                id = UUID.randomUUID().toString(),
                orderId = orderId,
                amountPaid = amount,
                paymentAt = paymentAt
            )

            // 1) Insert into local Room DB
            dao.insertPayment(payment)

            // 2) Enqueue "PAYMENT_UPSERT" sync op
            val dto = payment.toSyncDto()
            val payload = json.encodeToString(dto)

            syncRepository.enqueueOperation(
                type = "PAYMENT_UPSERT",
                payloadJson = payload,
                entityId = payment.id,
                cloudPath = "payments/${payment.id}.json",
                priority = 1,
                opUpdatedAt = payment.paymentAt
            )
        }
    }

    override suspend fun deletePaymentById(id: String) {
        withContext(Dispatchers.IO) {

            // 1) Get the payment to know which order it belonged to
            val existing = dao.getPaymentById(id)
            dao.deletePaymentById(id)

            // 2) Enqueue delete event only if payment existed
            // This ensures sync can delete from cloud
            if (existing != null) {
                val deletePayload = json.encodeToString(
                    mapOf(
                        "id" to existing.id,
                        "orderId" to existing.orderId,
                        "deletedAt" to System.currentTimeMillis()
                    )
                )

                syncRepository.enqueueOperation(
                    type = "PAYMENT_DELETE",
                    payloadJson = deletePayload,
                    entityId = existing.id,
                    cloudPath = "payments/${existing.id}.json",
                    priority = 1,
                    opUpdatedAt = System.currentTimeMillis()
                )
            }
        }
    }
}
