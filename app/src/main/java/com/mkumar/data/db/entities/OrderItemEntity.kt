package com.mkumar.data.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Order items — one row per product in an order.
 * We store product typing & the dynamic form as JSON for simplicity.
 *  - unitPrice: price per unit in minor units (if your pricing is fixed per item)
 *  - quantity: integer quantity
 *  - subtotal: unitPrice * quantity in minor units
 */
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

    @ColumnInfo
    val orderId: String,

    /** Frame | Lens | Contact Lens */
    val productTypeLabel: String = "",

    /** Optional owner name */
    val productOwnerName: String = "",

    /** Serialized ProductFormData */
    val formDataJson: String? = null,

    val unitPrice: Int = 0,
    val quantity: Int = 1,
    val discountPercentage: Int = 0,

    /** Derived totals */
    val subtotal: Int = unitPrice * quantity,
    val finalTotal: Int = 0,

    /** Updated time — update this whenever form changes */
    val updatedAt: Long = System.currentTimeMillis()
)
