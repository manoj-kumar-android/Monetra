package com.monetra.data.sync;

import android.content.Context;
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
public final class DriveSyncDataSource_Factory implements Factory<DriveSyncDataSource> {
  private final Provider<Context> contextProvider;

  private final Provider<DriveBackupManager> driveManagerProvider;

  private final Provider<EncryptionManager> encryptionManagerProvider;

  private DriveSyncDataSource_Factory(Provider<Context> contextProvider,
      Provider<DriveBackupManager> driveManagerProvider,
      Provider<EncryptionManager> encryptionManagerProvider) {
    this.contextProvider = contextProvider;
    this.driveManagerProvider = driveManagerProvider;
    this.encryptionManagerProvider = encryptionManagerProvider;
  }

  @Override
  public DriveSyncDataSource get() {
    return newInstance(contextProvider.get(), driveManagerProvider.get(), encryptionManagerProvider.get());
  }

  public static DriveSyncDataSource_Factory create(Provider<Context> contextProvider,
      Provider<DriveBackupManager> driveManagerProvider,
      Provider<EncryptionManager> encryptionManagerProvider) {
    return new DriveSyncDataSource_Factory(contextProvider, driveManagerProvider, encryptionManagerProvider);
  }

  public static DriveSyncDataSource newInstance(Context context, DriveBackupManager driveManager,
      EncryptionManager encryptionManager) {
    return new DriveSyncDataSource(context, driveManager, encryptionManager);
  }
}
