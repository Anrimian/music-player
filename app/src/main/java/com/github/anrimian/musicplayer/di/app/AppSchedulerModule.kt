package com.github.anrimian.musicplayer.di.app

import dagger.Module
import dagger.Provides
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.concurrent.Executors
import javax.inject.Named
import javax.inject.Singleton

@Module
class AppSchedulerModule {

    @Provides
    @Named(COMPUTATION_SCHEDULER)
    @Singleton
    fun provideComputationScheduler(): Scheduler = Schedulers.computation()

    @Provides
    @Named(DB_SCHEDULER)
    @Singleton
    fun provideDBScheduler(): Scheduler = Schedulers.from(Executors.newSingleThreadExecutor())

    @Provides
    @Named(SLOW_BG_SCHEDULER)
    @Singleton
    fun provideSlowBgScheduler(): Scheduler = Schedulers.from(Executors.newSingleThreadExecutor())

    companion object {
        const val COMPUTATION_SCHEDULER = "computation_scheduler"

        //replace db scheduler with io scheduler. Check sequential write operations
        //+play queue skipTo
        const val DB_SCHEDULER = "db_scheduler"
        const val SLOW_BG_SCHEDULER = "slow_bg_scheduler"
    }

}
