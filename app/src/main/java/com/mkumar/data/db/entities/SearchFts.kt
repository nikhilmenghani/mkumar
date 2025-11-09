package com.mkumar.data.db.entities

import androidx.room.Entity
import androidx.room.Fts4


// FTS4 contentless table managed by repo code.
@Fts4
@Entity(tableName = "search_fts")
data class SearchFts(
    val customerId: String, // foreign key reference (logical)
    val name: String, // folded name (lowercased, no diacritics)
    val phone: String?, // digits-only phone
    val name3: String?, // space-separated trigrams of folded name (no spaces)
    val phone3: String? // space-separated trigrams of phone digits
)