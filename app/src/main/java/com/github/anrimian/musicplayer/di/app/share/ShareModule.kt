package com.github.anrimian.musicplayer.di.app.share

import com.github.anrimian.filesync.SyncInteractor
import com.github.anrimian.musicplayer.di.app.SchedulerModule.UI_SCHEDULER
import com.github.anrimian.musicplayer.domain.interactors.player.CompositionSourceInteractor
import com.github.anrimian.musicplayer.ui.common.dialogs.share.ShareCompositionsPresenter
import com.github.anrimian.musicplayer.ui.common.error.parser.ErrorParser
import dagger.Module
import dagger.Provides
import io.reactivex.rxjava3.core.Scheduler
import javax.inject.Named

@Module
class ShareModule(private val ids: LongArray) {

    @Provides
    fun shareCompositionsPresenter(
        sourceInteractor: CompositionSourceInteractor,
        syncInteractor: SyncInteractor<*, *, Long>,
        @Named(UI_SCHEDULER) uiScheduler: Scheduler,
        errorParser: ErrorParser
    ) = ShareCompositionsPresenter(ids, sourceInteractor, syncInteractor, uiScheduler, errorParser)

}