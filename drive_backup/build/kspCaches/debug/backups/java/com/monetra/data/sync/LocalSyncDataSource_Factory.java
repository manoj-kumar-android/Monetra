package com.monetra.data.sync;

import com.monetra.data.local.MonetraDatabase;
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
public final class LocalSyncDataSource_Factory implements Factory<LocalSyncDataSource> {
  private final Provider<MonetraDatabase> dbProvider;

  private LocalSyncDataSource_Factory(Provider<MonetraDatabase> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public LocalSyncDataSource get() {
    return newInstance(dbProvider.get());
  }

  public static LocalSyncDataSource_Factory create(Provider<MonetraDatabase> dbProvider) {
    return new LocalSyncDataSource_Factory(dbProvider);
  }

  public static LocalSyncDataSource newInstance(MonetraDatabase db) {
    return new LocalSyncDataSource(db);
  }
}
