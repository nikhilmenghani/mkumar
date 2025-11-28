package com.mkumar.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.mkumar.data.db.entities.CustomerFts
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomerFtsDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(entry: CustomerFts)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(entries: List<CustomerFts>)

    @Query("DELETE FROM customer_fts WHERE customerId = :customerId")
    suspend fun deleteByCustomerId(customerId: String)

    @Query("DELETE FROM customer_fts WHERE customerId IN (:ids)")
    suspend fun deleteByCustomerIds(ids: List<String>)

    @Query("DELETE FROM customer_fts")
    suspend fun clear()

    /**
     * Upsert strategy:
     * 1) Try updating (returns count of updated rows)
     * 2) If no rows updated → insert new
     */
    @Transaction
    suspend fun upsert(entry: CustomerFts) {
        val updated = updateCustomerFields(
            entry.customerId,
            entry.name,
            entry.phone,
            entry.name3,
            entry.phone3
        )

        if (updated == 0) {
            insert(entry)
        }
    }

    @Query("""
        UPDATE customer_fts
        SET name = :name,
            phone = :phone,
            name3 = :name3,
            phone3 = :phone3
        WHERE customerId = :customerId
    """)
    suspend fun updateCustomerFields(
        customerId: String,
        name: String,
        phone: String?,
        name3: String?,
        phone3: String?
    ): Int

    // Search results: customerId only
    @Query("""
        SELECT customerId 
        FROM customer_fts
        WHERE customer_fts MATCH :match
        LIMIT :limit
    """)
    suspend fun searchCustomerIds(match: String, limit: Int = 50): List<String>

    // Reactive version
    @Query("""
        SELECT customerId 
        FROM customer_fts
        WHERE customer_fts MATCH :match
        LIMIT :limit
    """)
    fun observeSearchCustomerIds(match: String, limit: Int = 50): Flow<List<String>>
}
