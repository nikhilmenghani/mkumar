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
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(entry: SearchFts)


    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(entries: List<SearchFts>)


    @Query("DELETE FROM search_fts WHERE customerId = :customerId")
    suspend fun deleteByCustomerId(customerId: String)


    @Query("DELETE FROM search_fts WHERE customerId IN (:customerIds)")
    suspend fun deleteByCustomerIds(customerIds: List<String>)


    @Query("DELETE FROM search_fts")
    suspend fun clear()


    @Transaction
    suspend fun upsert(entry: SearchFts) {
        deleteByCustomerId(entry.customerId)
        insert(entry)
    }

    @Transaction
    suspend fun upsertCustomerFields(
        customerId: String,
        name: String,
        phone: String?,
        name3: String?,
        phone3: String?
    ) {
        val updated = updateCustomerFields(customerId, name, phone, name3, phone3)
        if (updated == 0) {
            insert(
                SearchFts(
                    customerId = customerId,
                    name = name,
                    phone = phone,
                    name3 = name3,
                    phone3 = phone3,
                    content = "",
                    orderId = null
                )
            )
        }
    }

    @Transaction
    suspend fun upsertAll(entries: List<SearchFts>) {
        if (entries.isEmpty()) return
        deleteByCustomerIds(entries.map { it.customerId }.distinct())
        insertAll(entries)
    }


    // Generic MATCH query
    @Query(
        """
SELECT customerId FROM search_fts
WHERE search_fts MATCH :match
LIMIT :limit
"""
    )
    suspend fun searchCustomerIds(match: String, limit: Int = 50): List<String>


    @Query(
        """
SELECT customerId FROM search_fts
WHERE search_fts MATCH :match
LIMIT :limit
"""
    )
    fun observeSearchCustomerIds(match: String, limit: Int = 50): Flow<List<String>>

    @Query("""
SELECT orderId FROM search_fts
WHERE search_fts MATCH :match
LIMIT :limit
""")
    suspend fun searchOrderIds(match: String, limit: Int = 50): List<String>

    @Query("""
    UPDATE search_fts
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

    @Query("""
    UPDATE search_fts
    SET content = :content
    WHERE orderId = :orderId
""")
    suspend fun updateOrderContent(orderId: String, content: String)

}