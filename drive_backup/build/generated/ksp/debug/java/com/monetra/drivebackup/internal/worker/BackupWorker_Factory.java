package com.monetra.drivebackup.internal.worker;

import android.content.Context;
import androidx.work.WorkerParameters;
import com.monetra.drivebackup.internal.drive.DriveService;
import com.monetra.drivebackup.internal.security.EncryptionManager;
import dagger.internal.DaggerGenerated;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata
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
public final class BackupWorker_Factory {
  private final Provider<DriveService> driveServiceProvider;

  private final Provider<EncryptionManager> encryptionManagerProvider;

  private BackupWorker_Factory(Provider<DriveService> driveServiceProvider,
      Provider<EncryptionManager> encryptionManagerProvider) {
    this.driveServiceProvider = driveServiceProvider;
    this.encryptionManagerProvider = encryptionManagerProvider;
  }

  public BackupWorker get(Context context, WorkerParameters params) {
    return newInstance(context, params, driveServiceProvider.get(), encryptionManagerProvider.get());
  }

  public static BackupWorker_Factory create(Provider<DriveService> driveServiceProvider,
      Provider<EncryptionManager> encryptionManagerProvider) {
    return new BackupWorker_Factory(driveServiceProvider, encryptionManagerProvider);
  }

  public static BackupWorker newInstance(Context context, WorkerParameters params,
      DriveService driveService, EncryptionManager encryptionManager) {
    return new BackupWorker(context, params, driveService, encryptionManager);
  }
}
