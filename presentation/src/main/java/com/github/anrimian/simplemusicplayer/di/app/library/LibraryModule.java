package com.github.anrimian.simplemusicplayer.di.app.library;

import com.github.anrimian.simplemusicplayer.domain.business.player.MusicPlayerInteractor;
import com.github.anrimian.simplemusicplayer.domain.business.playlists.PlayListsInteractor;
import com.github.anrimian.simplemusicplayer.ui.common.error.parser.ErrorParser;
import com.github.anrimian.simplemusicplayer.ui.player_screen.PlayerPresenter;

import javax.annotation.Nonnull;
import javax.inject.Named;

import dagger.Module;
import dagger.Provides;
import io.reactivex.Scheduler;

import static com.github.anrimian.simplemusicplayer.di.app.SchedulerModule.UI_SCHEDULER;

/**
 * Created on 29.10.2017.
 */
@Module
public class LibraryModule {

    @Provides
    @Nonnull
    PlayerPresenter playerPresenter(MusicPlayerInteractor musicPlayerInteractor,
                                    PlayListsInteractor playListsInteractor,
                                    ErrorParser errorParser,
                                    @Named(UI_SCHEDULER) Scheduler uiScheduler) {
        return new PlayerPresenter(musicPlayerInteractor,
                playListsInteractor,
                errorParser,
                uiScheduler);
    }
}
