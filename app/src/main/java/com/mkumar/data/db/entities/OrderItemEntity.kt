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
            onDelete = ForeignKey.Companion.CASCADE,
            onUpdate = ForeignKey.Companion.CASCADE
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
    val unitPrice: Int = 0,

    /** Whole number of units */
    val quantity: Int = 1,
    /** Discount percentage (0.0 - 100.0) */
    val discountPercentage: Int = 0,

    /** unitPrice * quantity in minor units */
    val subtotal: Int = unitPrice * quantity,
    /** Final total after discount applied in minor units */
    val finalTotal: Int = 0
)