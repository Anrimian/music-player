package com.github.anrimian.simplemusicplayer.ui.library.main;

import com.github.anrimian.simplemusicplayer.domain.business.player.MusicPlayerInteractor;
import com.github.anrimian.simplemusicplayer.domain.models.Composition;
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
public class LibraryPresenterTest {

    private MusicPlayerInteractor musicPlayerInteractor;
    private Scheduler uiScheduler = Schedulers.single();
    private LibraryView libraryView;

    private BehaviorSubject<PlayerState> playerStateSubject;
    private BehaviorSubject<Composition> currentCompositionSubject;
    private BehaviorSubject<List<Composition>> currentPlayListSubject;

    private LibraryPresenter libraryPresenter;

    @Before
    public void setUp() throws Exception {
        playerStateSubject = BehaviorSubject.createDefault(PlayerState.IDLE);
        currentCompositionSubject = BehaviorSubject.create();
        currentPlayListSubject = BehaviorSubject.create();

        musicPlayerInteractor = mock(MusicPlayerInteractor.class);
        when(musicPlayerInteractor.getCurrentCompositionObservable()).thenReturn(currentCompositionSubject);
        when(musicPlayerInteractor.getCurrentPlayListObservable()).thenReturn(currentPlayListSubject);
        when(musicPlayerInteractor.getPlayerStateObservable()).thenReturn(playerStateSubject);

        libraryView = mock(LibraryView.class);

        libraryPresenter = new LibraryPresenter(musicPlayerInteractor, uiScheduler);
        libraryPresenter.attachView(libraryView);

        verify(libraryView).hideMusicControls();
        verify(libraryView).bindPlayList(any());
    }

    @After
    public void tearDown() throws Exception {
        libraryPresenter.detachView(libraryView);
    }

    @Test
    public void onPlayButtonClicked() {
        libraryPresenter.onPlayButtonClicked();

        verify(musicPlayerInteractor).play();
    }

    @Test
    public void onPauseButtonClicked() {
        libraryPresenter.onStopButtonClicked();

        verify(musicPlayerInteractor).stop();
    }

    @Test
    public void onSkipToPreviousButtonClicked() {
        libraryPresenter.onSkipToPreviousButtonClicked();

        verify(musicPlayerInteractor).skipToPrevious();
    }

    @Test
    public void onSkipToNextButtonClicked() {
        libraryPresenter.onSkipToNextButtonClicked();

        verify(musicPlayerInteractor).skipToNext();
    }

}