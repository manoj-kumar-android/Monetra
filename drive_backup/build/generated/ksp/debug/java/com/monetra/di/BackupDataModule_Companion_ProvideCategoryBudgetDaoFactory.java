package com.monetra.di;

import com.monetra.data.local.MonetraDatabase;
import com.monetra.data.local.dao.CategoryBudgetDao;
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
public final class BackupDataModule_Companion_ProvideCategoryBudgetDaoFactory implements Factory<CategoryBudgetDao> {
  private final Provider<MonetraDatabase> dbProvider;

  private BackupDataModule_Companion_ProvideCategoryBudgetDaoFactory(
      Provider<MonetraDatabase> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public CategoryBudgetDao get() {
    return provideCategoryBudgetDao(dbProvider.get());
  }

  public static BackupDataModule_Companion_ProvideCategoryBudgetDaoFactory create(
      Provider<MonetraDatabase> dbProvider) {
    return new BackupDataModule_Companion_ProvideCategoryBudgetDaoFactory(dbProvider);
  }

  public static CategoryBudgetDao provideCategoryBudgetDao(MonetraDatabase db) {
    return Preconditions.checkNotNullFromProvides(BackupDataModule.Companion.provideCategoryBudgetDao(db));
  }
}
