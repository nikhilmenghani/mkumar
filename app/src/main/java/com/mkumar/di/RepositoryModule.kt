package com.mkumar.di

import com.mkumar.repository.CustomerRepository
import com.mkumar.repository.OrderRepository
import com.mkumar.repository.OrderWriteCoordinator
import com.mkumar.repository.PaymentRepository
import com.mkumar.repository.ProductRepository
import com.mkumar.repository.SearchRepository
import com.mkumar.repository.impl.CustomerRepositoryImpl
import com.mkumar.repository.impl.OrderRepositoryImpl
import com.mkumar.repository.impl.OrderWriteCoordinatorImpl
import com.mkumar.repository.impl.PaymentRepositoryImpl
import com.mkumar.repository.impl.ProductRepositoryImpl
import com.mkumar.repository.impl.SearchRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds @Singleton
    abstract fun bindCustomerRepository(impl: CustomerRepositoryImpl): CustomerRepository

    @Binds @Singleton
    abstract fun bindOrderRepository(impl: OrderRepositoryImpl): OrderRepository

    @Binds @Singleton
    abstract fun bindProductRepository(impl: ProductRepositoryImpl): ProductRepository

    @Binds @Singleton
    abstract fun bindOrderWriteCoordinator(impl: OrderWriteCoordinatorImpl): OrderWriteCoordinator

    @Binds @Singleton
    abstract fun bindSearchRepository(impl: SearchRepositoryImpl): SearchRepository

    @Binds @Singleton
    abstract fun bindPaymentRepository(impl: PaymentRepositoryImpl): PaymentRepository
}
