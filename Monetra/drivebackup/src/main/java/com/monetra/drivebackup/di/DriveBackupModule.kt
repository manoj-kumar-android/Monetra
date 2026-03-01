package com.monetra.drivebackup.di

import com.monetra.drivebackup.api.DriveBackupManager
import com.monetra.drivebackup.internal.DriveBackupManagerImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class DriveBackupModule {

    @Binds
    @Singleton
    abstract fun bindDriveBackupManager(
        impl: DriveBackupManagerImpl
    ): DriveBackupManager
}
