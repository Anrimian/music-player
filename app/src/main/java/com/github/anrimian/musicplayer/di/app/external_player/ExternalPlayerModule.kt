package com.github.anrimian.musicplayer.di.app.external_player

import com.github.anrimian.musicplayer.di.app.SchedulerModule
import com.github.anrimian.musicplayer.domain.interactors.player.ExternalPlayerInteractor
import com.github.anrimian.musicplayer.ui.common.error.parser.ErrorParser
import com.github.anrimian.musicplayer.ui.main.external_player.ExternalPlayerPresenter
import dagger.Module
import dagger.Provides
import io.reactivex.rxjava3.core.Scheduler
import javax.inject.Named

@Module
class ExternalPlayerModule {
    
    @Provides
    fun externalPlayerPresenter(
        interactor: ExternalPlayerInteractor,
        @Named(SchedulerModule.UI_SCHEDULER) uiScheduler: Scheduler,
        errorParser: ErrorParser
    ) = ExternalPlayerPresenter(interactor, uiScheduler, errorParser)
    
}
