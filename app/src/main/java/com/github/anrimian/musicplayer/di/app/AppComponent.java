package com.github.anrimian.musicplayer.di.app;


import com.github.anrimian.filesync.SyncInteractor;
import com.github.anrimian.musicplayer.data.controllers.music.equalizer.EqualizerController;
import com.github.anrimian.musicplayer.data.storage.files.StorageFilesDataSource;
import com.github.anrimian.musicplayer.data.storage.providers.albums.StorageAlbumsProvider;
import com.github.anrimian.musicplayer.data.storage.source.ContentSourceHelper;
import com.github.anrimian.musicplayer.di.app.editor.album.AlbumEditorComponent;
import com.github.anrimian.musicplayer.di.app.editor.album.AlbumEditorModule;
import com.github.anrimian.musicplayer.di.app.editor.artist.ArtistEditorComponent;
import com.github.anrimian.musicplayer.di.app.editor.artist.ArtistEditorModule;
import com.github.anrimian.musicplayer.di.app.editor.composition.CompositionEditorComponent;
import com.github.anrimian.musicplayer.di.app.editor.composition.CompositionEditorModule;
import com.github.anrimian.musicplayer.di.app.external_player.ExternalPlayerComponent;
import com.github.anrimian.musicplayer.di.app.external_player.ExternalPlayerModule;
import com.github.anrimian.musicplayer.di.app.library.LibraryComponent;
import com.github.anrimian.musicplayer.di.app.library.LibraryModule;
import com.github.anrimian.musicplayer.di.app.play_list.PlayListComponent;
import com.github.anrimian.musicplayer.di.app.play_list.PlayListModule;
import com.github.anrimian.musicplayer.di.app.settings.SettingsComponent;
import com.github.anrimian.musicplayer.di.app.share.ShareComponent;
import com.github.anrimian.musicplayer.di.app.share.ShareModule;
import com.github.anrimian.musicplayer.domain.controllers.SystemServiceController;
import com.github.anrimian.musicplayer.domain.interactors.analytics.Analytics;
import com.github.anrimian.musicplayer.domain.interactors.player.CompositionSourceInteractor;
import com.github.anrimian.musicplayer.domain.interactors.player.LibraryPlayerInteractor;
import com.github.anrimian.musicplayer.domain.interactors.player.MusicServiceInteractor;
import com.github.anrimian.musicplayer.domain.interactors.player.PlayerInteractor;
import com.github.anrimian.musicplayer.domain.interactors.settings.DisplaySettingsInteractor;
import com.github.anrimian.musicplayer.domain.interactors.settings.LibrarySettingsInteractor;
import com.github.anrimian.musicplayer.domain.repositories.LoggerRepository;
import com.github.anrimian.musicplayer.domain.repositories.MediaScannerRepository;
import com.github.anrimian.musicplayer.domain.repositories.StorageSourceRepository;
import com.github.anrimian.musicplayer.domain.repositories.UiStateRepository;
import com.github.anrimian.musicplayer.infrastructure.MediaSessionHandler;
import com.github.anrimian.musicplayer.ui.common.error.parser.ErrorParser;
import com.github.anrimian.musicplayer.ui.common.images.CoverImageLoader;
import com.github.anrimian.musicplayer.ui.common.locale.LocaleController;
import com.github.anrimian.musicplayer.ui.common.navigation.SpecialNavigation;
import com.github.anrimian.musicplayer.ui.common.theme.ThemeController;
import com.github.anrimian.musicplayer.ui.equalizer.EqualizerPresenter;
import com.github.anrimian.musicplayer.ui.notifications.MediaNotificationsDisplayer;
import com.github.anrimian.musicplayer.ui.notifications.NotificationsDisplayer;
import com.github.anrimian.musicplayer.ui.playlist_screens.choose.ChoosePlayListPresenter;
import com.github.anrimian.musicplayer.ui.playlist_screens.create.CreatePlayListPresenter;
import com.github.anrimian.musicplayer.ui.playlist_screens.playlists.PlayListsPresenter;
import com.github.anrimian.musicplayer.ui.sleep_timer.SleepTimerPresenter;
import com.github.anrimian.musicplayer.ui.widgets.WidgetUpdater;
import com.github.anrimian.musicplayer.utils.logger.AppLogger;
import com.github.anrimian.musicplayer.utils.logger.FileLog;

import javax.inject.Singleton;

import dagger.Subcomponent;

/**
 * Created on 11.02.2017.
 */

@Singleton
@Subcomponent(modules = {
        AppModule.class,
        SchedulerModule.class,
        MusicModule.class,
        DbModule.class,
        StorageModule.class,
        SettingsModule.class,
        PlayListsModule.class
})
public interface AppComponent {

    LibraryComponent libraryComponent(LibraryModule libraryModule);
    PlayListComponent playListComponent(PlayListModule module);
    SettingsComponent settingsComponent();
    CompositionEditorComponent compositionEditorComponent(CompositionEditorModule module);
    AlbumEditorComponent albumEditorComponent(AlbumEditorModule module);
    ArtistEditorComponent artistEditorComponent(ArtistEditorModule module);
    ExternalPlayerComponent externalPlayerComponent(ExternalPlayerModule module);
    ShareComponent shareComponent(ShareModule module);

    LibraryPlayerInteractor libraryPlayerInteractor();
    DisplaySettingsInteractor displaySettingsInteractor();
    PlayerInteractor playerInteractor();
    MusicServiceInteractor musicServiceInteractor();
    LibrarySettingsInteractor librarySettingsInteractor();
    CompositionSourceInteractor sourceInteractor();
    SyncInteractor<?, ?, Long> syncInteractor();

    PlayListsPresenter playListsPresenter();
    CreatePlayListPresenter createPlayListsPresenter();
    ChoosePlayListPresenter choosePlayListPresenter();
    EqualizerPresenter equalizerPresenter();
    SleepTimerPresenter sleepTimerPresenter();

    UiStateRepository uiStateRepository();
    MediaScannerRepository mediaScannerRepository();
    StorageSourceRepository storageSourceRepository();
    LoggerRepository loggerRepository();
    StorageAlbumsProvider storageAlbumsProvider();
    ContentSourceHelper contentSourceHelper();

    MediaSessionHandler mediaSessionHandler();
    CoverImageLoader imageLoader();
    WidgetUpdater widgetUpdater();
    MediaNotificationsDisplayer mediaNotificationsDisplayer();
    NotificationsDisplayer notificationsDisplayer();
    ErrorParser errorParser();
    Analytics analytics();
    FileLog fileLog();
    AppLogger appLogger();
    StorageFilesDataSource storageFilesDataSource();

    ThemeController themeController();
    LocaleController localeController();
    EqualizerController equalizerController();

    SystemServiceController systemServiceController();
    SpecialNavigation specificNavigation();
}