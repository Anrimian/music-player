package com.github.anrimian.musicplayer.di.app;

import android.content.Context;

import com.github.anrimian.musicplayer.data.repositories.music.folders.MusicFolderDataSource;
import com.github.anrimian.musicplayer.data.storage.providers.music.StorageMusicProvider;
import com.github.anrimian.musicplayer.data.storage.providers.music.StorageMusicDataSource;
import com.github.anrimian.musicplayer.data.storage.files.FileManager;

import javax.annotation.Nonnull;
import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import io.reactivex.Scheduler;

import static com.github.anrimian.musicplayer.di.app.SchedulerModule.IO_SCHEDULER;

@Module
public class StorageModule {

    @Provides
    @Nonnull
    @Singleton
    StorageMusicProvider storageMusicProvider(Context context) {
        return new StorageMusicProvider(context);
    }

    @Provides
    @Nonnull
    @Singleton
    FileManager fileManager() {
        return new FileManager();
    }

    @Provides
    @Nonnull
    @Singleton
    StorageMusicDataSource storageMusicDataSource(StorageMusicProvider musicProvider,
                                                  FileManager fileManager,
                                                  @Named(IO_SCHEDULER) Scheduler scheduler) {
        return new StorageMusicDataSource(musicProvider, fileManager, scheduler);
    }

    @Provides
    @Nonnull
    @Singleton
    MusicFolderDataSource musicFolderDataSource(StorageMusicDataSource storageMusicDataSource) {
        return new MusicFolderDataSource(storageMusicDataSource);
    }
}
