package com.monetra.data.worker;

import android.content.Context;
import androidx.work.WorkerParameters;
import dagger.internal.DaggerGenerated;
import dagger.internal.InstanceFactory;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

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
public final class FullBackupWorker_AssistedFactory_Impl implements FullBackupWorker_AssistedFactory {
  private final FullBackupWorker_Factory delegateFactory;

  FullBackupWorker_AssistedFactory_Impl(FullBackupWorker_Factory delegateFactory) {
    this.delegateFactory = delegateFactory;
  }

  @Override
  public FullBackupWorker create(Context p0, WorkerParameters p1) {
    return delegateFactory.get(p0, p1);
  }

  public static Provider<FullBackupWorker_AssistedFactory> create(
      FullBackupWorker_Factory delegateFactory) {
    return InstanceFactory.create(new FullBackupWorker_AssistedFactory_Impl(delegateFactory));
  }

  public static dagger.internal.Provider<FullBackupWorker_AssistedFactory> createFactoryProvider(
      FullBackupWorker_Factory delegateFactory) {
    return InstanceFactory.create(new FullBackupWorker_AssistedFactory_Impl(delegateFactory));
  }
}
