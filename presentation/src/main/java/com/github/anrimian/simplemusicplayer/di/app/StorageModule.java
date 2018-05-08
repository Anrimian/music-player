package com.github.anrimian.simplemusicplayer.di.app;

import android.content.Context;

import com.github.anrimian.simplemusicplayer.data.storage.StorageMusicProvider;
import com.github.anrimian.simplemusicplayer.data.storage.StorageMusicDataSource;

import javax.annotation.Nonnull;
import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import io.reactivex.Scheduler;

import static com.github.anrimian.simplemusicplayer.di.app.SchedulerModule.IO_SCHEDULER;

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
    StorageMusicDataSource storageMusicDataSource(StorageMusicProvider musicProvider,
                                                  @Named(IO_SCHEDULER) Scheduler scheduler) {
        return new StorageMusicDataSource(musicProvider, scheduler);
    }
}
