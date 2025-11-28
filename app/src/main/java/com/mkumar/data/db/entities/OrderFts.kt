package com.mkumar.data.db.entities

import androidx.room.Entity
import androidx.room.Fts4

/**
 * Order-level FTS index.
 *
 * One row per order.
 * Used for searching invoice number, product categories, owners, product types, etc.
 *
 * Contentless table: managed by repository code.
 */
@Fts4
@Entity(tableName = "order_fts")
data class OrderFts(
    val customerId: String,     // logical FK to CustomerEntity.id
    val orderId: String,        // logical FK to OrderEntity.id

    // Optional invoice sequence for search; keep as string for flexible matching.
    val invoiceSeq: String?,

    // These can be pre-joined tokens or raw strings; we'll decide when wiring repo.
    val productCategories: String, // e.g. "frames lenses"
    val owners: String,            // e.g. "nikhil wife"
    val productTypes: String,      // e.g. "singlevision bluelight"

    // Main searchable blob (combined & cleaned tokens)
    val content: String
)
