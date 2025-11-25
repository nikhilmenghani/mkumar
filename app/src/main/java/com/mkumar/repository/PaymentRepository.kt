package com.mkumar.repository

import com.mkumar.data.db.entities.PaymentEntity
import kotlinx.coroutines.flow.Flow

interface PaymentRepository {

    suspend fun getPaymentsForOrder(orderId: String): Flow<List<PaymentEntity>>

    suspend fun addPayment(orderId: String, amount: Int, paymentAt: Long)

    suspend fun deletePaymentById(id: String)
}
