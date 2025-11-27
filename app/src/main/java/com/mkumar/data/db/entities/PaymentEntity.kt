package com.mkumar.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.mkumar.common.extension.nowUtcMillis

@Entity(tableName = "payments")
data class PaymentEntity(
    @PrimaryKey val id: String,
    val orderId: String,
    val amountPaid: Int,
    val paymentAt: Long = nowUtcMillis()
)
