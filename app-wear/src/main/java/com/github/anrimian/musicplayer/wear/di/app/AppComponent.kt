package com.github.anrimian.musicplayer.wear.di.app

import com.github.anrimian.musicplayer.di.app.SchedulerModule
import com.github.anrimian.musicplayer.wear.domain.WearStateInteractor
import com.github.anrimian.musicplayer.wear.domain.queue.PlayQueueInteractor
import com.github.anrimian.musicplayer.wear.ui.MainPresenter
import dagger.Subcomponent
import javax.inject.Singleton

@Singleton
@Subcomponent(modules = [
    AppModule::class,
    SchedulerModule::class
])
interface AppComponent {

    fun mainPresenter(): MainPresenter

    fun wearStateInteractor(): WearStateInteractor
    fun playQueueInteractor(): PlayQueueInteractor

}