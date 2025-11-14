package com.mkumar.data.db.entities

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

    /** For recalculating summary values */
    val adjustedAmount: Int = 0,
    val totalAmount: Int = 0,
    val remainingBalance: Int = 0,
    val advanceTotal: Int = 0,

    /** Invoice sequence number, if any */
    @ColumnInfo(name = "invoice_seq")
    val invoiceSeq: Long? = null,

    /** Updated time — always overwrite when updating */
    val updatedAt: Long = System.currentTimeMillis()
)
