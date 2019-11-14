package com.github.anrimian.musicplayer.di.app.library.genres;

import com.github.anrimian.musicplayer.domain.business.library.LibraryGenresInteractor;
import com.github.anrimian.musicplayer.domain.repositories.MusicProviderRepository;
import com.github.anrimian.musicplayer.ui.common.error.parser.ErrorParser;
import com.github.anrimian.musicplayer.ui.library.genres.list.GenresListPresenter;

import javax.annotation.Nonnull;
import javax.inject.Named;

import dagger.Module;
import dagger.Provides;
import io.reactivex.Scheduler;

import static com.github.anrimian.musicplayer.di.app.SchedulerModule.UI_SCHEDULER;

@Module
public class GenresModule {

    @Provides
    @Nonnull
    LibraryGenresInteractor libraryArtistsInteractor(MusicProviderRepository repository) {
        return new LibraryGenresInteractor(repository);
    }

    @Provides
    @Nonnull
    GenresListPresenter artistsListPresenter(LibraryGenresInteractor interactor,
                                             ErrorParser errorParser,
                                             @Named(UI_SCHEDULER) Scheduler uiScheduler) {
        return new GenresListPresenter(interactor, errorParser, uiScheduler);
    }
}
