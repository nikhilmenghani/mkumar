package com.mkumar.repository.impl

import com.mkumar.common.extension.nowUtcMillis
import com.mkumar.data.db.dao.PaymentDao
import com.mkumar.data.db.entities.PaymentDeleteDto
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

            // 1) Insert local
            dao.insertPayment(payment)

            // 2) Enqueue UPSERT
            val payload = json.encodeToString(payment.toSyncDto())

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

            // 1) Load first (required for cloud delete)
            val existing = dao.getPaymentById(id) ?: return@withContext

            // 2) Local delete
            dao.deletePaymentById(id)

            // 3) Cancel any pending UPSERT (consistent with Order + Customer)
            syncRepository.cancelUpsertsFor("PAYMENT_UPSERT", id)

            // 4) Build DELETE DTO
            val deletedAt = nowUtcMillis()
            val dto = PaymentDeleteDto(
                id = existing.id,
                orderId = existing.orderId,
                deletedAt = deletedAt
            )
            val payload = json.encodeToString(dto)

            // 5) Enqueue DELETE
            syncRepository.enqueueOperation(
                type = "PAYMENT_DELETE",
                payloadJson = payload,
                entityId = existing.id,
                cloudPath = "payments/${existing.id}.json",
                priority = 1,
                opUpdatedAt = deletedAt
            )
        }
    }
}
