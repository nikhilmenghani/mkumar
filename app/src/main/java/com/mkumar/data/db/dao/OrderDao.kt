package com.mkumar.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RawQuery
import androidx.room.Upsert
import androidx.sqlite.db.SimpleSQLiteQuery
import com.mkumar.data.db.entities.CustomerEntity
import com.mkumar.data.db.entities.OrderEntity
import com.mkumar.model.OrderWithCustomerInfo
import kotlinx.coroutines.flow.Flow

@Dao
interface OrderDao {

    // --- Writes ---
    @Upsert
    suspend fun upsert(order: OrderEntity)

    @Query("UPDATE orders SET updatedAt = :now WHERE id = :orderId")
    suspend fun touchUpdatedAt(orderId: String, now: Long)

    @Insert(onConflict = OnConflictStrategy.Companion.ABORT)
    suspend fun insert(order: OrderEntity)

    @Insert(onConflict = OnConflictStrategy.Companion.ABORT)
    suspend fun insertAll(orders: List<OrderEntity>)

    @Query("DELETE FROM orders WHERE id = :orderId")
    suspend fun deleteById(orderId: String)

    // --- Reads ---
    @Query("SELECT * FROM orders WHERE id = :orderId LIMIT 1")
    suspend fun getById(orderId: String): OrderEntity?

    @RawQuery(observedEntities = [OrderEntity::class, CustomerEntity::class])
    fun getRecentOrdersWithCustomerRaw(query: SimpleSQLiteQuery): Flow<List<OrderWithCustomerInfo>>

    @Query("""
    SELECT o.id, o.invoiceSeq AS invoiceNumber, o.createdAt, o.totalAmount, o.adjustedAmount, o.remainingBalance, o.customerId, c.name AS customerName, c.phone AS customerPhone
    FROM orders o
    JOIN customers c ON c.id = o.customerId
    ORDER BY o.receivedAt DESC
    LIMIT :limit
""")
    fun getRecentOrdersWithCustomer(limit: Int): Flow<List<OrderWithCustomerInfo>>

    @Query("""
        SELECT * FROM orders 
        WHERE customerId = :customerId 
        ORDER BY receivedAt DESC
    """)
    fun observeForCustomer(customerId: String): Flow<List<OrderEntity>>

    @Query("""
        SELECT * FROM orders 
        WHERE customerId = :customerId 
        ORDER BY receivedAt DESC
    """)
    suspend fun getForCustomer(customerId: String): List<OrderEntity>

    @Query("""
        SELECT * FROM orders 
        WHERE customerId = :customerId 
        ORDER BY receivedAt DESC 
        LIMIT 1
    """)
    suspend fun getLatestForCustomer(customerId: String): OrderEntity?

    // Optional: date range queries (handy for reports)
    @Query("""
        SELECT * FROM orders 
        WHERE receivedAt BETWEEN :from AND :to
        ORDER BY receivedAt DESC
    """)
    suspend fun getBetween(from: Long, to: Long): List<OrderEntity>

    @Query("SELECT * FROM orders WHERE customerId = :customerId ORDER BY receivedAt DESC")
    fun observeOrdersForCustomer(customerId: String): Flow<List<OrderEntity>>
    @Query("SELECT * FROM orders WHERE id = :orderId")
    fun observeOrder(orderId: String): Flow<OrderEntity?>

    @Query("""
SELECT * FROM orders
WHERE invoiceSeq LIKE '%' || :invoice || '%'
ORDER BY receivedAt DESC
""")
    suspend fun searchOrdersByInvoice(invoice: String): List<OrderEntity>

    @Query("""
SELECT * FROM orders
WHERE customerId = :customerId
AND remainingBalance > 0
ORDER BY receivedAt DESC
""")

    suspend fun getPendingOrders(customerId: String): List<OrderEntity>
    @Query("""
SELECT * FROM orders
WHERE customerId = :customerId
AND (:category IS NULL OR :category IN (productCategories))
ORDER BY receivedAt DESC
""")
    suspend fun getOrdersByCategory(customerId: String, category: String?): List<OrderEntity>

    @Query("""
SELECT * FROM orders
WHERE customerId = :customerId
AND (:owner IS NULL OR :owner IN (owners))
ORDER BY receivedAt DESC
""")
    suspend fun getOrdersByOwner(customerId: String, owner: String?): List<OrderEntity>

    @Query("""
SELECT * FROM orders
WHERE customerId = :customerId
AND (:remainingOnly = 0 OR remainingBalance > 0)
AND (:category IS NULL OR :category IN (productCategories))
AND (:owner IS NULL OR :owner IN (owners))
ORDER BY receivedAt DESC
""")
    suspend fun filterOrders(
        customerId: String,
        remainingOnly: Int,
        category: String?,
        owner: String?
    ): List<OrderEntity>

}