package com.github.anrimian.musicplayer.di.app

import android.content.Context
import android.content.pm.ApplicationInfo
import android.os.Build
import androidx.lifecycle.ViewModel
import com.github.anrimian.fsync.SyncInteractor
import com.github.anrimian.musicplayer.data.repositories.logger.LoggerRepositoryImpl
import com.github.anrimian.musicplayer.di.mvvm.MultiBindingViewModelFactory
import com.github.anrimian.musicplayer.di.mvvm.ViewModelAssistedFactory
import com.github.anrimian.musicplayer.domain.controllers.SystemMusicController
import com.github.anrimian.musicplayer.domain.controllers.SystemServiceController
import com.github.anrimian.musicplayer.domain.interactors.analytics.Analytics
import com.github.anrimian.musicplayer.domain.interactors.library.MissingFilesInteractor
import com.github.anrimian.musicplayer.domain.interactors.player.CommonPlayerInteractor
import com.github.anrimian.musicplayer.domain.interactors.player.LibraryPlayerInteractor
import com.github.anrimian.musicplayer.domain.interactors.player.PlayerInteractor
import com.github.anrimian.musicplayer.domain.interactors.settings.DisplaySettingsInteractor
import com.github.anrimian.musicplayer.domain.interactors.sleep_timer.SleepTimerInteractor
import com.github.anrimian.musicplayer.domain.models.common.DeviceCapabilities
import com.github.anrimian.musicplayer.domain.models.sync.FileKey
import com.github.anrimian.musicplayer.domain.repositories.LibraryRepository
import com.github.anrimian.musicplayer.domain.repositories.LoggerRepository
import com.github.anrimian.musicplayer.domain.repositories.SettingsRepository
import com.github.anrimian.musicplayer.infrastructure.analytics.AnalyticsImpl
import com.github.anrimian.musicplayer.infrastructure.service.SystemServiceControllerImpl
import com.github.anrimian.musicplayer.infrastructure.service.wearable.WearableStateController
import com.github.anrimian.musicplayer.ui.common.dialogs.missing.MissingFilesPresenter
import com.github.anrimian.musicplayer.ui.common.error.parser.ErrorParser
import com.github.anrimian.musicplayer.ui.common.images.CoverImageLoader
import com.github.anrimian.musicplayer.ui.common.locale.LocaleController
import com.github.anrimian.musicplayer.ui.common.locale.LocaleControllerApi33
import com.github.anrimian.musicplayer.ui.common.locale.LocaleControllerImpl
import com.github.anrimian.musicplayer.ui.common.theme.ThemeController
import com.github.anrimian.musicplayer.ui.notifications.MediaNotificationsDisplayer
import com.github.anrimian.musicplayer.ui.notifications.MediaNotificationsDisplayerApi33
import com.github.anrimian.musicplayer.ui.notifications.MediaNotificationsDisplayerImpl
import com.github.anrimian.musicplayer.ui.notifications.NotificationDisplayerApi33
import com.github.anrimian.musicplayer.ui.notifications.NotificationsDisplayer
import com.github.anrimian.musicplayer.ui.notifications.NotificationsDisplayerImpl
import com.github.anrimian.musicplayer.ui.notifications.builder.AppNotificationBuilder
import com.github.anrimian.musicplayer.ui.sleep_timer.SleepTimerPresenter
import com.github.anrimian.musicplayer.ui.widgets.WidgetUpdater
import com.github.anrimian.musicplayer.ui.widgets.menu.WidgetMenuPresenter
import com.github.anrimian.musicplayer.utils.logger.AppLogger
import com.github.anrimian.musicplayer.utils.logger.FileLog
import dagger.Module
import dagger.Provides
import io.reactivex.rxjava3.core.Scheduler
import kotlinx.coroutines.CoroutineScope
import javax.inject.Named
import javax.inject.Provider
import javax.inject.Singleton

@Module
class AppModule(private val appContext: Context) {

    @Provides
    fun appContext(): Context = appContext

    @Provides
    @Singleton
    @Named(IS_DEBUG_FLAG)
    fun isDebug(
        context: Context
    ): Boolean = (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0

    @Provides
    @Singleton
    @Named(IS_QA_FLAG)
    fun isQa(
        context: Context
    ): Boolean = context.packageName.endsWith(".QA")

    @Provides
    @Singleton
    fun provideMultiBindingFactory(
        creators: MutableMap<Class<out ViewModel>, Provider<ViewModelAssistedFactory<*>>>
    ) = MultiBindingViewModelFactory(creators)

    @Provides
    @Singleton
    fun deviceCapabilities() = DeviceCapabilities(
        hasSystemDeleteFileDialog = Build.VERSION.SDK_INT >= Build.VERSION_CODES.R,
        isResizeablePopupsSupported = Build.VERSION.SDK_INT > Build.VERSION_CODES.P,
        isClipboardVisualConfirmationSupported = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU,
        isHardwareAcceleratedClippingSupported = Build.VERSION.SDK_INT > Build.VERSION_CODES.P
    )

    @Provides
    @Singleton
    fun mediaNotificationsDisplayer(
        context: Context,
        notificationBuilder: AppNotificationBuilder,
        coverImageLoader: CoverImageLoader
    ): MediaNotificationsDisplayer = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        MediaNotificationsDisplayerApi33(context, notificationBuilder)
    } else {
        MediaNotificationsDisplayerImpl(context, notificationBuilder, coverImageLoader)
    }

