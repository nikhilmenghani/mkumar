package com.mkumar.repository.impl

import com.mkumar.data.db.dao.PaymentDao
import com.mkumar.data.db.entities.PaymentEntity
import com.mkumar.repository.PaymentRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class PaymentRepositoryImpl @Inject constructor(
    private val dao: PaymentDao
) : PaymentRepository {

    override suspend fun getPaymentsForOrder(orderId: String): Flow<List<PaymentEntity>> =
        dao.getPayments(orderId)

    override suspend fun addPayment(orderId: String, amount: Int, paymentAt: Long) {
        withContext(Dispatchers.IO) {
            dao.insertPayment(
                PaymentEntity(
                    id = UUID.randomUUID().toString(),
                    orderId = orderId,
                    amountPaid = amount,
                    paymentAt = paymentAt
                )
            )
        }
    }

    override suspend fun deletePaymentById(id: String) {
        withContext(Dispatchers.IO) {
            dao.deletePaymentById(id)
        }
    }
}
