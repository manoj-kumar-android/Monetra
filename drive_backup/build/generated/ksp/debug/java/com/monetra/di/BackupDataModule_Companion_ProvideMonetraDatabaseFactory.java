package com.monetra.di;

import android.app.Application;
import com.monetra.data.local.MonetraDatabase;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
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
public final class BackupDataModule_Companion_ProvideMonetraDatabaseFactory implements Factory<MonetraDatabase> {
  private final Provider<Application> appProvider;

  private BackupDataModule_Companion_ProvideMonetraDatabaseFactory(
      Provider<Application> appProvider) {
    this.appProvider = appProvider;
  }

  @Override
  public MonetraDatabase get() {
    return provideMonetraDatabase(appProvider.get());
  }

  public static BackupDataModule_Companion_ProvideMonetraDatabaseFactory create(
      Provider<Application> appProvider) {
    return new BackupDataModule_Companion_ProvideMonetraDatabaseFactory(appProvider);
  }

  public static MonetraDatabase provideMonetraDatabase(Application app) {
    return Preconditions.checkNotNullFromProvides(BackupDataModule.Companion.provideMonetraDatabase(app));
  }
}
