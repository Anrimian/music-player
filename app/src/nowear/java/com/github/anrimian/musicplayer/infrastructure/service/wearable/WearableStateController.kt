package com.github.anrimian.musicplayer.infrastructure.service.wearable

import android.content.Context
import com.github.anrimian.musicplayer.domain.controllers.SystemMusicController
import com.github.anrimian.musicplayer.domain.interactors.analytics.Analytics
import com.github.anrimian.musicplayer.domain.interactors.player.CommonPlayerInteractor
import com.github.anrimian.musicplayer.domain.interactors.player.LibraryPlayerInteractor
import com.github.anrimian.musicplayer.domain.interactors.player.PlayerInteractor
import com.github.anrimian.musicplayer.ui.common.error.parser.ErrorParser
import io.reactivex.rxjava3.core.Scheduler

@Suppress("unused")
class WearableStateController(
    context: Context,
    playerInteractor: PlayerInteractor,
    libraryPlayerInteractor: LibraryPlayerInteractor,
    commonPlayerInteractor: CommonPlayerInteractor,
    systemMusicController: SystemMusicController,
    private val analytics: Analytics,
    private val ioScheduler: Scheduler,
    errorParser: ErrorParser
) {

    fun init() {}

}