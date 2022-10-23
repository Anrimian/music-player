package com.github.anrimian.musicplayer.di.app.library.albums.items;

import static com.github.anrimian.musicplayer.di.app.SchedulerModule.UI_SCHEDULER;

import com.github.anrimian.musicplayer.domain.interactors.library.LibraryAlbumsInteractor;
import com.github.anrimian.musicplayer.domain.interactors.player.LibraryPlayerInteractor;
import com.github.anrimian.musicplayer.domain.interactors.playlists.PlayListsInteractor;
import com.github.anrimian.musicplayer.domain.interactors.settings.DisplaySettingsInteractor;
import com.github.anrimian.musicplayer.ui.common.error.parser.ErrorParser;
import com.github.anrimian.musicplayer.ui.library.albums.items.AlbumItemsPresenter;

import javax.annotation.Nonnull;
import javax.inject.Named;

import dagger.Module;
import dagger.Provides;
import io.reactivex.rxjava3.core.Scheduler;

@Module
public class AlbumItemsModule {

    private final long id;

    public AlbumItemsModule(long id) {
        this.id = id;
    }

    @Provides
    @Nonnull
    AlbumItemsPresenter genreItemsPresenter(LibraryAlbumsInteractor interactor,
                                            PlayListsInteractor playListsInteractor,
                                            LibraryPlayerInteractor playerInteractor,
                                            DisplaySettingsInteractor displaySettingsInteractor,
                                            ErrorParser errorParser,
                                            @Named(UI_SCHEDULER) Scheduler uiScheduler) {
        return new AlbumItemsPresenter(id,
                interactor,
                playListsInteractor,
                playerInteractor,
                displaySettingsInteractor,
                errorParser,
                uiScheduler);
    }
}
