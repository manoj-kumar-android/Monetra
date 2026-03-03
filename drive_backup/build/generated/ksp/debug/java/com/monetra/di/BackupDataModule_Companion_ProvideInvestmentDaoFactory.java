package com.monetra.di;

import com.monetra.data.local.MonetraDatabase;
import com.monetra.data.local.dao.InvestmentDao;
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
public final class BackupDataModule_Companion_ProvideInvestmentDaoFactory implements Factory<InvestmentDao> {
  private final Provider<MonetraDatabase> dbProvider;

  private BackupDataModule_Companion_ProvideInvestmentDaoFactory(
      Provider<MonetraDatabase> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public InvestmentDao get() {
    return provideInvestmentDao(dbProvider.get());
  }

  public static BackupDataModule_Companion_ProvideInvestmentDaoFactory create(
      Provider<MonetraDatabase> dbProvider) {
    return new BackupDataModule_Companion_ProvideInvestmentDaoFactory(dbProvider);
  }

  public static InvestmentDao provideInvestmentDao(MonetraDatabase db) {
    return Preconditions.checkNotNullFromProvides(BackupDataModule.Companion.provideInvestmentDao(db));
  }
}
