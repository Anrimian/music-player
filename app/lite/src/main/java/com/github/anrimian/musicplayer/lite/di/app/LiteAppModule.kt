package com.github.anrimian.musicplayer.lite.di.app

import android.content.Context
import com.github.anrimian.filesync.SyncInteractor
import com.github.anrimian.filesync.stubs.StubSyncInteractor
import com.github.anrimian.musicplayer.data.controllers.music.error.PlayerErrorParserImpl
import com.github.anrimian.musicplayer.data.controllers.music.players.utils.ExoPlayerMediaItemBuilder
import com.github.anrimian.musicplayer.data.controllers.music.players.utils.MediaPlayerDataSourceBuilder
import com.github.anrimian.musicplayer.data.storage.providers.music.StorageMusicProvider
import com.github.anrimian.musicplayer.data.storage.source.ContentSourceHelper
import com.github.anrimian.musicplayer.domain.controllers.SystemMusicController
import com.github.anrimian.musicplayer.domain.interactors.analytics.Analytics
import com.github.anrimian.musicplayer.domain.interactors.player.LibraryPlayerInteractor
import com.github.anrimian.musicplayer.domain.interactors.player.PlayerErrorParser
import com.github.anrimian.musicplayer.domain.interactors.player.PlayerScreenInteractor
import com.github.anrimian.musicplayer.domain.interactors.sleep_timer.SleepTimerInteractor
import com.github.anrimian.musicplayer.domain.models.sync.FileKey
import com.github.anrimian.musicplayer.domain.repositories.MediaScannerRepository
import com.github.anrimian.musicplayer.domain.repositories.PlayQueueRepository
import com.github.anrimian.musicplayer.domain.repositories.SettingsRepository
import com.github.anrimian.musicplayer.domain.repositories.UiStateRepository
import com.github.anrimian.musicplayer.lite.ui.AboutTextBinderImpl
import com.github.anrimian.musicplayer.lite.ui.ActionStateBinderImpl
import com.github.anrimian.musicplayer.lite.ui.SpecialNavigationImpl
import com.github.anrimian.musicplayer.ui.about.AboutTextBinder
import com.github.anrimian.musicplayer.ui.common.error.parser.DefaultErrorParser
import com.github.anrimian.musicplayer.ui.common.error.parser.ErrorParser
import com.github.anrimian.musicplayer.ui.common.format.MessageTextFormatter
import com.github.anrimian.musicplayer.ui.common.navigation.SpecialNavigation
import com.github.anrimian.musicplayer.ui.player_screen.view.ActionStateBinder
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class LiteAppModule {

    @Provides
    @Singleton
    fun navigation(): SpecialNavigation = SpecialNavigationImpl()

    @Provides
    @Singleton
    fun syncInteractor(): SyncInteractor<FileKey, *, Long> = StubSyncInteractor<FileKey, Any, Long>()

    @Provides
    @Singleton
    fun exoPlayerMediaItemBuilder() = ExoPlayerMediaItemBuilder()

    @Provides
    @Singleton
    fun contentSourceUriBuilder(
        storageMusicProvider: StorageMusicProvider,
    ) = ContentSourceHelper(storageMusicProvider)

    @Provides
    @Singleton
    fun mediaPlayerDataSourceBuilder(
        context: Context,
        storageMusicProvider: StorageMusicProvider,
    ) = MediaPlayerDataSourceBuilder(
        context,
        storageMusicProvider
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
    fun actionStateBinder(): ActionStateBinder = ActionStateBinderImpl()

    @Provides
    fun playerScreenInteractor(
        sleepTimerInteractor: SleepTimerInteractor,
        libraryPlayerInteractor: LibraryPlayerInteractor,
        syncInteractor: SyncInteractor<FileKey, *, Long>,
        playQueueRepository: PlayQueueRepository,
        uiStateRepository: UiStateRepository,
        settingsRepository: SettingsRepository,
        mediaScannerRepository: MediaScannerRepository,
        systemMusicController: SystemMusicController,
    ) = PlayerScreenInteractor(
        sleepTimerInteractor,
        libraryPlayerInteractor,
        syncInteractor,
        playQueueRepository,
        uiStateRepository,
        settingsRepository,
        mediaScannerRepository,
        systemMusicController
    )

    @Provides
    fun messageTextFormatter() = MessageTextFormatter()

}