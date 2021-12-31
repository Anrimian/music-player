package com.github.anrimian.musicplayer.di.app;

import androidx.annotation.NonNull;

import java.util.concurrent.Executors;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * Created on 15.9.17. It is awesome java class.
 */

@Module
public class SchedulerModule {

    public static final String IO_SCHEDULER = "io_scheduler";
    public static final String UI_SCHEDULER = "ui_scheduler";
    public static final String DB_SCHEDULER = "db_scheduler";
    public static final String SLOW_BG_SCHEDULER = "slow_bg_scheduler";

    @Provides
    @NonNull
    @Named(IO_SCHEDULER)
    @Singleton
    Scheduler provideIOScheduler() {
        return Schedulers.io();
    }

    @Provides
    @NonNull
    @Named(UI_SCHEDULER)
    @Singleton
    Scheduler provideUiScheduler() {
        return AndroidSchedulers.mainThread();
    }

    @Provides
    @NonNull
    @Named(DB_SCHEDULER)
    @Singleton
    Scheduler provideDBScheduler() {
        return Schedulers.from(Executors.newSingleThreadExecutor());
    }

    @Provides
    @NonNull
    @Named(SLOW_BG_SCHEDULER)
    @Singleton
    Scheduler provideSlowBgScheduler() {
        return Schedulers.from(Executors.newSingleThreadExecutor());
    }
}
