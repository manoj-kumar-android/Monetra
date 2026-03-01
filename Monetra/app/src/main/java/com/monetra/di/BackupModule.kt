package com.monetra.di

import com.monetra.data.backup.BackupManagerImpl
import com.monetra.data.backup.RestoreManagerImpl
import com.monetra.domain.backup.BackupManager
import com.monetra.domain.backup.RestoreManager
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
    abstract fun bindBackupManager(impl: BackupManagerImpl): BackupManager

    @Binds
    @Singleton
    abstract fun bindRestoreManager(impl: RestoreManagerImpl): RestoreManager
}
