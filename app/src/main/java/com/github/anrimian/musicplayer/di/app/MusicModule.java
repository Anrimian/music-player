package com.github.anrimian.musicplayer.di.app;


import static com.github.anrimian.musicplayer.di.app.SchedulerModule.DB_SCHEDULER;
import static com.github.anrimian.musicplayer.di.app.SchedulerModule.IO_SCHEDULER;
import static com.github.anrimian.musicplayer.di.app.SchedulerModule.UI_SCHEDULER;

import android.content.Context;

import androidx.annotation.NonNull;

import com.github.anrimian.filesync.SyncInteractor;
import com.github.anrimian.musicplayer.data.controllers.music.MusicPlayerControllerImpl;
import com.github.anrimian.musicplayer.data.controllers.music.SystemMusicControllerImpl;
import com.github.anrimian.musicplayer.data.controllers.music.equalizer.EqualizerController;
import com.github.anrimian.musicplayer.data.controllers.music.equalizer.external.ExternalEqualizer;
import com.github.anrimian.musicplayer.data.controllers.music.equalizer.internal.InternalEqualizer;
import com.github.anrimian.musicplayer.data.controllers.music.players.utils.ExoPlayerMediaItemBuilder;
import com.github.anrimian.musicplayer.data.controllers.music.players.utils.MediaPlayerDataSourceBuilder;
import com.github.anrimian.musicplayer.data.database.dao.albums.AlbumsDaoWrapper;
import com.github.anrimian.musicplayer.data.database.dao.artist.ArtistsDaoWrapper;
import com.github.anrimian.musicplayer.data.database.dao.compositions.CompositionsDaoWrapper;
import com.github.anrimian.musicplayer.data.database.dao.folders.FoldersDaoWrapper;
import com.github.anrimian.musicplayer.data.database.dao.genre.GenresDaoWrapper;
import com.github.anrimian.musicplayer.data.database.dao.play_queue.PlayQueueDaoWrapper;
import com.github.anrimian.musicplayer.data.repositories.equalizer.EqualizerRepositoryImpl;
import com.github.anrimian.musicplayer.data.repositories.equalizer.EqualizerStateRepository;
import com.github.anrimian.musicplayer.data.repositories.library.LibraryRepositoryImpl;
import com.github.anrimian.musicplayer.data.repositories.play_queue.PlayQueueRepositoryImpl;
import com.github.anrimian.musicplayer.data.storage.files.StorageFilesDataSource;
import com.github.anrimian.musicplayer.domain.controllers.MusicPlayerController;
import com.github.anrimian.musicplayer.domain.controllers.SystemMusicController;
import com.github.anrimian.musicplayer.domain.controllers.SystemServiceController;
import com.github.anrimian.musicplayer.domain.interactors.analytics.Analytics;
import com.github.anrimian.musicplayer.domain.interactors.library.LibraryAlbumsInteractor;
import com.github.anrimian.musicplayer.domain.interactors.library.LibraryArtistsInteractor;
import com.github.anrimian.musicplayer.domain.interactors.library.LibraryCompositionsInteractor;
import com.github.anrimian.musicplayer.domain.interactors.library.LibraryFoldersInteractor;
import com.github.anrimian.musicplayer.domain.interactors.player.CompositionSourceInteractor;
import com.github.anrimian.musicplayer.domain.interactors.player.EqualizerInteractor;
import com.github.anrimian.musicplayer.domain.interactors.player.ExternalPlayerInteractor;
import com.github.anrimian.musicplayer.domain.interactors.player.LibraryPlayerInteractor;
import com.github.anrimian.musicplayer.domain.interactors.player.MusicServiceInteractor;
import com.github.anrimian.musicplayer.domain.interactors.player.PlayerCoordinatorInteractor;
import com.github.anrimian.musicplayer.domain.interactors.player.PlayerErrorParser;
import com.github.anrimian.musicplayer.domain.interactors.player.PlayerInteractor;
import com.github.anrimian.musicplayer.domain.interactors.playlists.PlayListsInteractor;
import com.github.anrimian.musicplayer.domain.repositories.EditorRepository;
import com.github.anrimian.musicplayer.domain.repositories.EqualizerRepository;
import com.github.anrimian.musicplayer.domain.repositories.LibraryRepository;
import com.github.anrimian.musicplayer.domain.repositories.MediaScannerRepository;
import com.github.anrimian.musicplayer.domain.repositories.PlayQueueRepository;
import com.github.anrimian.musicplayer.domain.repositories.SettingsRepository;
import com.github.anrimian.musicplayer.domain.repositories.StorageSourceRepository;
import com.github.anrimian.musicplayer.domain.repositories.UiStateRepository;
import com.github.anrimian.musicplayer.infrastructure.MediaSessionHandler;
import com.github.anrimian.musicplayer.ui.common.error.parser.ErrorParser;
import com.github.anrimian.musicplayer.ui.common.images.CoverImageLoader;
import com.github.anrimian.musicplayer.ui.common.theme.ThemeController;
import com.github.anrimian.musicplayer.ui.equalizer.EqualizerPresenter;

