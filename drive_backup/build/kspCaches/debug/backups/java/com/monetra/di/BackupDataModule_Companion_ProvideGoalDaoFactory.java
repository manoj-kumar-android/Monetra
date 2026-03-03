package com.monetra.di;

import com.monetra.data.local.MonetraDatabase;
import com.monetra.data.local.dao.GoalDao;
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
public final class BackupDataModule_Companion_ProvideGoalDaoFactory implements Factory<GoalDao> {
  private final Provider<MonetraDatabase> dbProvider;

  private BackupDataModule_Companion_ProvideGoalDaoFactory(Provider<MonetraDatabase> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public GoalDao get() {
    return provideGoalDao(dbProvider.get());
  }

  public static BackupDataModule_Companion_ProvideGoalDaoFactory create(
      Provider<MonetraDatabase> dbProvider) {
    return new BackupDataModule_Companion_ProvideGoalDaoFactory(dbProvider);
  }

  public static GoalDao provideGoalDao(MonetraDatabase db) {
    return Preconditions.checkNotNullFromProvides(BackupDataModule.Companion.provideGoalDao(db));
  }
}
