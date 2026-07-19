package com.mkumar.data.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.mkumar.common.extension.nowUtcMillis
import java.util.UUID

@Entity(
    tableName = "customers",
    indices = [
        Index(value = ["phone"]),
        Index(value = ["createdAt"]),
        Index(value = ["updatedAt"]),
        Index(value = ["hasPendingOrder"]),
        Index(value = ["totalOutstanding"])
    ]
)
data class CustomerEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),

    @ColumnInfo(collate = ColumnInfo.NOCASE)
    val name: String,

    val phone: String,
    val dateOfBirth: Long? = null,
    val email: String? = null,
    val address: String? = null,

    val createdAt: Long = nowUtcMillis(),
    val updatedAt: Long = nowUtcMillis(),

    /** Sum of remainingBalance from all ACTIVE orders */
    val totalOutstanding: Int = 0,

    /** true if any order remainingBalance > 0 */
    val hasPendingOrder: Boolean = false
)
