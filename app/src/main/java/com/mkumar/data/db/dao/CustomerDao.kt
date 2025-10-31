package com.mkumar.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.mkumar.data.db.entities.CustomerEntity
import com.mkumar.data.local.relations.CustomerWithOrders
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomerDao {

    // --- Writes ---
    @Upsert
    suspend fun upsert(customer: CustomerEntity)

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insertAll(customers: List<CustomerEntity>)

    @Query("DELETE FROM customers WHERE id = :customerId")
    suspend fun deleteById(customerId: String)

    // --- Reads ---
    @Query("SELECT * FROM customers ORDER BY updatedAt DESC")
    fun observeAll(): Flow<List<CustomerEntity>>

    @Query("SELECT * FROM customers ORDER BY updatedAt DESC")
    suspend fun getAll(): List<CustomerEntity>

    @Query("SELECT * FROM customers WHERE id = :customerId LIMIT 1")
    suspend fun getById(customerId: String): CustomerEntity?

    // Customer + Orders relation stream (used by repository)
    @Transaction
    @Query("SELECT * FROM customers WHERE id = :customerId LIMIT 1")
    fun observeWithOrders(customerId: String): Flow<CustomerWithOrders?>

    @Transaction
    @Query("SELECT * FROM customers WHERE id = :customerId LIMIT 1")
    suspend fun getWithOrders(customerId: String): CustomerWithOrders?

}