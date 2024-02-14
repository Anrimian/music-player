package com.github.anrimian.musicplayer.di.app.library.genres;

import static com.github.anrimian.musicplayer.di.app.SchedulerModule.UI_SCHEDULER;

import com.github.anrimian.musicplayer.domain.interactors.library.LibraryGenresInteractor;
import com.github.anrimian.musicplayer.domain.interactors.player.LibraryPlayerInteractor;
import com.github.anrimian.musicplayer.domain.interactors.playlists.PlayListsInteractor;
import com.github.anrimian.musicplayer.ui.common.error.parser.ErrorParser;
import com.github.anrimian.musicplayer.ui.library.genres.list.GenresListPresenter;

import javax.annotation.Nonnull;
import javax.inject.Named;

import dagger.Module;
import dagger.Provides;
import io.reactivex.rxjava3.core.Scheduler;

@Module
public class GenresModule {

    @Provides
    @Nonnull
    GenresListPresenter genreListPresenter(LibraryGenresInteractor interactor,
                                           LibraryPlayerInteractor playerInteractor,
                                           PlayListsInteractor playListsInteractor,
                                           ErrorParser errorParser,
                                           @Named(UI_SCHEDULER) Scheduler uiScheduler) {
        return new GenresListPresenter(interactor,
                playerInteractor,
                playListsInteractor,
                errorParser,
                uiScheduler);
    }
}
