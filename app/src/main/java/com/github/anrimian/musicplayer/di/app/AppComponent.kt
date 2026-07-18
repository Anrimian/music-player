package com.github.anrimian.musicplayer.di.app

import com.github.anrimian.fsync.SyncInteractor
import com.github.anrimian.musicplayer.data.controllers.music.equalizer.EqualizerController
import com.github.anrimian.musicplayer.data.storage.files.StorageFilesDataSource
import com.github.anrimian.musicplayer.data.storage.providers.albums.StorageAlbumsProvider
import com.github.anrimian.musicplayer.data.storage.source.ContentSourceHelper
import com.github.anrimian.musicplayer.di.app.editor.album.AlbumEditorComponent
import com.github.anrimian.musicplayer.di.app.editor.album.AlbumEditorModule
import com.github.anrimian.musicplayer.di.app.editor.artist.ArtistEditorComponent
import com.github.anrimian.musicplayer.di.app.editor.artist.ArtistEditorModule
import com.github.anrimian.musicplayer.di.app.editor.composition.CompositionEditorComponent
import com.github.anrimian.musicplayer.di.app.editor.composition.CompositionEditorModule
import com.github.anrimian.musicplayer.di.app.editor.genre.GenreEditorComponent
import com.github.anrimian.musicplayer.di.app.editor.genre.GenreEditorModule
import com.github.anrimian.musicplayer.di.app.editor.lyrics.LyricsEditorComponent
import com.github.anrimian.musicplayer.di.app.editor.lyrics.LyricsEditorModule
import com.github.anrimian.musicplayer.di.app.external_player.ExternalPlayerComponent
import com.github.anrimian.musicplayer.di.app.external_player.ExternalPlayerModule
import com.github.anrimian.musicplayer.di.app.library.LibraryComponent
import com.github.anrimian.musicplayer.di.app.order.OrderComponent
import com.github.anrimian.musicplayer.di.app.order.OrderModule
import com.github.anrimian.musicplayer.di.app.play_list.PlayListComponent
import com.github.anrimian.musicplayer.di.app.play_list.PlayListModule
import com.github.anrimian.musicplayer.di.app.settings.SettingsComponent
import com.github.anrimian.musicplayer.di.app.share.ShareComponent
import com.github.anrimian.musicplayer.di.app.share.ShareModule
import com.github.anrimian.musicplayer.di.mvvm.MultiBindingViewModelFactory
import com.github.anrimian.musicplayer.domain.controllers.SystemMusicController
import com.github.anrimian.musicplayer.domain.controllers.SystemServiceController
import com.github.anrimian.musicplayer.domain.interactors.analytics.Analytics
import com.github.anrimian.musicplayer.domain.interactors.player.CompositionSourceInteractor
import com.github.anrimian.musicplayer.domain.interactors.player.LibraryPlayerInteractor
import com.github.anrimian.musicplayer.domain.interactors.player.MusicServiceInteractor
import com.github.anrimian.musicplayer.domain.interactors.player.PlayerInteractor
import com.github.anrimian.musicplayer.domain.interactors.settings.DisplaySettingsInteractor
import com.github.anrimian.musicplayer.domain.interactors.settings.LibrarySettingsInteractor
import com.github.anrimian.musicplayer.domain.interactors.storage.StorageScannerInteractor
import com.github.anrimian.musicplayer.domain.models.common.DeviceCapabilities
import com.github.anrimian.musicplayer.domain.models.sync.FileKey
import com.github.anrimian.musicplayer.domain.repositories.LoggerRepository
import com.github.anrimian.musicplayer.domain.repositories.SettingsRepository
import com.github.anrimian.musicplayer.domain.repositories.StorageSourceRepository
import com.github.anrimian.musicplayer.domain.repositories.UiStateRepository
import com.github.anrimian.musicplayer.infrastructure.MediaSessionHandler
import com.github.anrimian.musicplayer.infrastructure.service.wearable.WearableStateController
import com.github.anrimian.musicplayer.ui.about.AboutTextBinder
import com.github.anrimian.musicplayer.ui.common.dialogs.Dialogs
import com.github.anrimian.musicplayer.ui.common.dialogs.missing.MissingFilesPresenter
import com.github.anrimian.musicplayer.ui.common.dialogs.missing.actions.MissingFilesActionsBinder
import com.github.anrimian.musicplayer.ui.common.error.parser.ErrorParser
import com.github.anrimian.musicplayer.ui.common.format.MessageTextFormatter
import com.github.anrimian.musicplayer.ui.common.images.CoverImageLoader
import com.github.anrimian.musicplayer.ui.common.locale.LocaleController
import com.github.anrimian.musicplayer.ui.common.navigation.SpecialNavigation
import com.github.anrimian.musicplayer.ui.common.theme.ThemeController
import com.github.anrimian.musicplayer.ui.equalizer.EqualizerPresenter
import com.github.anrimian.musicplayer.ui.notifications.MediaNotificationsDisplayer
import com.github.anrimian.musicplayer.ui.notifications.NotificationsDisplayer
import com.github.anrimian.musicplayer.ui.player_screen.view.PlayerScreenBinder



