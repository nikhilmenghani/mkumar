package com.mkumar.data.local.entities

import androidx.room.*
import java.util.UUID

/**
 * Customers table
 */
@Entity(
    tableName = "customers",
    indices = [
        Index(value = ["phone"]),                    // search/useful filter
        Index(value = ["createdAt"]),
        Index(value = ["updatedAt"])
    ]
)
data class CustomerEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),

    @ColumnInfo(collate = ColumnInfo.NOCASE)
    val name: String,

    /**
     * Store normalized phone (e.g., E.164) in UI/mapper layer.
     */
    val phone: String,

    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)


