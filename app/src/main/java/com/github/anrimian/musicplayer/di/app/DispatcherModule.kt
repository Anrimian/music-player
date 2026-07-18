package com.github.anrimian.musicplayer.di.app

import dagger.Module
import dagger.Provides
import io.reactivex.rxjava3.core.Scheduler
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.rx3.asCoroutineDispatcher
import javax.inject.Named
import javax.inject.Singleton

@Module
class DispatcherModule {

    companion object {
        const val IO_DISPATCHER = "io_dispatcher"
        const val UI_DISPATCHER_COROUTINES = "ui_dispatcher_coroutines"
        const val COMPUTATION_DISPATCHER = "computation_dispatcher"
        const val DB_DISPATCHER = "db_dispatcher"
        const val SLOW_BG_DISPATCHER = "slow_bg_dispatcher"

        const val APP_IO_SCOPE = "app_io_scope"
        const val APP_COMPUTATION_SCOPE = "app_computation_scope"
    }


    @Provides
    @Named(UI_DISPATCHER_COROUTINES)
    @Singleton
    fun provideUiCoroutineDispatcher(): CoroutineDispatcher = Dispatchers.Main

    @Provides
    @Named(IO_DISPATCHER)
    @Singleton
    fun provideIoCoroutineDispatcher(
        @Named(SchedulerModule.IO_SCHEDULER) ioScheduler: Scheduler
    ): CoroutineDispatcher = ioScheduler.asCoroutineDispatcher()

    @Provides
    @Named(COMPUTATION_DISPATCHER)
    @Singleton
    fun provideComputationCoroutineDispatcher(
        @Named(AppSchedulerModule.COMPUTATION_SCHEDULER) computationScheduler: Scheduler
    ): CoroutineDispatcher = computationScheduler.asCoroutineDispatcher()

    @Provides
    @Named(DB_DISPATCHER)
    @Singleton
    fun provideDbCoroutineDispatcher(
        @Named(AppSchedulerModule.DB_SCHEDULER) dbScheduler: Scheduler
    ): CoroutineDispatcher = dbScheduler.asCoroutineDispatcher()

    @Provides
    @Named(SLOW_BG_DISPATCHER)
    @Singleton
    fun provideSlowBgCoroutineDispatcher(
        @Named(AppSchedulerModule.SLOW_BG_SCHEDULER) slowBgScheduler: Scheduler
    ): CoroutineDispatcher = slowBgScheduler.asCoroutineDispatcher()

    @Provides
    @Named(APP_IO_SCOPE)
    @Singleton
    fun provideAppIoScope(
        @Named(IO_DISPATCHER) ioDispatcher: CoroutineDispatcher
    ): CoroutineScope = CoroutineScope(SupervisorJob() + ioDispatcher)

    @Provides
    @Named(APP_COMPUTATION_SCOPE)
    @Singleton
    fun provideAppComputationScope(
        @Named(COMPUTATION_DISPATCHER) computationDispatcher: CoroutineDispatcher
    ): CoroutineScope = CoroutineScope(SupervisorJob() + computationDispatcher)
}