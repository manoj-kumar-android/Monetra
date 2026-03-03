package com.monetra.data.worker;

import android.content.Context;
import androidx.work.WorkerParameters;
import com.monetra.domain.repository.CloudBackupRepository;
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
public final class FullBackupWorker_Factory {
  private final Provider<CloudBackupRepository> cloudBackupRepositoryProvider;

  private FullBackupWorker_Factory(Provider<CloudBackupRepository> cloudBackupRepositoryProvider) {
    this.cloudBackupRepositoryProvider = cloudBackupRepositoryProvider;
  }

  public FullBackupWorker get(Context context, WorkerParameters params) {
    return newInstance(context, params, cloudBackupRepositoryProvider.get());
  }

  public static FullBackupWorker_Factory create(
      Provider<CloudBackupRepository> cloudBackupRepositoryProvider) {
    return new FullBackupWorker_Factory(cloudBackupRepositoryProvider);
  }

  public static FullBackupWorker newInstance(Context context, WorkerParameters params,
      CloudBackupRepository cloudBackupRepository) {
    return new FullBackupWorker(context, params, cloudBackupRepository);
  }
}
