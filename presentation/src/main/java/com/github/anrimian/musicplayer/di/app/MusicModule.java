package com.github.anrimian.musicplayer.di.app;


import android.content.Context;

import androidx.annotation.NonNull;

import com.github.anrimian.musicplayer.data.controllers.music.MusicPlayerControllerImpl;
import com.github.anrimian.musicplayer.data.controllers.music.SystemMusicControllerImpl;
import com.github.anrimian.musicplayer.data.database.dao.albums.AlbumsDaoWrapper;
import com.github.anrimian.musicplayer.data.database.dao.artist.ArtistsDaoWrapper;
import com.github.anrimian.musicplayer.data.database.dao.compositions.CompositionsDaoWrapper;
import com.github.anrimian.musicplayer.data.database.dao.genre.GenresDaoWrapper;
import com.github.anrimian.musicplayer.data.database.dao.play_queue.PlayQueueDaoWrapper;
import com.github.anrimian.musicplayer.data.repositories.library.LibraryRepositoryImpl;
import com.github.anrimian.musicplayer.data.repositories.library.folders.MusicFolderDataSource;
import com.github.anrimian.musicplayer.data.repositories.play_queue.PlayQueueRepositoryImpl;
import com.github.anrimian.musicplayer.data.repositories.source.SourceRepository;
import com.github.anrimian.musicplayer.data.storage.providers.albums.StorageAlbumsProvider;
import com.github.anrimian.musicplayer.data.storage.providers.music.StorageMusicDataSource;
import com.github.anrimian.musicplayer.data.storage.providers.music.StorageMusicProvider;
import com.github.anrimian.musicplayer.domain.business.analytics.Analytics;
import com.github.anrimian.musicplayer.domain.business.player.MusicPlayerInteractor;
import com.github.anrimian.musicplayer.domain.business.player.MusicServiceInteractor;
import com.github.anrimian.musicplayer.domain.business.player.PlayerErrorParser;
import com.github.anrimian.musicplayer.domain.controllers.MusicPlayerController;
import com.github.anrimian.musicplayer.domain.controllers.SystemMusicController;
import com.github.anrimian.musicplayer.domain.controllers.SystemServiceController;
import com.github.anrimian.musicplayer.domain.repositories.LibraryRepository;
import com.github.anrimian.musicplayer.domain.repositories.PlayQueueRepository;
import com.github.anrimian.musicplayer.domain.repositories.SettingsRepository;
import com.github.anrimian.musicplayer.domain.repositories.UiStateRepository;
import com.github.anrimian.musicplayer.ui.common.images.CoverImageLoader;

import javax.annotation.Nonnull;
import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import io.reactivex.Scheduler;

import static com.github.anrimian.musicplayer.di.app.SchedulerModule.DB_SCHEDULER;
import static com.github.anrimian.musicplayer.di.app.SchedulerModule.IO_SCHEDULER;
import static com.github.anrimian.musicplayer.di.app.SchedulerModule.UI_SCHEDULER;

/**
 * Created on 02.11.2017.
 */

@Module
class MusicModule {

    @Provides
    @NonNull
    @Singleton
    MusicPlayerInteractor musicPlayerInteractor(MusicPlayerController musicPlayerController,
                                                SettingsRepository settingsRepository,
                                                SystemMusicController systemMusicController,
                                                SystemServiceController systemServiceController,
                                                PlayQueueRepository playQueueRepository,
                                                LibraryRepository musicProviderRepository,
                                                Analytics analytics,
                                                PlayerErrorParser playerErrorParser) {
        return new MusicPlayerInteractor(musicPlayerController,
                settingsRepository,
                systemMusicController,
                systemServiceController,
                playQueueRepository,
                musicProviderRepository,
                analytics);
    }

    @Provides
    @NonNull
    @Singleton
    PlayQueueRepository playQueueRepository(PlayQueueDaoWrapper playQueueDao,
                                            SettingsRepository settingsPreferences,
                                            UiStateRepository uiStateRepository,
                                            @Named(DB_SCHEDULER) Scheduler dbScheduler) {
        return new PlayQueueRepositoryImpl(playQueueDao,
                settingsPreferences,
                uiStateRepository,
                dbScheduler);
    }

    @Provides
    @NonNull
    @Singleton
    SystemMusicController provideSystemMusicController(Context context) {
        return new SystemMusicControllerImpl(context);
    }

    @Provides
    @NonNull
    @Singleton
    MusicPlayerController provideMusicPlayerController(UiStateRepository uiStateRepository,
                                                       Context context,
                                                       @Named(UI_SCHEDULER) Scheduler scheduler,
                                                       PlayerErrorParser playerErrorParser) {
        return new MusicPlayerControllerImpl(uiStateRepository, context, scheduler, playerErrorParser);
    }

    @Provides
    @NonNull
    @Singleton
    LibraryRepository musicProviderRepository(StorageMusicDataSource storageMusicDataSource,
                                              CompositionsDaoWrapper compositionsDao,
                                              ArtistsDaoWrapper artistsDao,
                                              AlbumsDaoWrapper albumsDao,
                                              GenresDaoWrapper genresDao,
                                              MusicFolderDataSource musicFolderDataSource,
                                              SettingsRepository settingsPreferences,
                                              @Named(IO_SCHEDULER) Scheduler scheduler) {
        return new LibraryRepositoryImpl(storageMusicDataSource,
                compositionsDao,
                artistsDao,
                albumsDao,
                genresDao,
                musicFolderDataSource,
                settingsPreferences,
                scheduler);
    }

    @Provides
    @Nonnull
    @Singleton
    MusicServiceInteractor musicServiceInteractor(SettingsRepository settingsRepository) {
        return new MusicServiceInteractor(settingsRepository);
    }

    @Provides
    @Nonnull
    @Singleton
    CoverImageLoader coverImageLoader(StorageAlbumsProvider storageAlbumsProvider) {
        return new CoverImageLoader(storageAlbumsProvider);
    }

    @Provides
    @Nonnull
    @Singleton
    SourceRepository sourceRepository(CompositionsDaoWrapper compositionsDao,
                                      StorageMusicProvider storageMusicProvider,
                                      @Named(DB_SCHEDULER) Scheduler scheduler) {
        return new SourceRepository(compositionsDao, storageMusicProvider, scheduler);
    }

}