import javax.annotation.Nonnull;
import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import io.reactivex.rxjava3.core.Scheduler;

/**
 * Created on 02.11.2017.
 */

@Module
public class MusicModule {

    @Provides
    @NonNull
    @Singleton
    PlayerInteractor playerInteractor(MusicPlayerController musicPlayerController,
                                       CompositionSourceInteractor compositionSourceInteractor,
                                       PlayerErrorParser playerErrorParser,
                                       SystemMusicController systemMusicController,
                                       SystemServiceController systemServiceController,
                                       SettingsRepository settingsRepository,
                                       Analytics analytics) {
        return new PlayerInteractor(musicPlayerController,
                compositionSourceInteractor,
                playerErrorParser,
                systemMusicController,
                systemServiceController,
                settingsRepository,
                analytics,
                2);
    }

    @Provides
    @NonNull
    @Singleton
    CompositionSourceInteractor compositionSourceInteractor(StorageSourceRepository storageSourceRepository,
                                                            SyncInteractor<?, ?, Long> syncInteractor) {
        return new CompositionSourceInteractor(storageSourceRepository, syncInteractor);
    }

    @Provides
    @NonNull
    @Singleton
    PlayerCoordinatorInteractor playerCoordinatorInteractor(PlayerInteractor playerInteractor,
                                                              UiStateRepository uiStateRepository) {
        return new PlayerCoordinatorInteractor(playerInteractor, uiStateRepository);
    }

    @Provides
    @NonNull
    @Singleton
    ExternalPlayerInteractor externalPlayerInteractor(PlayerCoordinatorInteractor interactor,
                                                       SettingsRepository settingsRepository) {
        return new ExternalPlayerInteractor(interactor, settingsRepository);
    }

