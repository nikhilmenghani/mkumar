package com.mkumar.data.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.mkumar.model.OrderStatus
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
        Index(value = ["occurredAt"]),
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

    val occurredAt: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis(),

    /** Single numeric invoice identity */
    @ColumnInfo(name = "invoiceSeq")
    val invoiceSeq: Long? = null,

    /** Pricing data */
    val adjustedAmount: Int = 0,
    val totalAmount: Int = 0,
    val remainingBalance: Int = 0,
    val advanceTotal: Int = 0,

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

    val updatedAt: Long = System.currentTimeMillis(),

    /** String-stored enum: DRAFT / ACTIVE / COMPLETED */
    val orderStatus: String = OrderStatus.DRAFT.value,

    // Future fields
    val deliveryDate: Long? = System.currentTimeMillis(),
    val warrantyMonths: Int? = 12
)
