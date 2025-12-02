package com.mkumar.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.mkumar.common.extension.nowUtcMillis
import kotlinx.serialization.Serializable

@Entity(tableName = "payments")
data class PaymentEntity(
    @PrimaryKey val id: String,
    val orderId: String,
    val amountPaid: Int,
    val paymentAt: Long = nowUtcMillis()
)

@Serializable
data class PaymentDto(
    val id: String,
    val orderId: String,
    val amountPaid: Int,
    val paymentAt: Long
)

fun PaymentEntity.toSyncDto(): PaymentDto {
    return PaymentDto(
        id = id,
        orderId = orderId,
        amountPaid = amountPaid,
        paymentAt = paymentAt
    )
}
