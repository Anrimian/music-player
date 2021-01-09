package com.github.anrimian.musicplayer.di.app;

import android.content.Context;

import androidx.annotation.NonNull;

import com.github.anrimian.musicplayer.domain.controllers.SystemServiceController;
import com.github.anrimian.musicplayer.domain.interactors.analytics.Analytics;
import com.github.anrimian.musicplayer.domain.interactors.player.LibraryPlayerInteractor;
import com.github.anrimian.musicplayer.domain.interactors.player.SleepTimerInteractor;
import com.github.anrimian.musicplayer.domain.interactors.settings.DisplaySettingsInteractor;
import com.github.anrimian.musicplayer.domain.repositories.SettingsRepository;
import com.github.anrimian.musicplayer.domain.repositories.UiStateRepository;
import com.github.anrimian.musicplayer.infrastructure.analytics.AnalyticsImpl;
import com.github.anrimian.musicplayer.infrastructure.service.SystemServiceControllerImpl;
import com.github.anrimian.musicplayer.ui.common.error.parser.ErrorParser;
import com.github.anrimian.musicplayer.ui.common.images.CoverImageLoader;
import com.github.anrimian.musicplayer.ui.common.theme.ThemeController;
import com.github.anrimian.musicplayer.ui.notifications.NotificationsDisplayer;
import com.github.anrimian.musicplayer.ui.notifications.builder.AppNotificationBuilder;
import com.github.anrimian.musicplayer.ui.sleep_timer.SleepTimerPresenter;
import com.github.anrimian.musicplayer.ui.widgets.WidgetUpdater;
import com.github.anrimian.musicplayer.utils.filelog.FileLog;

import javax.annotation.Nonnull;
import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import io.reactivex.rxjava3.core.Scheduler;

import static com.github.anrimian.musicplayer.di.app.SchedulerModule.UI_SCHEDULER;

/**
 * Created on 11.02.2017.
 */

@Module
public class AppModule {

    private final Context appContext;

    public AppModule(@NonNull Context appContext) {
        this.appContext = appContext;
    }

    @Provides
    @NonNull
    Context provideAppContext() {
        return appContext;
    }

    @Provides
    @Nonnull
    @Singleton
    NotificationsDisplayer provideNotificationsController(Context context,
                                                          AppNotificationBuilder notificationBuilder,
                                                          CoverImageLoader coverImageLoader) {
        return new NotificationsDisplayer(context, notificationBuilder, coverImageLoader);
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
    SleepTimerInteractor sleepTimerInteractor(LibraryPlayerInteractor libraryPlayerInteractor,
                                              SettingsRepository settingsRepository,
                                              UiStateRepository uiStateRepository) {
        return new SleepTimerInteractor(libraryPlayerInteractor, settingsRepository, uiStateRepository);
    }

    @Provides
    @NonNull
    SleepTimerPresenter sleepTimerPresenter(SleepTimerInteractor sleepTimerInteractor,
                                            @Named(UI_SCHEDULER) Scheduler uiScheduler,
                                            ErrorParser errorParser) {
        return new SleepTimerPresenter(sleepTimerInteractor, uiScheduler, errorParser);
    }
}
