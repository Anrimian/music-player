package com.github.anrimian.simplemusicplayer.di.app.play_list;

import com.github.anrimian.simplemusicplayer.domain.business.player.MusicPlayerInteractor;
import com.github.anrimian.simplemusicplayer.domain.business.playlists.PlayListsInteractor;
import com.github.anrimian.simplemusicplayer.domain.models.playlist.PlayList;
import com.github.anrimian.simplemusicplayer.ui.playlist_screens.playlist.PlayListPresenter;

import javax.annotation.Nonnull;
import javax.inject.Named;

import dagger.Module;
import dagger.Provides;
import io.reactivex.Scheduler;

import static com.github.anrimian.simplemusicplayer.di.app.SchedulerModule.UI_SCHEDULER;

@Module
public class PlayListModule {

    private final long playListId;

    public PlayListModule(long playListId) {
        this.playListId = playListId;
    }

    @Provides
    @Nonnull
    PlayListPresenter playListsPresenter(MusicPlayerInteractor musicPlayerInteractor,
                                         PlayListsInteractor playListsInteractor,
                                         @Named(UI_SCHEDULER) Scheduler uiSchedule) {
        return new PlayListPresenter(playListId,
                musicPlayerInteractor,
                playListsInteractor,
                uiSchedule);
    }
}
