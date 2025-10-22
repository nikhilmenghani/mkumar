package com.mkumar.di

import android.content.Context
import androidx.room.Room
import com.mkumar.data.local.MKumarDatabase
import com.mkumar.data.local.dao.CustomerDao
import com.mkumar.data.local.dao.OrderDao
import com.mkumar.data.local.dao.OrderItemDao
import com.mkumar.data.repository.CustomerRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.time.Clock
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides @Singleton
    fun provideDatabase(@ApplicationContext context: Context): MKumarDatabase =
        Room.databaseBuilder(context, MKumarDatabase::class.java, "mkumar.db").build()

    @Provides fun provideCustomerDao(db: MKumarDatabase): CustomerDao = db.customerDao()
    @Provides fun provideOrderDao(db: MKumarDatabase): OrderDao = db.orderDao()
    @Provides fun provideOrderItemDao(db: MKumarDatabase): OrderItemDao = db.orderItemDao()

    @Provides @Singleton
    fun provideClock(): Clock = Clock.systemUTC()

    @Provides @Singleton
    fun provideCustomerRepository(
        db: MKumarDatabase,
        customerDao: CustomerDao,
        orderDao: OrderDao,
        orderItemDao: OrderItemDao,
        clock: Clock
    ): CustomerRepository = CustomerRepository(db, customerDao, orderDao, orderItemDao, clock)
}
