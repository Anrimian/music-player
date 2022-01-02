package com.github.anrimian.musicplayer.di.app.library.artists;

import com.github.anrimian.musicplayer.domain.interactors.library.LibraryArtistsInteractor;
import com.github.anrimian.musicplayer.ui.common.error.parser.ErrorParser;
import com.github.anrimian.musicplayer.ui.library.artists.list.ArtistsListPresenter;

import javax.annotation.Nonnull;
import javax.inject.Named;

import dagger.Module;
import dagger.Provides;
import io.reactivex.rxjava3.core.Scheduler;

import static com.github.anrimian.musicplayer.di.app.SchedulerModule.UI_SCHEDULER;

@Module
public class ArtistsModule {

    @Provides
    @Nonnull
    ArtistsListPresenter artistsListPresenter(LibraryArtistsInteractor interactor,
                                              ErrorParser errorParser,
                                              @Named(UI_SCHEDULER) Scheduler uiScheduler) {
        return new ArtistsListPresenter(interactor, errorParser, uiScheduler);
    }
}
