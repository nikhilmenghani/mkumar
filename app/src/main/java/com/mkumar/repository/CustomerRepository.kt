package com.mkumar.repository

import com.mkumar.data.CustomerFormState
import com.mkumar.data.db.entities.CustomerEntity
import com.mkumar.data.db.relations.CustomerWithOrders
import com.mkumar.model.OrderWithCustomerInfo
import com.mkumar.repository.impl.SearchMode
import com.mkumar.repository.impl.UiCustomerMini
import kotlinx.coroutines.flow.Flow

interface CustomerRepository {

    suspend fun upsert(customer: CustomerEntity)
    suspend fun deleteById(customerId: String)
    fun observeAll(): Flow<List<CustomerEntity>>
    suspend fun getAll(): List<CustomerEntity>
    fun observeWithOrders(customerId: String): Flow<CustomerWithOrders?>
    suspend fun getWithOrders(customerId: String): CustomerWithOrders?
    fun getRecentCustomers(limit: Int): Flow<List<CustomerFormState>>
    fun getRecentOrders(limit: Int): Flow<List<OrderWithCustomerInfo>>
    suspend fun reindexCustomerForSearch(customer: CustomerEntity)
    suspend fun searchCustomers(
        q: String,
        mode: SearchMode,
        limit: Int = 50
    ): List<UiCustomerMini>
    // --- NEW ---
    suspend fun searchCustomersByInvoice(invoice: String): List<UiCustomerMini>
    suspend fun searchCustomersWithPendingBalance(): List<UiCustomerMini>
    suspend fun searchCustomersAdvanced(
        nameOrPhone: String?,
        invoice: String?,
        remainingOnly: Boolean
    ): List<UiCustomerMini>
}
