package com.github.anrimian.musicplayer.di.app.external_player;

import static com.github.anrimian.musicplayer.di.app.SchedulerModule.UI_SCHEDULER;

import com.github.anrimian.musicplayer.domain.interactors.player.ExternalPlayerInteractor;
import com.github.anrimian.musicplayer.ui.common.error.parser.ErrorParser;
import com.github.anrimian.musicplayer.ui.main.external_player.ExternalPlayerPresenter;

import javax.annotation.Nonnull;
import javax.inject.Named;

import dagger.Module;
import dagger.Provides;
import io.reactivex.rxjava3.core.Scheduler;

@Module
public class ExternalPlayerModule {

    @Provides
    @Nonnull
    ExternalPlayerPresenter externalPlayerPresenter(ExternalPlayerInteractor interactor,
                                                    @Named(UI_SCHEDULER) Scheduler uiScheduler,
                                                    ErrorParser errorParser) {
        return new ExternalPlayerPresenter(interactor, uiScheduler, errorParser);
    }
}
