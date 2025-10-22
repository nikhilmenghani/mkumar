package com.mkumar.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Orders table — one row per order for a customer.
 * NOTE: No tax per your requirement.
 *  - subtotal: sum(items.subtotal)
 *  - discountAmount: absolute discount applied to subtotal (in minor units)
 *  - grandTotal = subtotal - discountAmount
 */
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
        Index(value = ["occurredAt"])
    ]
)
data class OrderEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),

    @ColumnInfo
    val customerId: String,

    /** When the order occurred (epoch millis) */
    val occurredAt: Long = System.currentTimeMillis(),

    /** Sum of item subtotals (minor units) */
    val subtotal: Long = 0L,

    /** Absolute discount in minor units (0 if none) */
    val discountAmount: Long = 0L,

    /** Computed: subtotal - discountAmount (redundant but handy for queries/UI) */
    val grandTotal: Long = 0L
)