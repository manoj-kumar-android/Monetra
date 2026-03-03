package com.monetra.data.sync;

import com.monetra.data.local.MonetraDatabase;
import com.monetra.domain.repository.SyncRepository;
import com.monetra.drivebackup.api.DriveBackupManager;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast",
    "deprecation",
    "nullness:initialization.field.uninitialized"
})
public final class SyncManager_Factory implements Factory<SyncManager> {
  private final Provider<LocalSyncDataSource> localDataSourceProvider;

  private final Provider<DriveSyncDataSource> driveDataSourceProvider;

  private final Provider<SyncRepository> syncRepositoryProvider;

  private final Provider<DriveBackupManager> driveManagerProvider;

  private final Provider<MonetraDatabase> dbProvider;

  private SyncManager_Factory(Provider<LocalSyncDataSource> localDataSourceProvider,
      Provider<DriveSyncDataSource> driveDataSourceProvider,
      Provider<SyncRepository> syncRepositoryProvider,
      Provider<DriveBackupManager> driveManagerProvider, Provider<MonetraDatabase> dbProvider) {
    this.localDataSourceProvider = localDataSourceProvider;
    this.driveDataSourceProvider = driveDataSourceProvider;
    this.syncRepositoryProvider = syncRepositoryProvider;
    this.driveManagerProvider = driveManagerProvider;
    this.dbProvider = dbProvider;
  }

  @Override
  public SyncManager get() {
    return newInstance(localDataSourceProvider.get(), driveDataSourceProvider.get(), syncRepositoryProvider.get(), driveManagerProvider.get(), dbProvider.get());
  }

  public static SyncManager_Factory create(Provider<LocalSyncDataSource> localDataSourceProvider,
      Provider<DriveSyncDataSource> driveDataSourceProvider,
      Provider<SyncRepository> syncRepositoryProvider,
      Provider<DriveBackupManager> driveManagerProvider, Provider<MonetraDatabase> dbProvider) {
    return new SyncManager_Factory(localDataSourceProvider, driveDataSourceProvider, syncRepositoryProvider, driveManagerProvider, dbProvider);
  }

  public static SyncManager newInstance(LocalSyncDataSource localDataSource,
      DriveSyncDataSource driveDataSource, SyncRepository syncRepository,
      DriveBackupManager driveManager, MonetraDatabase db) {
    return new SyncManager(localDataSource, driveDataSource, syncRepository, driveManager, db);
  }
}
