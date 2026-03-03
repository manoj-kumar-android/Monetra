package com.monetra.data.repository;

import android.content.Context;
import com.monetra.data.local.MonetraDatabase;
import com.monetra.domain.repository.SyncRepository;
import com.monetra.drivebackup.api.DriveBackupManager;
import com.monetra.drivebackup.internal.security.EncryptionManager;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
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
public final class CloudBackupRepositoryImpl_Factory implements Factory<CloudBackupRepositoryImpl> {
  private final Provider<Context> contextProvider;

  private final Provider<MonetraDatabase> dbProvider;

  private final Provider<DriveBackupManager> driveBackupManagerProvider;

  private final Provider<EncryptionManager> encryptionManagerProvider;

  private final Provider<SyncRepository> syncRepositoryProvider;

  private CloudBackupRepositoryImpl_Factory(Provider<Context> contextProvider,
      Provider<MonetraDatabase> dbProvider, Provider<DriveBackupManager> driveBackupManagerProvider,
      Provider<EncryptionManager> encryptionManagerProvider,
      Provider<SyncRepository> syncRepositoryProvider) {
    this.contextProvider = contextProvider;
    this.dbProvider = dbProvider;
    this.driveBackupManagerProvider = driveBackupManagerProvider;
    this.encryptionManagerProvider = encryptionManagerProvider;
    this.syncRepositoryProvider = syncRepositoryProvider;
  }

  @Override
  public CloudBackupRepositoryImpl get() {
    return newInstance(contextProvider.get(), dbProvider.get(), driveBackupManagerProvider.get(), encryptionManagerProvider.get(), syncRepositoryProvider.get());
  }

  public static CloudBackupRepositoryImpl_Factory create(Provider<Context> contextProvider,
      Provider<MonetraDatabase> dbProvider, Provider<DriveBackupManager> driveBackupManagerProvider,
      Provider<EncryptionManager> encryptionManagerProvider,
      Provider<SyncRepository> syncRepositoryProvider) {
    return new CloudBackupRepositoryImpl_Factory(contextProvider, dbProvider, driveBackupManagerProvider, encryptionManagerProvider, syncRepositoryProvider);
  }

  public static CloudBackupRepositoryImpl newInstance(Context context, MonetraDatabase db,
      DriveBackupManager driveBackupManager, EncryptionManager encryptionManager,
      SyncRepository syncRepository) {
    return new CloudBackupRepositoryImpl(context, db, driveBackupManager, encryptionManager, syncRepository);
  }
}
