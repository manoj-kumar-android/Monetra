package com.monetra.drivebackup.internal;

import android.content.Context;
import com.monetra.drivebackup.internal.drive.DriveService;
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
public final class DriveBackupManagerImpl_Factory implements Factory<DriveBackupManagerImpl> {
  private final Provider<Context> contextProvider;

  private final Provider<DriveService> driveServiceProvider;

  private final Provider<EncryptionManager> encryptionManagerProvider;

  private DriveBackupManagerImpl_Factory(Provider<Context> contextProvider,
      Provider<DriveService> driveServiceProvider,
      Provider<EncryptionManager> encryptionManagerProvider) {
    this.contextProvider = contextProvider;
    this.driveServiceProvider = driveServiceProvider;
    this.encryptionManagerProvider = encryptionManagerProvider;
  }

  @Override
  public DriveBackupManagerImpl get() {
    return newInstance(contextProvider.get(), driveServiceProvider.get(), encryptionManagerProvider.get());
  }

  public static DriveBackupManagerImpl_Factory create(Provider<Context> contextProvider,
      Provider<DriveService> driveServiceProvider,
      Provider<EncryptionManager> encryptionManagerProvider) {
    return new DriveBackupManagerImpl_Factory(contextProvider, driveServiceProvider, encryptionManagerProvider);
  }

  public static DriveBackupManagerImpl newInstance(Context context, DriveService driveService,
      EncryptionManager encryptionManager) {
    return new DriveBackupManagerImpl(context, driveService, encryptionManager);
  }
}
