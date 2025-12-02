package com.mkumar.data.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.mkumar.common.extension.nowUtcMillis
import com.mkumar.model.OrderStatus
import kotlinx.serialization.Serializable
import java.util.UUID

@Entity(
    tableName = "orders",
    foreignKeys = [
        ForeignKey(
            entity = CustomerEntity::class,
            parentColumns = ["id"],
            childColumns = ["customerId"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["customerId"]),
        Index(value = ["receivedAt"]),
        Index(value = ["remainingBalance"]),
        Index(value = ["invoiceSeq"]),
        Index(value = ["orderStatus"]),
        Index(value = ["customerId", "orderStatus"]),
        Index(value = ["customerId", "remainingBalance"]),
        Index(value = ["productCategories"]),
        Index(value = ["owners"])
    ]
)
data class OrderEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),

    val customerId: String,

    val receivedAt: Long = nowUtcMillis(),
    val createdAt: Long = nowUtcMillis(),

    /** Single numeric invoice identity */
    @ColumnInfo(name = "invoiceSeq")
    val invoiceSeq: Long? = null,

    /** Pricing data */
    val adjustedAmount: Int = 0,
    val totalAmount: Int = 0,
    val remainingBalance: Int = 0,
    val paidTotal: Int = 0,

    /**
     * For searching/filtering:
     * - “orders containing frame”
     * - “orders containing lens”
     */
    val productCategories: List<String> = emptyList(),

    /**
     * For searching by owner:
     * - “owner A's orders”
     */
    val owners: List<String> = emptyList(),

    val updatedAt: Long = nowUtcMillis(),

    /** String-stored enum: DRAFT / ACTIVE / COMPLETED */
    val orderStatus: String = OrderStatus.DRAFT.value,

    // Future fields
    val deliveryDate: Long? = nowUtcMillis(),
    val warrantyMonths: Int? = 12
)

@Serializable
data class OrderDto(
    val id: String,
    val customerId: String,
    val invoiceSeq: Long?,
    val receivedAt: Long,
    val createdAt: Long,
    val updatedAt: Long,
    val adjustedAmount: Int,
    val totalAmount: Int,
    val remainingBalance: Int,
    val paidTotal: Int,
    val productCategories: List<String>,
    val owners: List<String>,
    val orderStatus: String,
    val deliveryDate: Long?,
    val warrantyMonths: Int?,
    val items: List<OrderItemDto>
)

@Serializable
data class OrderDeleteDto(
    val id: String,
    val customerId: String,
    val deletedAt: Long
)

fun OrderEntity.toSyncDto(
    items: List<OrderItemEntity>
): OrderDto {
    return OrderDto(
        id = id,
        customerId = customerId,
        invoiceSeq = invoiceSeq,
        receivedAt = receivedAt,
        createdAt = createdAt,
        updatedAt = updatedAt,
        adjustedAmount = adjustedAmount,
        totalAmount = totalAmount,
        remainingBalance = remainingBalance,
        paidTotal = paidTotal,
        productCategories = productCategories,
        owners = owners,
        orderStatus = orderStatus,
        deliveryDate = deliveryDate,
        warrantyMonths = warrantyMonths,
        items = items.map { it.toSyncDto() }
    )
}
