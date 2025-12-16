package com.mkumar.di

import android.content.Context
import com.mkumar.data.PreferencesManager
import com.mkumar.data.db.dao.OutboxDao
import com.mkumar.network.NetworkClient
import com.mkumar.repository.SyncRepository
import com.mkumar.repository.impl.SyncRepositoryImpl
import com.mkumar.sync.remote.CloudRemote
import com.mkumar.sync.remote.GithubRemoteImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SyncModule {

    @Provides
    @Singleton
    fun provideSyncRepository(
        outboxDao: OutboxDao,
        @ApplicationContext context: Context
    ): SyncRepository =
        SyncRepositoryImpl(outboxDao, context)

    @Provides
    @Singleton
    fun provideCloudRemote(
        prefs: PreferencesManager,
        networkClient: NetworkClient,
        json: Json
    ): CloudRemote =
        GithubRemoteImpl(prefs, networkClient, json)

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        prettyPrint = false
    }
}
