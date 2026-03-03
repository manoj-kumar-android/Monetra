package com.monetra.drivebackup.internal.security;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
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
public final class EncryptionManager_Factory implements Factory<EncryptionManager> {
  @Override
  public EncryptionManager get() {
    return newInstance();
  }

  public static EncryptionManager_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static EncryptionManager newInstance() {
    return new EncryptionManager();
  }

  private static final class InstanceHolder {
    static final EncryptionManager_Factory INSTANCE = new EncryptionManager_Factory();
  }
}
