package com.github.anrimian.simplemusicplayer.di.library;

import android.content.Context;

import com.github.anrimian.simplemusicplayer.data.repositories.music.MusicProviderRepositoryImpl;
import com.github.anrimian.simplemusicplayer.domain.business.music.StorageLibraryInteractor;
import com.github.anrimian.simplemusicplayer.domain.business.music.StorageLibraryInteractorImpl;
import com.github.anrimian.simplemusicplayer.domain.repositories.MusicProviderRepository;

import javax.annotation.Nonnull;
import javax.inject.Named;

import dagger.Module;
import dagger.Provides;
import io.reactivex.Scheduler;

import static com.github.anrimian.simplemusicplayer.di.app.SchedulerModule.IO_SCHEDULER;

/**
 * Created on 29.10.2017.
 */
@Module
public class LibraryModule {

    @Provides
    @Nonnull
    MusicProviderRepository provideMusicProviderRepository(Context ctx,
                                                           @Named(IO_SCHEDULER) Scheduler scheduler) {
        return new MusicProviderRepositoryImpl(ctx, scheduler);
    }

    @Provides
    @Nonnull
    @LibraryScope
    StorageLibraryInteractor provideMusicProviderInteractor(MusicProviderRepository musicProviderRepository) {
        return new StorageLibraryInteractorImpl(musicProviderRepository);
    }
}
