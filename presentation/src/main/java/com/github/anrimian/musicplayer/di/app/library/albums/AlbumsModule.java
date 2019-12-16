package com.github.anrimian.musicplayer.di.app.library.albums;

import com.github.anrimian.musicplayer.domain.business.library.LibraryAlbumsInteractor;
import com.github.anrimian.musicplayer.domain.repositories.EditorRepository;
import com.github.anrimian.musicplayer.domain.repositories.MusicProviderRepository;
import com.github.anrimian.musicplayer.domain.repositories.SettingsRepository;
import com.github.anrimian.musicplayer.ui.common.error.parser.ErrorParser;
import com.github.anrimian.musicplayer.ui.library.albums.list.AlbumsListPresenter;

import javax.annotation.Nonnull;
import javax.inject.Named;

import dagger.Module;
import dagger.Provides;
import io.reactivex.Scheduler;

import static com.github.anrimian.musicplayer.di.app.SchedulerModule.UI_SCHEDULER;

@Module
public class AlbumsModule {

    @Provides
    @Nonnull
    LibraryAlbumsInteractor libraryArtistsInteractor(MusicProviderRepository repository,
                                                     EditorRepository editorRepository,
                                                     SettingsRepository settingsRepository) {
        return new LibraryAlbumsInteractor(repository, editorRepository, settingsRepository);
    }

    @Provides
    @Nonnull
    AlbumsListPresenter artistsListPresenter(LibraryAlbumsInteractor interactor,
                                             ErrorParser errorParser,
                                             @Named(UI_SCHEDULER) Scheduler uiScheduler) {
        return new AlbumsListPresenter(interactor, errorParser, uiScheduler);
    }
}
