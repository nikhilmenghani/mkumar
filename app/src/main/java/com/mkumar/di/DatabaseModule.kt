// com/mkumar/di/DatabaseModule.kt
package com.mkumar.di

import android.content.Context
import androidx.room.Room
import com.mkumar.data.db.AppDatabase
import com.mkumar.data.db.dao.CustomerDao
import com.mkumar.data.db.dao.OrderDao
import com.mkumar.data.db.dao.OrderItemDao
import com.mkumar.data.db.dao.SearchDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

// com/mkumar/di/DatabaseModule.kt
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides @Singleton
    fun provideDatabase(@ApplicationContext ctx: Context): AppDatabase =
        Room.databaseBuilder(ctx, AppDatabase::class.java, "mkumar.db").build()

    @Provides fun provideCustomerDao(db: AppDatabase): CustomerDao = db.customerDao()
    @Provides fun provideOrderDao(db: AppDatabase): OrderDao = db.orderDao()
    @Provides fun provideOrderItemDao(db: AppDatabase): OrderItemDao = db.orderItemDao()

    @Provides
    fun provideSearchDao(db: AppDatabase): SearchDao = db.searchDao()   // <- add this
}
