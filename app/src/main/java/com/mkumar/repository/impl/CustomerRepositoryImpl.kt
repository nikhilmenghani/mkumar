package com.mkumar.repository.impl

import com.mkumar.data.db.dao.CustomerDao
import com.mkumar.data.db.dao.SearchDao
import com.mkumar.data.db.entities.CustomerEntity
import com.mkumar.data.db.entities.SearchFts
import com.mkumar.data.local.relations.CustomerWithOrders
import com.mkumar.repository.CustomerRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CustomerRepositoryImpl @Inject constructor(
    private val customerDao: CustomerDao,
    private val searchDao: SearchDao
) : CustomerRepository {

    override suspend fun upsert(customer: CustomerEntity) {
        customerDao.upsert(customer)
        reindexCustomerForSearch(customer)
    }

    override suspend fun deleteById(customerId: String) {
        customerDao.deleteById(customerId)
        searchDao.deleteByCustomerId(customerId)
    }

    override fun observeAll(): Flow<List<CustomerEntity>> = customerDao.observeAll()

    override suspend fun getAll(): List<CustomerEntity> = customerDao.getAll()

    override fun observeWithOrders(customerId: String): Flow<CustomerWithOrders?> =
        customerDao.observeWithOrders(customerId)

    override suspend fun getWithOrders(customerId: String): CustomerWithOrders? =
        customerDao.getWithOrders(customerId)

    override suspend fun reindexCustomerForSearch(customer: CustomerEntity) {
        val entry = SearchFts(
            customerId = customer.id,
            name = customer.name,
            phone = customer.phone
        )
        searchDao.upsert(entry)
    }
}