    @Provides
    @Singleton
    fun notificationsDisplayer(
        context: Context
    ): NotificationsDisplayer = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        NotificationDisplayerApi33(context)
    } else {
        NotificationsDisplayerImpl(context)
    }

    @Provides
    @Singleton
    fun appNotificationBuilder() = AppNotificationBuilder()

    @Provides
    @Singleton
    fun systemServiceController(
        context: Context,
        settingsRepository: SettingsRepository
    ): SystemServiceController = SystemServiceControllerImpl(context, settingsRepository)

    @Provides
    @Singleton
    fun analytics(
        fileLog: FileLog
    ): Analytics = AnalyticsImpl(fileLog)

    @Provides
    @Singleton
    fun fileLog(
        context: Context
    ) = FileLog(context)

    @Provides
    @Singleton
    fun loggerRepository(
        context: Context
    ): LoggerRepository = LoggerRepositoryImpl(context)

    @Provides
    @Singleton
    fun appLogger(
        fileLog: FileLog,
        loggerRepository: LoggerRepository
    ) = AppLogger(fileLog, loggerRepository)

    @Provides
    @Singleton
    fun widgetUpdater(
        context: Context,
        playerInteractor: LibraryPlayerInteractor,
        displaySettingsInteractor: DisplaySettingsInteractor,
        themeController: ThemeController,
        @Named(SchedulerModule.UI_SCHEDULER) scheduler: Scheduler
    ) = WidgetUpdater(
        context,
        playerInteractor,
        displaySettingsInteractor,
        themeController,
        scheduler
    )

    @Provides
    @Singleton
    fun themeController(
        context: Context
    ) = ThemeController(context)

    @Provides
    @Singleton
    fun localeController(
        context: Context
    ): LocaleController = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        LocaleControllerApi33()
    } else {
        LocaleControllerImpl(context)
    }

    @Provides
    @Singleton
    fun sleepTimerInteractor(
        libraryPlayerInteractor: LibraryPlayerInteractor,
        settingsRepository: SettingsRepository,
        @Named(DispatcherModule.APP_COMPUTATION_SCOPE) appComputationScope: CoroutineScope
    ) = SleepTimerInteractor(
        libraryPlayerInteractor,
        settingsRepository,
        appComputationScope
    )

    @Provides
    fun sleepTimerPresenter(
        sleepTimerInteractor: SleepTimerInteractor,
        @Named(SchedulerModule.UI_SCHEDULER) uiScheduler: Scheduler,
        errorParser: ErrorParser
    ) = SleepTimerPresenter(sleepTimerInteractor, uiScheduler, errorParser)

    @Provides
    fun widgetMenuPresenter(
        libraryPlayerInteractor: LibraryPlayerInteractor,
        @Named(SchedulerModule.UI_SCHEDULER) uiScheduler: Scheduler,
        errorParser: ErrorParser
    ) = WidgetMenuPresenter(libraryPlayerInteractor, uiScheduler, errorParser)

    @Provides
    @Singleton
    fun missingFilesInteractor(
        libraryRepository: LibraryRepository,
        syncInteractor: SyncInteractor<FileKey, *, Long>,
        @Named(DispatcherModule.APP_IO_SCOPE) appIoScope: CoroutineScope,
        analytics: Analytics
    ) = MissingFilesInteractor(libraryRepository, syncInteractor, appIoScope, analytics)

    @Provides
    fun missingFilesPresenter(
        missingFilesInteractor: MissingFilesInteractor,
        @Named(SchedulerModule.UI_SCHEDULER) uiScheduler: Scheduler,
        errorParser: ErrorParser
    ) = MissingFilesPresenter(missingFilesInteractor, uiScheduler, errorParser)

    @Provides
    @Singleton
    fun wearableStateController(
        context: Context,
        playerInteractor: PlayerInteractor,
        libraryPlayerInteractor: LibraryPlayerInteractor,
        commonPlayerInteractor: CommonPlayerInteractor,
        systemMusicController: SystemMusicController,
        analytics: Analytics,
        @Named(SchedulerModule.IO_SCHEDULER) ioScheduler: Scheduler,
        errorParser: ErrorParser
    ) = WearableStateController(
        context,
        playerInteractor,
        libraryPlayerInteractor,
        commonPlayerInteractor,
        systemMusicController,
        analytics,
        ioScheduler,
        errorParser
    )

    companion object {
        const val IS_DEBUG_FLAG = "is_debug"
        const val IS_QA_FLAG = "is_qa"
    }
}
