package com.monetra.di;

import com.monetra.data.local.MonetraDatabase;
import com.monetra.data.local.dao.MonthlyReportDao;
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
public final class BackupDataModule_Companion_ProvideMonthlyReportDaoFactory implements Factory<MonthlyReportDao> {
  private final Provider<MonetraDatabase> dbProvider;

  private BackupDataModule_Companion_ProvideMonthlyReportDaoFactory(
      Provider<MonetraDatabase> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public MonthlyReportDao get() {
    return provideMonthlyReportDao(dbProvider.get());
  }

  public static BackupDataModule_Companion_ProvideMonthlyReportDaoFactory create(
      Provider<MonetraDatabase> dbProvider) {
    return new BackupDataModule_Companion_ProvideMonthlyReportDaoFactory(dbProvider);
  }

  public static MonthlyReportDao provideMonthlyReportDao(MonetraDatabase db) {
    return Preconditions.checkNotNullFromProvides(BackupDataModule.Companion.provideMonthlyReportDao(db));
  }
}
