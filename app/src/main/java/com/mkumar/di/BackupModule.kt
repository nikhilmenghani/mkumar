package com.mkumar.di

import com.mkumar.backup.BackupProvider
import com.mkumar.backup.BackupSnapshotter
import com.mkumar.backup.RoomBackupSnapshotter
import com.mkumar.backup.github.GithubBackupProvider
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class BackupModule {
    @Binds
    @Singleton
    abstract fun bindBackupProvider(implementation: GithubBackupProvider): BackupProvider

    @Binds
    @Singleton
    abstract fun bindBackupSnapshotter(implementation: RoomBackupSnapshotter): BackupSnapshotter
}