import com.github.anrimian.musicplayer.ui.playlists.choose.ChoosePlayListPresenter
import com.github.anrimian.musicplayer.ui.playlists.create.CreatePlayListPresenter
import com.github.anrimian.musicplayer.ui.sleep_timer.SleepTimerPresenter
import com.github.anrimian.musicplayer.ui.widgets.WidgetUpdater
import com.github.anrimian.musicplayer.ui.widgets.menu.WidgetMenuPresenter
import com.github.anrimian.musicplayer.utils.logger.AppLogger
import com.github.anrimian.musicplayer.utils.logger.FileLog
import dagger.Subcomponent
import javax.inject.Singleton

@Singleton
@Subcomponent(
    modules = [
        AppModule::class,
        SchedulerModule::class,
        AppSchedulerModule::class,
        DispatcherModule::class,
        MusicModule::class,
        DbModule::class,
        StorageModule::class,
        LibraryModule::class,
        SettingsModule::class,
        PlayListsModule::class,
        ActionsModule::class
    ]
)
interface AppComponent {

    fun viewModelFactory(): MultiBindingViewModelFactory

    fun libraryComponent(): LibraryComponent
    fun playListComponent(module: PlayListModule): PlayListComponent
    fun settingsComponent(): SettingsComponent
    fun compositionEditorComponent(module: CompositionEditorModule): CompositionEditorComponent
    fun albumEditorComponent(module: AlbumEditorModule): AlbumEditorComponent
    fun artistEditorComponent(module: ArtistEditorModule): ArtistEditorComponent
    fun genreEditorComponent(module: GenreEditorModule): GenreEditorComponent
    fun lyricsEditorComponent(module: LyricsEditorModule): LyricsEditorComponent
    fun externalPlayerComponent(module: ExternalPlayerModule): ExternalPlayerComponent
    fun shareComponent(module: ShareModule): ShareComponent
    fun orderComponent(orderModule: OrderModule): OrderComponent

    fun libraryPlayerInteractor(): LibraryPlayerInteractor
    fun displaySettingsInteractor(): DisplaySettingsInteractor
    fun playerInteractor(): PlayerInteractor
    fun musicServiceInteractor(): MusicServiceInteractor
    fun librarySettingsInteractor(): LibrarySettingsInteractor
    fun sourceInteractor(): CompositionSourceInteractor
    fun syncInteractor(): SyncInteractor<FileKey, *, Long>

    fun createPlayListsPresenter(): CreatePlayListPresenter
    fun choosePlayListPresenter(): ChoosePlayListPresenter
    fun equalizerPresenter(): EqualizerPresenter
    fun sleepTimerPresenter(): SleepTimerPresenter
    fun widgetMenuPresenter(): WidgetMenuPresenter
    fun missingFilesPresenter(): MissingFilesPresenter

    fun uiStateRepository(): UiStateRepository
    fun settingsRepository(): SettingsRepository
    fun storageScannerInteractor(): StorageScannerInteractor
    fun storageSourceRepository(): StorageSourceRepository
    fun loggerRepository(): LoggerRepository
    fun storageAlbumsProvider(): StorageAlbumsProvider
    fun contentSourceHelper(): ContentSourceHelper

    fun mediaSessionHandler(): MediaSessionHandler
    fun imageLoader(): CoverImageLoader
    fun widgetUpdater(): WidgetUpdater
    fun mediaNotificationsDisplayer(): MediaNotificationsDisplayer
    fun notificationsDisplayer(): NotificationsDisplayer
    fun errorParser(): ErrorParser
    fun analytics(): Analytics
    fun fileLog(): FileLog
    fun appLogger(): AppLogger
    fun storageFilesDataSource(): StorageFilesDataSource
    fun wearableManager(): WearableStateController

    fun themeController(): ThemeController
    fun localeController(): LocaleController
    fun equalizerController(): EqualizerController
    fun systemMusicController(): SystemMusicController
    fun systemServiceController(): SystemServiceController

    fun specificNavigation(): SpecialNavigation
    fun playerScreenBinder(): PlayerScreenBinder
    fun aboutTextBinder(): AboutTextBinder

    fun messageTextFormatter(): MessageTextFormatter
    fun dialogs(): Dialogs

    fun missingFilesActionsBinder(): MissingFilesActionsBinder
    fun deviceCapabilities(): DeviceCapabilities
}