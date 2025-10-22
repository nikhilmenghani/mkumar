package com.mkumar.di

import com.mkumar.data.CustomerRepository
import com.mkumar.data.RoomCustomerRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindCustomerRepository(
        impl: RoomCustomerRepository
    ): CustomerRepository
}
