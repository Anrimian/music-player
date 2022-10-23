package com.github.anrimian.musicplayer.di.app;

import static com.github.anrimian.musicplayer.di.app.SchedulerModule.UI_SCHEDULER;

import android.content.Context;
import android.os.Build;

import androidx.annotation.NonNull;

import com.github.anrimian.musicplayer.data.repositories.logger.LoggerRepositoryImpl;
import com.github.anrimian.musicplayer.domain.controllers.SystemServiceController;
import com.github.anrimian.musicplayer.domain.interactors.analytics.Analytics;
import com.github.anrimian.musicplayer.domain.interactors.player.LibraryPlayerInteractor;
import com.github.anrimian.musicplayer.domain.interactors.settings.DisplaySettingsInteractor;
import com.github.anrimian.musicplayer.domain.interactors.sleep_timer.SleepTimerInteractor;
import com.github.anrimian.musicplayer.domain.repositories.LoggerRepository;
import com.github.anrimian.musicplayer.domain.repositories.SettingsRepository;
import com.github.anrimian.musicplayer.infrastructure.analytics.AnalyticsImpl;
import com.github.anrimian.musicplayer.infrastructure.service.SystemServiceControllerImpl;
import com.github.anrimian.musicplayer.ui.common.error.parser.ErrorParser;
import com.github.anrimian.musicplayer.ui.common.images.CoverImageLoader;
import com.github.anrimian.musicplayer.ui.common.locale.LocaleController;
import com.github.anrimian.musicplayer.ui.common.locale.LocaleControllerApi33;
import com.github.anrimian.musicplayer.ui.common.locale.LocaleControllerImpl;
import com.github.anrimian.musicplayer.ui.common.theme.ThemeController;
import com.github.anrimian.musicplayer.ui.notifications.MediaNotificationsDisplayer;
import com.github.anrimian.musicplayer.ui.notifications.NotificationDisplayerApi33;
import com.github.anrimian.musicplayer.ui.notifications.NotificationsDisplayer;
import com.github.anrimian.musicplayer.ui.notifications.NotificationsDisplayerImpl;
import com.github.anrimian.musicplayer.ui.notifications.builder.AppNotificationBuilder;
import com.github.anrimian.musicplayer.ui.sleep_timer.SleepTimerPresenter;
import com.github.anrimian.musicplayer.ui.widgets.WidgetUpdater;
import com.github.anrimian.musicplayer.utils.logger.AppLogger;
import com.github.anrimian.musicplayer.utils.logger.FileLog;

import javax.annotation.Nonnull;
import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import io.reactivex.rxjava3.core.Scheduler;

/**
 * Created on 11.02.2017.
 */

@Module
public class AppModule {

    private final Context appContext;

    public AppModule(Context appContext) {
        this.appContext = appContext;
    }

    @Provides
    @Nonnull
    Context appContext() {
        return appContext;
    }

    @Provides
    @Nonnull
    @Singleton
    MediaNotificationsDisplayer mediaNotificationsDisplayer(Context context,
                                                             AppNotificationBuilder notificationBuilder,
                                                             CoverImageLoader coverImageLoader) {
        return new MediaNotificationsDisplayer(context, notificationBuilder, coverImageLoader);
    }

    @Provides
    @Nonnull
    @Singleton
    NotificationsDisplayer notificationsDisplayer(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return new NotificationDisplayerApi33();
        }
        return new NotificationsDisplayerImpl(context);
    }

    @Provides
    @Nonnull
    @Singleton
    AppNotificationBuilder appNotificationBuilder() {
        return new AppNotificationBuilder();
    }

    @Provides
    @Nonnull
    @Singleton
    SystemServiceController systemServiceController(Context context) {
        return new SystemServiceControllerImpl(context);
    }

    @Provides
    @Nonnull
    @Singleton
    Analytics analytics(FileLog fileLog) {
        return new AnalyticsImpl(fileLog);
    }

    @Provides
    @Nonnull
    @Singleton
    FileLog fileLog(Context context) {
        return new FileLog(context);
    }

    @Provides
    @Nonnull
    @Singleton
    LoggerRepository loggerRepository(Context context) {
        return new LoggerRepositoryImpl(context);
    }

    @Provides
    @Nonnull
    @Singleton
    AppLogger appLogger(FileLog fileLog, LoggerRepository loggerRepository) {
        return new AppLogger(fileLog, loggerRepository);
    }

    @Provides
    @NonNull
    @Singleton
    WidgetUpdater widgetUpdater(Context context,
                                LibraryPlayerInteractor playerInteractor,
                                DisplaySettingsInteractor displaySettingsInteractor,
                                @Named(UI_SCHEDULER) Scheduler scheduler) {
        return new WidgetUpdater(context, playerInteractor, displaySettingsInteractor, scheduler);
    }

    @Provides
    @NonNull
    @Singleton
    ThemeController themeController(Context context) {
        return new ThemeController(context);
    }

    @Provides
    @NonNull
    @Singleton
    LocaleController localeController(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return new LocaleControllerApi33();
        }
        return new LocaleControllerImpl(context);
    }

    @Provides
    @NonNull
    @Singleton
    SleepTimerInteractor sleepTimerInteractor(LibraryPlayerInteractor libraryPlayerInteractor,
                                              SettingsRepository settingsRepository) {
        return new SleepTimerInteractor(libraryPlayerInteractor, settingsRepository);
    }

    @Provides
    @NonNull
    SleepTimerPresenter sleepTimerPresenter(SleepTimerInteractor sleepTimerInteractor,
                                            @Named(UI_SCHEDULER) Scheduler uiScheduler,
                                            ErrorParser errorParser) {
        return new SleepTimerPresenter(sleepTimerInteractor, uiScheduler, errorParser);
    }
}