    @Provides
    @NonNull
    @Singleton
    LibraryPlayerInteractor libraryPlayerInteractor(PlayerCoordinatorInteractor playerCoordinatorInteractor,
                                                     SettingsRepository settingsRepository,
                                                     PlayQueueRepository playQueueRepository,
                                                     LibraryRepository musicProviderRepository,
                                                     UiStateRepository uiStateRepository,
                                                     Analytics analytics) {
        return new LibraryPlayerInteractor(playerCoordinatorInteractor,
                settingsRepository,
                playQueueRepository,
                musicProviderRepository,
                uiStateRepository,
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
    MusicPlayerController musicPlayerController(SettingsRepository settingsRepository,
                                                 Context context,
                                                 @Named(UI_SCHEDULER) Scheduler uiScheduler,
                                                 EqualizerController equalizerController,
                                                 ExoPlayerMediaItemBuilder exoPlayerMediaItemBuilder,
                                                 MediaPlayerDataSourceBuilder mediaPlayerSourceBuilder) {
        return new MusicPlayerControllerImpl(settingsRepository,
                context,
                uiScheduler,
                equalizerController,
                exoPlayerMediaItemBuilder,
                mediaPlayerSourceBuilder);
    }

    @Provides
    @NonNull
    @Singleton
    LibraryRepository musicProviderRepository(StorageFilesDataSource storageFilesDataSource,
                                              CompositionsDaoWrapper compositionsDao,
                                              ArtistsDaoWrapper artistsDao,
                                              AlbumsDaoWrapper albumsDao,
                                              GenresDaoWrapper genresDao,
                                              FoldersDaoWrapper foldersDao,
                                              SettingsRepository settingsPreferences,
                                              @Named(IO_SCHEDULER) Scheduler scheduler) {
        return new LibraryRepositoryImpl(
                storageFilesDataSource,
                compositionsDao,
                artistsDao,
                albumsDao,
                genresDao,
                foldersDao,
                settingsPreferences,
                scheduler);
    }

    @Provides
    @Nonnull
    @Singleton
    MusicServiceInteractor musicServiceInteractor(PlayerCoordinatorInteractor playerCoordinatorInteractor,
                                                  LibraryPlayerInteractor libraryPlayerInteractor,
                                                  ExternalPlayerInteractor externalPlayerInteractor,
                                                  LibraryCompositionsInteractor libraryCompositionsInteractor,
                                                  LibraryFoldersInteractor libraryFoldersInteractor,
                                                  LibraryArtistsInteractor libraryArtistsInteractor,
                                                  LibraryAlbumsInteractor libraryAlbumsInteractor,
                                                  PlayListsInteractor playListsInteractor,
                                                  SettingsRepository settingsRepository) {
        return new MusicServiceInteractor(playerCoordinatorInteractor,
                libraryPlayerInteractor,
                externalPlayerInteractor,
                libraryCompositionsInteractor,
                libraryFoldersInteractor,
                libraryArtistsInteractor,
                libraryAlbumsInteractor,
                playListsInteractor,
                settingsRepository);
    }

    @Provides
    @Nonnull
    @Singleton
    CoverImageLoader coverImageLoader(Context context, ThemeController themeController) {
        return new CoverImageLoader(context, themeController);
    }

    @Provides
    @NonNull
    @Singleton
    EqualizerController equalizerController(SettingsRepository settingsRepository,
                                            ExternalEqualizer externalEqualizer,
                                            InternalEqualizer internalEqualizer) {
        return new EqualizerController(settingsRepository, externalEqualizer, internalEqualizer);
    }

    @Provides
    @NonNull
    @Singleton
    ExternalEqualizer externalEqualizer(Context context) {
        return new ExternalEqualizer(context);
    }

    @Provides
    @NonNull
    @Singleton
    InternalEqualizer internalEqualizer(EqualizerStateRepository equalizerStateRepository,
                                        Analytics analytics) {
        return new InternalEqualizer(equalizerStateRepository, analytics);
    }

    @Provides
    @NonNull
    @Singleton
    EqualizerStateRepository equalizerStateRepository(Context context) {
        return new EqualizerStateRepository(context);
    }

    @Provides
    @Nonnull
    EqualizerPresenter equalizerPresenter(EqualizerInteractor equalizerInteractor,
                                          @Named(UI_SCHEDULER) Scheduler scheduler,
                                          ErrorParser errorParser) {
        return new EqualizerPresenter(equalizerInteractor, scheduler, errorParser);
    }

    @Provides
    @Nonnull
    EqualizerInteractor equalizerInteractor(EqualizerRepository equalizerRepository) {
        return new EqualizerInteractor(equalizerRepository);
    }

    @Provides
    @Nonnull
    EqualizerRepository equalizerRepository(InternalEqualizer internalEqualizer) {
        return new EqualizerRepositoryImpl(internalEqualizer);
    }

    @Provides
    @Nonnull
    @Singleton
    MediaSessionHandler mediaSessionHandler(Context context,
                                            PlayerInteractor playerInteractor,
                                            LibraryPlayerInteractor libraryPlayerInteractor,
                                            MusicServiceInteractor musicServiceInteractor,
                                            @Named(IO_SCHEDULER) Scheduler ioScheduler,
                                            @Named(UI_SCHEDULER) Scheduler uiScheduler,
                                            ErrorParser errorParser) {
        return new MediaSessionHandler(
                context,
                playerInteractor,
                libraryPlayerInteractor,
                musicServiceInteractor,
                ioScheduler,
                uiScheduler,
                errorParser
        );
    }

    @Provides
    @Nonnull
    LibraryCompositionsInteractor libraryCompositionsInteractor(LibraryRepository musicProviderRepository,
                                                                SettingsRepository settingsRepository,
                                                                UiStateRepository uiStateRepository) {
        return new LibraryCompositionsInteractor(musicProviderRepository,
                settingsRepository,
                uiStateRepository);
    }

    @Provides
    @Nonnull
    LibraryFoldersInteractor libraryFilesInteractor(LibraryRepository musicProviderRepository,
                                                    EditorRepository editorRepository,
                                                    LibraryPlayerInteractor musicPlayerInteractor,
                                                    PlayListsInteractor playListsInteractor,
                                                    SettingsRepository settingsRepository,
                                                    UiStateRepository uiStateRepository,
                                                    MediaScannerRepository mediaScannerRepository) {
        return new LibraryFoldersInteractor(musicProviderRepository,
                editorRepository,
                musicPlayerInteractor,
                playListsInteractor,
                settingsRepository,
                uiStateRepository,
                mediaScannerRepository);
    }

    @Provides
    @Nonnull
    LibraryArtistsInteractor libraryArtistsInteractor(LibraryRepository repository,
                                                      EditorRepository editorRepository,
                                                      SettingsRepository settingsRepository,
                                                      UiStateRepository uiStateRepository) {
        return new LibraryArtistsInteractor(repository,
                editorRepository,
                settingsRepository,
                uiStateRepository);
    }

    @Provides
    @Nonnull
    LibraryAlbumsInteractor libraryAlbumsInteractor(LibraryRepository repository,
                                                    EditorRepository editorRepository,
                                                    SettingsRepository settingsRepository,
                                                    UiStateRepository uiStateRepository) {
        return new LibraryAlbumsInteractor(repository,
                editorRepository,
                settingsRepository,
                uiStateRepository);
    }
}
