package com.github.anrimian.musicplayer.di.app.library.genres.items;

import com.github.anrimian.musicplayer.domain.business.library.LibraryGenresInteractor;
import com.github.anrimian.musicplayer.ui.common.error.parser.ErrorParser;
import com.github.anrimian.musicplayer.ui.library.genres.items.GenreItemsPresenter;

import javax.annotation.Nonnull;
import javax.inject.Named;

import dagger.Module;
import dagger.Provides;
import io.reactivex.Scheduler;

import static com.github.anrimian.musicplayer.di.app.SchedulerModule.UI_SCHEDULER;

@Module
public class GenreItemsModule {

    private long id;

    public GenreItemsModule(long id) {
        this.id = id;
    }

    @Provides
    @Nonnull
    GenreItemsPresenter genreItemsPresenter(LibraryGenresInteractor interactor,
                                            ErrorParser errorParser,
                                            @Named(UI_SCHEDULER) Scheduler uiScheduler) {
        return new GenreItemsPresenter(id, interactor, errorParser, uiScheduler);
    }
}
