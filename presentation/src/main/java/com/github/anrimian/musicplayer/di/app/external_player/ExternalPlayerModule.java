package com.github.anrimian.musicplayer.di.app.external_player;

import com.github.anrimian.musicplayer.domain.interactors.player.PlayerCoordinatorInteractor;
import com.github.anrimian.musicplayer.domain.models.composition.source.CompositionSource;
import com.github.anrimian.musicplayer.ui.common.error.parser.ErrorParser;
import com.github.anrimian.musicplayer.ui.main.external_player.ExternalPlayerPresenter;

import javax.annotation.Nonnull;
import javax.inject.Named;

import dagger.Module;
import dagger.Provides;
import io.reactivex.Scheduler;

import static com.github.anrimian.musicplayer.di.app.SchedulerModule.UI_SCHEDULER;

@Module
public class ExternalPlayerModule {

    private final CompositionSource compositionSource;

    public ExternalPlayerModule(CompositionSource compositionSource) {
        this.compositionSource = compositionSource;
    }

    @Provides
    @Nonnull
    ExternalPlayerPresenter externalPlayerPresenter(PlayerCoordinatorInteractor interactor,
                                                    @Named(UI_SCHEDULER) Scheduler uiScheduler,
                                                    ErrorParser errorParser) {
        return new ExternalPlayerPresenter(compositionSource, interactor, uiScheduler, errorParser);
    }
}
