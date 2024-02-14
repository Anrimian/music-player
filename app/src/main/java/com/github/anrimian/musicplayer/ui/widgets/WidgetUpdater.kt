package com.github.anrimian.musicplayer.ui.widgets

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import com.github.anrimian.musicplayer.data.utils.rx.retryWithDelay
import com.github.anrimian.musicplayer.domain.interactors.player.LibraryPlayerInteractor
import com.github.anrimian.musicplayer.domain.interactors.settings.DisplaySettingsInteractor
import com.github.anrimian.musicplayer.domain.models.play_queue.PlayQueueEvent
import com.github.anrimian.musicplayer.domain.models.player.PlayerState
import com.github.anrimian.musicplayer.domain.models.utils.CompositionHelper
import com.github.anrimian.musicplayer.ui.common.format.FormatUtils
import com.github.anrimian.musicplayer.ui.common.format.getRemoteViewPlayerState
import com.github.anrimian.musicplayer.ui.common.theme.ThemeController
import com.github.anrimian.musicplayer.ui.widgets.binders.MediumWidgetBinder
import com.github.anrimian.musicplayer.ui.widgets.binders.SmallExtWidgetBinder
import com.github.anrimian.musicplayer.ui.widgets.binders.SmallWidgetBinder
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.disposables.CompositeDisposable
import java.util.concurrent.TimeUnit


class WidgetUpdater(
    private val context: Context,
    private val musicPlayerInteractor: LibraryPlayerInteractor,
    private val displaySettingsInteractor: DisplaySettingsInteractor,
    private val themeController: ThemeController,
    private val uiScheduler: Scheduler,
) {

    private val widgetBinders = listOf(
        SmallWidgetBinder(),
        SmallExtWidgetBinder(),
        MediumWidgetBinder()
    )

    private val updateDisposable = CompositeDisposable()

    fun start() {
        if (updateDisposable.size() > 0) {
            return
        }
        updateDisposable.add(Observable.combineLatest(
            musicPlayerInteractor.getCurrentQueueItemObservable(),
            musicPlayerInteractor.getPlayQueueSizeObservable(),
            musicPlayerInteractor.getIsPlayingStateObservable(),
            musicPlayerInteractor.getPlayerStateObservable(),
            displaySettingsInteractor.getCoversEnabledObservable(),
            musicPlayerInteractor.getRepeatModeObservable(),
            musicPlayerInteractor.getRandomPlayingObservable(),
            themeController.getRoundCoversObservable(),
            this::applyWidgetState
        ).observeOn(uiScheduler)
            .retryWithDelay(10, 10, TimeUnit.SECONDS)
            .subscribe(this::onWidgetStateChanged))
    }

    private fun applyWidgetState(
        playQueueEvent: PlayQueueEvent,
        playQueueSize: Int,
        isPlaying: Boolean,
        playerState: PlayerState,
        isCoversEnabled: Boolean,
        repeatMode: Int,
        randomPlay: Boolean,
        isRoundCoversEnabled: Boolean
    ): Boolean {
        var compositionName: String? = null
        var compositionAuthor: String? = null
        var compositionId = 0L
        var coverModifyTime = 0L
        var compositionUpdateTime = 0L
        var compositionSize = 0L
        var isFileExists = false
        val item = playQueueEvent.playQueueItem
        if (item != null) {
            val composition = item.composition
            compositionName = CompositionHelper.formatCompositionName(composition)
            compositionAuthor = FormatUtils.formatCompositionAuthor(composition, context).toString()
            compositionId = composition.id
            compositionUpdateTime = composition.dateModified.time
            coverModifyTime = composition.coverModifyTime.time
            compositionSize = composition.size
            isFileExists = composition.isFileExists
        }

        val remotePlayerState = getRemoteViewPlayerState(isPlaying, playerState)
        return WidgetDataHolder.applyWidgetData(
            context,
            compositionName,
            compositionAuthor,
            compositionId,
            compositionUpdateTime,
            coverModifyTime,
            compositionSize,
            isFileExists,
            playQueueSize,
            remotePlayerState,
            randomPlay,
            repeatMode,
            isCoversEnabled,
            isRoundCoversEnabled
        )
    }

    private fun onWidgetStateChanged(changed: Boolean) {
        if (!changed) {
            return
        }
        val appWidgetManager = AppWidgetManager.getInstance(context)
        widgetBinders.forEach { widgetBinder ->
            appWidgetManager.updateAppWidget(
                ComponentName(context, widgetBinder.getWidgetProviderClass()),
                widgetBinder.getBoundRemoteViews(
                    context,
                    WidgetDataHolder.getWidgetColors(context),
                    WidgetDataHolder.getWidgetData(context)
                ))
        }
    }

}