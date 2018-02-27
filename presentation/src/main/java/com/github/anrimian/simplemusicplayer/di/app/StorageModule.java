package com.github.anrimian.simplemusicplayer.di.app;

import android.content.Context;

import com.github.anrimian.simplemusicplayer.data.storage.StorageMusicDataSource;

import javax.annotation.Nonnull;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class StorageModule {

    @Provides
    @Nonnull
    @Singleton
    StorageMusicDataSource storageMusicDataSource(Context context) {
        return new StorageMusicDataSource(context);
    }
}
