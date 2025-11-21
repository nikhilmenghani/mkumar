package com.mkumar.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.mkumar.data.db.entities.CustomerEntity
import com.mkumar.data.db.relations.CustomerWithOrders
import com.mkumar.model.UiCustomerMini
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

    @Query("SELECT * FROM customers ORDER BY createdAt DESC LIMIT :limit")
    fun getRecentCustomers(limit: Int): Flow<List<CustomerEntity>>

    @Query("SELECT * FROM customers ORDER BY createdAt DESC LIMIT :limit")
    fun getRecentCustomerList(limit: Int): List<CustomerEntity>


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

    @Query("SELECT id, name, phone FROM customers WHERE id IN (:ids)")
    suspend fun loadMiniByIds(ids: List<String>): List<UiCustomerMini>

    // Optional fallback for very short queries (<3) in FLEXIBLE mode
    @Query(
        """
SELECT id FROM customers
WHERE lower(name) LIKE '%' || lower(:q) || '%'
OR REPLACE(REPLACE(REPLACE(phone,' ',''),'-',''),'(','') LIKE '%' || :digits || '%'
LIMIT :limit
"""
    )
    suspend fun containsCustomerIds(q: String, digits: String, limit: Int): List<String>

    @Query("""
SELECT c.id FROM customers c
JOIN orders o ON o.customerId = c.id
WHERE o.invoiceSeq LIKE '%' || :invoice || '%'
""")
    suspend fun findCustomerIdsByInvoice(invoice: String): List<String>

    @Query("""
SELECT DISTINCT c.id FROM customers c
JOIN orders o ON o.customerId = c.id
WHERE o.remainingBalance > 0
""")
    suspend fun findCustomerIdsWithPendingBalance(): List<String>

    @Query("""
SELECT DISTINCT c.id FROM customers c
JOIN orders o ON o.customerId = c.id
WHERE (:remainingOnly == 0 OR o.remainingBalance > 0)
""")
    suspend fun filterCustomers(remainingOnly: Int = 0): List<String>

}