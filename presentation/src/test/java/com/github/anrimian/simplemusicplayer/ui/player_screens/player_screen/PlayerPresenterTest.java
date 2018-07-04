package com.github.anrimian.simplemusicplayer.ui.player_screens.player_screen;

import com.github.anrimian.simplemusicplayer.domain.business.player.MusicPlayerInteractor;
import com.github.anrimian.simplemusicplayer.domain.models.composition.Composition;
import com.github.anrimian.simplemusicplayer.domain.models.player.PlayerState;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import io.reactivex.Scheduler;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created on 13.11.2017.
 */
public class PlayerPresenterTest {

    private MusicPlayerInteractor musicPlayerInteractor = mock(MusicPlayerInteractor.class);
    private Scheduler uiScheduler = Schedulers.single();
    private PlayerView playerView;

    private BehaviorSubject<PlayerState> playerStateSubject;
    private BehaviorSubject<Composition> currentCompositionSubject;
    private BehaviorSubject<List<Composition>> currentPlayListSubject;

    private PlayerPresenter playerPresenter;

    @Before
    public void setUp() {
        playerStateSubject = BehaviorSubject.createDefault(PlayerState.IDLE);
        currentCompositionSubject = BehaviorSubject.create();
        currentPlayListSubject = BehaviorSubject.create();

        when(musicPlayerInteractor.getCurrentCompositionObservable()).thenReturn(currentCompositionSubject);
        when(musicPlayerInteractor.getPlayQueueObservable()).thenReturn(currentPlayListSubject);
        when(musicPlayerInteractor.getPlayerStateObservable()).thenReturn(playerStateSubject);

        playerView = mock(PlayerView.class);

        playerPresenter = new PlayerPresenter(musicPlayerInteractor, uiScheduler);
        playerPresenter.attachView(playerView);
        playerPresenter.onStart();

        verify(playerView).showMusicControls(false);
        verify(playerView).bindPlayList(any());
    }

    @After
    public void tearDown() {
        playerPresenter.onStop();
        playerPresenter.destroyView(playerView);
    }

    @Test
    public void onPlayButtonClicked() {
        playerPresenter.onPlayButtonClicked();

        verify(musicPlayerInteractor).play();
    }

    @Test
    public void onPauseButtonClicked() {
        playerPresenter.onStopButtonClicked();

        verify(musicPlayerInteractor).pause();
    }

    @Test
    public void onSkipToPreviousButtonClicked() {
        playerPresenter.onSkipToPreviousButtonClicked();

        verify(musicPlayerInteractor).skipToPrevious();
    }

    @Test
    public void onSkipToNextButtonClicked() {
        playerPresenter.onSkipToNextButtonClicked();

        verify(musicPlayerInteractor).skipToNext();
    }

}