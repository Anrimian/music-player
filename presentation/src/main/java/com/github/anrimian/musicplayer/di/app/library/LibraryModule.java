package com.github.anrimian.musicplayer.di.app.library;

import androidx.annotation.NonNull;

import com.github.anrimian.musicplayer.domain.business.player.MusicPlayerInteractor;
import com.github.anrimian.musicplayer.domain.business.player.PlayerScreenInteractor;
import com.github.anrimian.musicplayer.domain.business.playlists.PlayListsInteractor;
import com.github.anrimian.musicplayer.domain.repositories.UiStateRepository;
import com.github.anrimian.musicplayer.ui.common.error.parser.ErrorParser;
import com.github.anrimian.musicplayer.ui.player_screen.PlayerPresenter;

import javax.annotation.Nonnull;
import javax.inject.Named;

import dagger.Module;
import dagger.Provides;
import io.reactivex.Scheduler;

import static com.github.anrimian.musicplayer.di.app.SchedulerModule.UI_SCHEDULER;

/**
 * Created on 29.10.2017.
 */
@Module
public class LibraryModule {

    @Provides
    @Nonnull
    PlayerPresenter playerPresenter(MusicPlayerInteractor musicPlayerInteractor,
                                    PlayListsInteractor playListsInteractor,
                                    PlayerScreenInteractor playerScreenInteractor,
                                    ErrorParser errorParser,
                                    @Named(UI_SCHEDULER) Scheduler uiScheduler) {
        return new PlayerPresenter(musicPlayerInteractor,
                playListsInteractor,
                playerScreenInteractor,
                errorParser,
                uiScheduler);
    }

    @Provides
    @NonNull
    PlayerScreenInteractor playerScreenInteractor(UiStateRepository uiStateRepository) {
        return new PlayerScreenInteractor(uiStateRepository);
    }
}
