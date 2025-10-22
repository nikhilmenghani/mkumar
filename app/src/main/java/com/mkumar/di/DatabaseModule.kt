package com.mkumar.di

import android.content.Context
import androidx.room.Room
import com.mkumar.data.local.Converters
import com.mkumar.data.local.MKumarDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides @Singleton
    fun provideJson(): Json = Json { ignoreUnknownKeys = true }

    @Provides @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
    ): MKumarDatabase =
        Room.databaseBuilder(context, MKumarDatabase::class.java, "mkumar.db")
            .build()
}
