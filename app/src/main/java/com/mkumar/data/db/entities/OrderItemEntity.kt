package com.mkumar.data.db.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.mkumar.common.extension.nowUtcMillis
import kotlinx.serialization.Serializable
import java.util.UUID

@Entity(
    tableName = "order_items",
    foreignKeys = [
        ForeignKey(
            entity = OrderEntity::class,
            parentColumns = ["id"],
            childColumns = ["orderId"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["orderId"])
    ]
)
data class OrderItemEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val orderId: String,
    val productTypeLabel: String,
    val productOwnerName: String,
    val formDataJson: String? = null,
    val unitPrice: Int = 0,
    val quantity: Int = 1,
    val discountPercentage: Int = 0,
    val subtotal: Int = unitPrice * quantity,
    val finalTotal: Int = 0,
    val updatedAt: Long = nowUtcMillis()
)

@Serializable
data class OrderItemDto(
    val id: String,
    val orderId: String,
    val productTypeLabel: String,
    val productOwnerName: String,
    val formDataJson: String? = null,
    val unitPrice: Int = 0,
    val quantity: Int = 1,
    val discountPercentage: Int = 0,
    val subtotal: Int = unitPrice * quantity,
    val finalTotal: Int = 0,
    val updatedAt: Long = nowUtcMillis()
)


fun OrderItemEntity.toSyncDto(): OrderItemDto {
    return OrderItemDto(
        id = id,
        orderId = orderId,
        quantity = quantity,
        unitPrice = unitPrice,
        discountPercentage = discountPercentage,
        productTypeLabel = productTypeLabel,
        productOwnerName = productOwnerName,
        formDataJson = formDataJson,
        finalTotal = finalTotal,
        updatedAt = updatedAt
    )
}
