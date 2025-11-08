package com.mkumar.repository

import com.mkumar.data.db.entities.CustomerEntity
import com.mkumar.data.db.relations.CustomerWithOrders
import com.mkumar.repository.impl.UiCustomerMini
import kotlinx.coroutines.flow.Flow

interface CustomerRepository {
    suspend fun upsert(customer: CustomerEntity)
    suspend fun deleteById(customerId: String)
    fun observeAll(): Flow<List<CustomerEntity>>
    suspend fun getAll(): List<CustomerEntity>
    fun observeWithOrders(customerId: String): Flow<CustomerWithOrders?>
    suspend fun getWithOrders(customerId: String): CustomerWithOrders?
    suspend fun reindexCustomerForSearch(customer: CustomerEntity)
    suspend fun searchCustomers(q: String, limit: Int = 50): List<UiCustomerMini>
}
