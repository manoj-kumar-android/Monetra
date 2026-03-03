package com.monetra.data.repository;

import android.content.Context;
import com.monetra.data.local.MonetraDatabase;
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
public final class SyncRepositoryImpl_Factory implements Factory<SyncRepositoryImpl> {
  private final Provider<Context> contextProvider;

  private final Provider<MonetraDatabase> dbProvider;

  private SyncRepositoryImpl_Factory(Provider<Context> contextProvider,
      Provider<MonetraDatabase> dbProvider) {
    this.contextProvider = contextProvider;
    this.dbProvider = dbProvider;
  }

  @Override
  public SyncRepositoryImpl get() {
    return newInstance(contextProvider.get(), dbProvider.get());
  }

  public static SyncRepositoryImpl_Factory create(Provider<Context> contextProvider,
      Provider<MonetraDatabase> dbProvider) {
    return new SyncRepositoryImpl_Factory(contextProvider, dbProvider);
  }

  public static SyncRepositoryImpl newInstance(Context context, MonetraDatabase db) {
    return new SyncRepositoryImpl(context, db);
  }
}
