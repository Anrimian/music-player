package com.github.anrimian.musicplayer.di.app

import dagger.Module
import dagger.Provides
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.schedulers.Schedulers
import javax.inject.Named
import javax.inject.Singleton

@Module
class SchedulerModule {

    @Provides
    @Named(IO_SCHEDULER)
    @Singleton
    fun provideIOScheduler(): Scheduler {
        return Schedulers.io()
    }

    @Provides
    @Named(UI_SCHEDULER)
    @Singleton
    fun provideUiScheduler(): Scheduler {
        return AndroidSchedulers.mainThread()
    }

    companion object {
        const val IO_SCHEDULER = "io_scheduler"
        const val UI_SCHEDULER = "ui_scheduler"
    }
}
