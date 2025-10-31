package com.mkumar.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.mkumar.data.db.entities.SearchFts
import kotlinx.coroutines.flow.Flow

@Dao
interface SearchDao {

    // Inserts (no REPLACE needed for FTS when we control deletes)
    @Insert(onConflict = OnConflictStrategy.Companion.IGNORE)
    suspend fun insert(entry: SearchFts)

    @Insert(onConflict = OnConflictStrategy.Companion.IGNORE)
    suspend fun insertAll(entries: List<SearchFts>)

    // Deletes
    @Query("DELETE FROM search_fts WHERE customerId = :customerId")
    suspend fun deleteByCustomerId(customerId: String)

    @Query("DELETE FROM search_fts WHERE customerId IN (:customerIds)")
    suspend fun deleteByCustomerIds(customerIds: List<String>)

    @Query("DELETE FROM search_fts")
    suspend fun clear()

    // "Upsert" semantics for FTS: delete then insert
    @Transaction
    suspend fun upsert(entry: SearchFts) {
        deleteByCustomerId(entry.customerId)
        insert(entry)
    }

    @Transaction
    suspend fun upsertAll(entries: List<SearchFts>) {
        if (entries.isEmpty()) return
        deleteByCustomerIds(entries.map { it.customerId }.distinct())
        insertAll(entries)
    }

    // Search
    @Query("""
        SELECT customerId
        FROM search_fts
        WHERE search_fts MATCH :match
        LIMIT :limit
    """)
    suspend fun searchCustomerIds(match: String, limit: Int = 50): List<String>

    @Query("""
        SELECT customerId
        FROM search_fts
        WHERE search_fts MATCH :match
        LIMIT :limit
    """)
    fun observeSearchCustomerIds(match: String, limit: Int = 50): Flow<List<String>>
}