package com.github.anrimian.musicplayer.lite.di.app

import android.content.Context
import com.github.anrimian.filesync.SyncInteractor
import com.github.anrimian.filesync.stubs.StubSyncInteractor
import com.github.anrimian.musicplayer.data.controllers.music.error.PlayerErrorParserImpl
import com.github.anrimian.musicplayer.data.controllers.music.players.utils.ExoPlayerMediaItemBuilder
import com.github.anrimian.musicplayer.data.controllers.music.players.utils.MediaPlayerDataSourceBuilder
import com.github.anrimian.musicplayer.data.storage.providers.music.StorageMusicProvider
import com.github.anrimian.musicplayer.data.storage.source.ContentSourceHelper
import com.github.anrimian.musicplayer.domain.interactors.analytics.Analytics
import com.github.anrimian.musicplayer.domain.interactors.player.PlayerErrorParser
import com.github.anrimian.musicplayer.lite.ui.SpecialNavigationImpl
import com.github.anrimian.musicplayer.ui.common.error.parser.DefaultErrorParser
import com.github.anrimian.musicplayer.ui.common.error.parser.ErrorParser
import com.github.anrimian.musicplayer.ui.common.navigation.SpecialNavigation
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
    fun syncInteractor(): SyncInteractor<*, *, Long> = StubSyncInteractor<Any, Any, Long>()

    @Provides
    @Singleton
    fun exoPlayerMediaItemBuilder() = ExoPlayerMediaItemBuilder()

    @Provides
    @Singleton
    fun contentSourceUriBuilder(
        storageMusicProvider: StorageMusicProvider
    ) = ContentSourceHelper(storageMusicProvider)

    @Provides
    @Singleton
    fun mediaPlayerDataSourceBuilder(
        context: Context,
        storageMusicProvider: StorageMusicProvider
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

}