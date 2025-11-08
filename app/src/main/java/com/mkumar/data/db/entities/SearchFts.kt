package com.mkumar.data.db.entities

import androidx.room.Entity
import androidx.room.Fts4

@Fts4
@Entity(tableName = "search_fts")
data class SearchFts(
    val customerId: String,
    val name: String,
    val phone: String?
)