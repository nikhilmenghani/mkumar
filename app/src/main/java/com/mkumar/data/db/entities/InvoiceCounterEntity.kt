// com/mkumar/data/db/entities/InvoiceCounterEntity.kt
package com.mkumar.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "invoice_counter")
data class InvoiceCounterEntity(
    @PrimaryKey val id: Int = 1,
    val lastNumber: Long
)
