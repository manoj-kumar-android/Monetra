package com.monetra.drivebackup.internal.worker;

import androidx.hilt.work.WorkerAssistedFactory;
import androidx.work.ListenableWorker;
import dagger.Binds;
import dagger.Module;
import dagger.hilt.InstallIn;
import dagger.hilt.codegen.OriginatingElement;
import dagger.hilt.components.SingletonComponent;
import dagger.multibindings.IntoMap;
import dagger.multibindings.StringKey;
import javax.annotation.processing.Generated;

@Generated("androidx.hilt.AndroidXHiltProcessor")
@Module
@InstallIn(SingletonComponent.class)
@OriginatingElement(
    topLevelClass = BackupWorker.class
)
public interface BackupWorker_HiltModule {
  @Binds
  @IntoMap
  @StringKey("com.monetra.drivebackup.internal.worker.BackupWorker")
  WorkerAssistedFactory<? extends ListenableWorker> bind(BackupWorker_AssistedFactory factory);
}
