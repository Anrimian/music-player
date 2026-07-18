package com.github.anrimian.musicplayer.wear.di.app

import android.content.Context
import com.github.anrimian.musicplayer.di.app.SchedulerModule.Companion.IO_SCHEDULER
import com.github.anrimian.musicplayer.di.app.SchedulerModule.Companion.UI_SCHEDULER
import com.github.anrimian.musicplayer.domain.interactors.analytics.Analytics
import com.github.anrimian.musicplayer.ui.common.error.parser.ErrorParser
import com.github.anrimian.musicplayer.wear.Constants
import com.github.anrimian.musicplayer.wear.data.WearStateRepository
import com.github.anrimian.musicplayer.wear.data.repositories.HostDeviceRepository
import com.github.anrimian.musicplayer.wear.domain.WearStateInteractor
import com.github.anrimian.musicplayer.wear.domain.controllers.RemoteStateController
import com.github.anrimian.musicplayer.wear.domain.queue.PlayQueueInteractor
import com.github.anrimian.musicplayer.wear.infrastructure.DeviceConnectionController
import com.github.anrimian.musicplayer.wear.infrastructure.analytics.WearableAnalytics
import com.github.anrimian.musicplayer.wear.infrastructure.remote.RemoteStateControllerImpl
import com.github.anrimian.musicplayer.wear.ui.MainPresenter
import com.github.anrimian.musicplayer.wear.ui.common.error.WearErrorParser
import com.github.anrimian.musicplayer.wear.utils.logger.WearableFileLogger
import dagger.Module
import dagger.Provides
import io.reactivex.rxjava3.core.Scheduler
import javax.inject.Named
import javax.inject.Singleton

@Module
class AppModule(private val context: Context) {

    @Provides
    fun context() = context

    @Provides
    @Singleton
    fun wearStateRepository(context: Context) = WearStateRepository(context)

    @Provides
    @Singleton
    fun remoteStateController(
        context: Context
    ): RemoteStateController = RemoteStateControllerImpl(context)

    @Provides
    @Singleton
    fun wearStateInteractor(
        wearStateRepository: WearStateRepository,
        deviceRepository: HostDeviceRepository,
        remoteStateController: RemoteStateController,
    ) = WearStateInteractor(
        wearStateRepository,
        deviceRepository,
        remoteStateController
    )

    @Provides
    @Singleton
    fun playQueueInteractor(
        hostDeviceRepository: HostDeviceRepository
    ) = PlayQueueInteractor(
        hostDeviceRepository
    )

    @Provides
    @Singleton
    fun hostDeviceRepository(
        deviceConnectionController: DeviceConnectionController,
        @Named(IO_SCHEDULER) ioScheduler: Scheduler,
    ) = HostDeviceRepository(
        deviceConnectionController,
        ioScheduler,
        Constants.EVENT_TIMEOUT_MILLIS
    )

    @Provides
    fun mainPresenter(
        wearStateInteractor: WearStateInteractor,
        playQueueInteractor: PlayQueueInteractor,
        @Named(UI_SCHEDULER) uiScheduler: Scheduler,
        errorParser: ErrorParser,
    ) = MainPresenter(
        wearStateInteractor,
        playQueueInteractor,
        uiScheduler,
        errorParser
    )

    @Provides
    @Singleton
    fun errorParser(
        context: Context,
        analytics: Analytics,
    ): ErrorParser = WearErrorParser(context, analytics)

    @Provides
    @Singleton
    fun deviceConnectionController(
        context: Context,
        @Named(IO_SCHEDULER) ioScheduler: Scheduler,
    ) = DeviceConnectionController(context, ioScheduler)

    @Provides
    @Singleton
    fun analytics(fileLog: WearableFileLogger): Analytics {
        return WearableAnalytics(fileLog)
    }

    @Provides
    @Singleton
    fun fileLog(): WearableFileLogger {
        return WearableFileLogger()
    }

}