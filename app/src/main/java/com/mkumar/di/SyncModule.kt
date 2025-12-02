package com.mkumar.di

import com.mkumar.data.db.dao.OutboxDao
import com.mkumar.repository.SyncRepository
import com.mkumar.repository.impl.SyncRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SyncModule {

    @Provides
    @Singleton
    fun provideSyncRepository(
        outboxDao: OutboxDao
    ): SyncRepository {
        return SyncRepositoryImpl(outboxDao)
    }

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        prettyPrint = false
    }
}
