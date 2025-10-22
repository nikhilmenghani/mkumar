// entities/SearchFts.kt
package com.mkumar.data.local.entities

import androidx.room.Entity
import androidx.room.Fts4

@Fts4 // or @Fts5 if you prefer; both work with Room 2.6.x
@Entity(tableName = "search_fts")
data class SearchFts(
    val customerId: String,  // foreign ref – not a PK
    val name: String,
    val phone: String?,
    val email: String?
)
