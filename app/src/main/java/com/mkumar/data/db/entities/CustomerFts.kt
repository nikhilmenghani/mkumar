package com.mkumar.data.db.entities

import androidx.room.Entity
import androidx.room.Fts4

/**
 * Customer-level FTS index.
 *
 * One row per customer.
 * Used for searching by name / phone.
 *
 * Contentless table: managed by repository code.
 */
@Fts4
@Entity(tableName = "customer_fts")
data class CustomerFts(
    val customerId: String, // logical FK to CustomerEntity.id
    val name: String,       // folded name (lowercase, no diacritics)
    val phone: String?,     // normalized phone digits-only (or null)
    val name3: String?,     // space-separated trigrams of folded name
    val phone3: String?     // space-separated trigrams of phone digits
)
