package com.mkumar.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.mkumar.data.db.entities.OrderFts
import kotlinx.coroutines.flow.Flow

@Dao
interface OrderFtsDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(entry: OrderFts)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(entries: List<OrderFts>)

    @Query("DELETE FROM order_fts WHERE orderId = :orderId")
    suspend fun deleteByOrderId(orderId: String)

    @Query("DELETE FROM order_fts WHERE customerId = :customerId")
    suspend fun deleteByCustomerId(customerId: String)

    @Query("DELETE FROM order_fts WHERE orderId IN (:ids)")
    suspend fun deleteByOrderIds(ids: List<String>)

    @Query("DELETE FROM order_fts")
    suspend fun clear()

    @Transaction
    suspend fun upsert(entry: OrderFts) {
        // delete previous copy of this order
        deleteByOrderId(entry.orderId)
        insert(entry)
    }

    @Query("""
        UPDATE order_fts
        SET 
            invoiceSeq = :invoiceSeq,
            productCategories = :productCategories,
            owners = :owners,
            productTypes = :productTypes,
            content = :content
        WHERE orderId = :orderId
    """)
    suspend fun updateOrderFields(
        orderId: String,
        invoiceSeq: String?,
        productCategories: String,
        owners: String,
        productTypes: String,
        content: String
    ): Int

    // Search orderIds
    @Query("""
        SELECT orderId
        FROM order_fts
        WHERE order_fts MATCH :match
        LIMIT :limit
    """)
    suspend fun searchOrderIds(match: String, limit: Int = 50): List<String>

    // Reactive version
    @Query("""
        SELECT orderId
        FROM order_fts
        WHERE order_fts MATCH :match
        LIMIT :limit
    """)
    fun observeSearchOrderIds(match: String, limit: Int = 50): Flow<List<String>>
}
