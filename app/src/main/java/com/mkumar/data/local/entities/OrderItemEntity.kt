package com.mkumar.data.local.entities

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

    /** Frame | Lens | Contact Lens (from ProductType.label) */
    val productTypeLabel: String = "",

    /** Owner name captured in your UI (can be empty) */
    val productOwnerName: String = "",

    /**
     * Serialized ProductFormData (kotlinx serialization) as JSON string.
     * You can add a TypeConverter later if you want a strong type here.
     */
    val formDataJson: String? = null,

    /** Per-unit price in minor units (optional if you derive only from form data) */
    val unitPrice: Long = 0L,

    /** Whole number of units */
    val quantity: Int = 1,

    /** unitPrice * quantity in minor units */
    val subtotal: Long = unitPrice * quantity
)