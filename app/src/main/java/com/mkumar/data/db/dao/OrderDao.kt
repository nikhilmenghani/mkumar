package com.mkumar.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import com.mkumar.data.db.entities.OrderEntity
import kotlinx.coroutines.flow.Flow
import java.time.Instant

@Dao
interface OrderDao {

    // --- Writes ---
    @Upsert
    suspend fun upsert(order: OrderEntity)

    @Insert(onConflict = OnConflictStrategy.Companion.ABORT)
    suspend fun insert(order: OrderEntity)

    @Insert(onConflict = OnConflictStrategy.Companion.ABORT)
    suspend fun insertAll(orders: List<OrderEntity>)

    @Query("DELETE FROM orders WHERE id = :orderId")
    suspend fun deleteById(orderId: String)

    // --- Reads ---
    @Query("SELECT * FROM orders WHERE id = :orderId LIMIT 1")
    suspend fun getById(orderId: String): OrderEntity?

    @Query("""
        SELECT * FROM orders 
        WHERE customerId = :customerId 
        ORDER BY occurredAt DESC
    """)
    fun observeForCustomer(customerId: String): Flow<List<OrderEntity>>

    @Query("""
        SELECT * FROM orders 
        WHERE customerId = :customerId 
        ORDER BY occurredAt DESC
    """)
    suspend fun getForCustomer(customerId: String): List<OrderEntity>

    @Query("""
        SELECT * FROM orders 
        WHERE customerId = :customerId 
        ORDER BY occurredAt DESC 
        LIMIT 1
    """)
    suspend fun getLatestForCustomer(customerId: String): OrderEntity?

    // Optional: date range queries (handy for reports)
    @Query("""
        SELECT * FROM orders 
        WHERE occurredAt BETWEEN :from AND :to
        ORDER BY occurredAt DESC
    """)
    suspend fun getBetween(from: Instant, to: Instant): List<OrderEntity>

    @Query("SELECT * FROM orders WHERE customerId = :customerId ORDER BY occurredAt DESC")
    fun observeOrdersForCustomer(customerId: String): Flow<List<OrderEntity>>
    @Query("SELECT * FROM orders WHERE id = :orderId")
    fun observeOrder(orderId: String): Flow<OrderEntity?>

}