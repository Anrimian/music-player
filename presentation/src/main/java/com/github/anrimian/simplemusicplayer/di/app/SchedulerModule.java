package com.github.anrimian.simplemusicplayer.di.app;

import android.support.annotation.NonNull;

import javax.inject.Named;

import dagger.Module;
import dagger.Provides;
import io.reactivex.Scheduler;
import io.reactivex.schedulers.Schedulers;

/**
 * Created on 15.9.17. It is awesome java class.
 */

@Module
public class SchedulerModule {

    public static final String IO_SCHEDULER = "network_scheduler";

    @Provides
    @NonNull
    @Named(IO_SCHEDULER)
    Scheduler provideIOScheduler() {
        return Schedulers.io();
    }
}
