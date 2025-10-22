// app/src/main/java/com/mkumar/data/local/dao/CustomerDao.kt
package com.mkumar.data.local.dao

import androidx.room.*
import com.mkumar.data.local.entity.CustomerEntity
import com.mkumar.data.local.entity.OrderEntity
import com.mkumar.data.local.entity.OrderItemEntity
import com.mkumar.data.local.entity.SearchFts
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomerDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCustomer(customer: CustomerEntity)

    @Query("SELECT * FROM customers WHERE id = :customerId LIMIT 1")
    suspend fun getCustomer(customerId: String): CustomerEntity?

    @Query("SELECT * FROM customers ORDER BY updatedAt DESC")
    fun observeAllCustomers(): Flow<List<CustomerEntity>>

    @Query("SELECT * FROM customers ORDER BY updatedAt DESC")
    suspend fun getAllCustomers(): List<CustomerEntity>

    /**
     * Helper to maintain FTS row for a customer.
     * If you use FTS4(content=..) triggers you may not need this, but Room doesn't auto-create triggers.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSearchFts(row: SearchFts)

    /**
     * Inserts a customer + order + items in a single transaction,
     * and updates the FTS table for offline search.
     */
    @Transaction
    suspend fun insertCustomerWithOrder(
        customer: CustomerEntity,
        order: OrderEntity,
        items: List<OrderItemEntity>
    ) {
        upsertCustomer(customer)
        upsertSearchFts(SearchFts(name = customer.name, phone = customer.phone))
        insertOrder(order)
        insertOrderItems(items)
    }

    // Delegated to OrderDao methods but exposed here for transactional convenience:
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertOrder(order: OrderEntity)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertOrderItems(items: List<OrderItemEntity>)
}
