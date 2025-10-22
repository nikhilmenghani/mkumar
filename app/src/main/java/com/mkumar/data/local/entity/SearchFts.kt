package com.mkumar.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Fts4

/**
 * FTS virtual table for offline search (by name/phone).
 * We link it to customers via contentEntity so updates can be synchronized.
 */
@Fts4(contentEntity = CustomerEntity::class)
@Entity(tableName = "search_fts")
data class SearchFts(
    @ColumnInfo(collate = ColumnInfo.NOCASE)
    val name: String,

    val phone: String
)