package com.github.anrimian.musicplayer.lite.di.app

import android.content.Context
import com.github.anrimian.fsync.SyncInteractor
import com.github.anrimian.fsync.stubs.StubSyncInteractor
import com.github.anrimian.musicplayer.data.controllers.music.error.PlayerErrorParserImpl
import com.github.anrimian.musicplayer.data.controllers.music.players.utils.ExoPlayerMediaItemBuilder
import com.github.anrimian.musicplayer.data.controllers.music.players.utils.MediaPlayerDataSourceBuilder
import com.github.anrimian.musicplayer.data.storage.providers.music.SystemAudioCatalogProvider
import com.github.anrimian.musicplayer.data.storage.source.ContentSourceHelper
import com.github.anrimian.musicplayer.di.config.AppSetupConfig
import com.github.anrimian.musicplayer.domain.controllers.SystemMusicController
import com.github.anrimian.musicplayer.domain.interactors.analytics.Analytics
import com.github.anrimian.musicplayer.domain.interactors.player.LibraryPlayerInteractor
import com.github.anrimian.musicplayer.domain.interactors.player.PlayerErrorParser
import com.github.anrimian.musicplayer.domain.interactors.player.screen.PlayerScreenInteractor
import com.github.anrimian.musicplayer.domain.interactors.sleep_timer.SleepTimerInteractor
import com.github.anrimian.musicplayer.domain.interactors.storage.StorageScannerInteractor
import com.github.anrimian.musicplayer.domain.models.sync.FileKey
import com.github.anrimian.musicplayer.domain.repositories.LibraryRepository
import com.github.anrimian.musicplayer.domain.repositories.PlayQueueRepository
import com.github.anrimian.musicplayer.domain.repositories.SettingsRepository
import com.github.anrimian.musicplayer.domain.repositories.UiStateRepository
import com.github.anrimian.musicplayer.lite.ui.AboutTextBinderImpl
import com.github.anrimian.musicplayer.lite.ui.SpecialNavigationImpl
import com.github.anrimian.musicplayer.ui.about.AboutTextBinder
import com.github.anrimian.musicplayer.ui.common.dialogs.Dialogs
import com.github.anrimian.musicplayer.ui.common.error.parser.DefaultErrorParser
import com.github.anrimian.musicplayer.ui.common.error.parser.ErrorParser
import com.github.anrimian.musicplayer.ui.common.format.MessageTextFormatter
import com.github.anrimian.musicplayer.ui.common.navigation.SpecialNavigation
import com.github.anrimian.musicplayer.ui.player_screen.view.PlayerScreenBinder


import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class LiteAppModule {

    @Provides
    @Singleton
    fun navigation(): SpecialNavigation = SpecialNavigationImpl()

    @Provides
    fun playerScreenBinder(): PlayerScreenBinder = PlayerScreenBinder()

    @Provides
    @Singleton
    fun syncInteractor(): SyncInteractor<FileKey, *, Long> = StubSyncInteractor<FileKey, Any, Long>()

    @Provides
    @Singleton
    fun exoPlayerMediaItemBuilder() = ExoPlayerMediaItemBuilder()

    @Provides
    @Singleton
    fun contentSourceUriBuilder(
        systemAudioCatalogProvider: SystemAudioCatalogProvider,
    ) = ContentSourceHelper(systemAudioCatalogProvider)

    @Provides
    @Singleton
    fun mediaPlayerDataSourceBuilder(
        context: Context,
    ) = MediaPlayerDataSourceBuilder(
        context
    )

    @Provides
    @Singleton
    fun playerErrorParser(analytics: Analytics): PlayerErrorParser {
        return PlayerErrorParserImpl(analytics)
    }

    @Provides
    @Singleton
    fun provideErrorParser(context: Context, analytics: Analytics): ErrorParser {
        return DefaultErrorParser(context, analytics)
    }

    @Provides
    fun aboutTextBinder(): AboutTextBinder = AboutTextBinderImpl()

    @Provides
    fun playerScreenInteractor(
        sleepTimerInteractor: SleepTimerInteractor,
        libraryPlayerInteractor: LibraryPlayerInteractor,
        syncInteractor: SyncInteractor<FileKey, *, Long>,
        playQueueRepository: PlayQueueRepository,
        uiStateRepository: UiStateRepository,
        settingsRepository: SettingsRepository,
        storageScannerInteractor: StorageScannerInteractor,
        libraryRepository: LibraryRepository,
        systemMusicController: SystemMusicController,
    ) = PlayerScreenInteractor(
        sleepTimerInteractor,
        libraryPlayerInteractor,
        syncInteractor,
        playQueueRepository,
        uiStateRepository,
        settingsRepository,
        storageScannerInteractor,
        libraryRepository,
        systemMusicController
    )

    @Provides
    fun messageTextFormatter() = MessageTextFormatter()

    @Provides
    fun dialogs() = Dialogs()

    @Provides
    @Singleton
    fun appSetupConfig() = AppSetupConfig(
        isPathChangeForNonExistentFilesAllowed = false
    )

}