package com.github.anrimian.musicplayer.di.app.library.albums.items;

import com.github.anrimian.musicplayer.domain.business.library.LibraryAlbumsInteractor;
import com.github.anrimian.musicplayer.domain.business.player.MusicPlayerInteractor;
import com.github.anrimian.musicplayer.domain.business.playlists.PlayListsInteractor;
import com.github.anrimian.musicplayer.domain.business.settings.DisplaySettingsInteractor;
import com.github.anrimian.musicplayer.ui.common.error.parser.ErrorParser;
import com.github.anrimian.musicplayer.ui.library.albums.items.AlbumItemsPresenter;

import javax.annotation.Nonnull;
import javax.inject.Named;

import dagger.Module;
import dagger.Provides;
import io.reactivex.Scheduler;

import static com.github.anrimian.musicplayer.di.app.SchedulerModule.UI_SCHEDULER;

@Module
public class AlbumItemsModule {

    private long id;

    public AlbumItemsModule(long id) {
        this.id = id;
    }

    @Provides
    @Nonnull
    AlbumItemsPresenter genreItemsPresenter(LibraryAlbumsInteractor interactor,
                                            PlayListsInteractor playListsInteractor,
                                            MusicPlayerInteractor playerInteractor,
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
