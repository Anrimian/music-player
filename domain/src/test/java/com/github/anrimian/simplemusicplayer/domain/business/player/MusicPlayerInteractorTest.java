package com.github.anrimian.simplemusicplayer.domain.business.player;

import com.github.anrimian.simplemusicplayer.domain.controllers.MusicPlayerController;
import com.github.anrimian.simplemusicplayer.domain.controllers.MusicServiceController;
import com.github.anrimian.simplemusicplayer.domain.models.Composition;
import com.github.anrimian.simplemusicplayer.domain.models.player.InternalPlayerState;
import com.github.anrimian.simplemusicplayer.domain.models.player.PlayerState;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Completable;
import io.reactivex.observers.TestObserver;
import io.reactivex.subjects.PublishSubject;

import static com.github.anrimian.simplemusicplayer.domain.models.player.PlayerState.IDLE;
import static com.github.anrimian.simplemusicplayer.domain.models.player.PlayerState.LOADING;
import static com.github.anrimian.simplemusicplayer.domain.models.player.PlayerState.PLAYING;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created on 12.11.2017.
 */
public class MusicPlayerInteractorTest {

    private MusicPlayerInteractor musicPlayerInteractor;
    private MusicPlayerController musicPlayerController;
    private MusicServiceController musicServiceController;

    private TestObserver<PlayerState> playerStateTestObserver = new TestObserver<>();
    private TestObserver<Composition> currentCompositionTestObserver = new TestObserver<>();

    private Composition one = new Composition();
    private Composition two = new Composition();
    private Composition three = new Composition();
    private Composition four = new Composition();

    private List<Composition> fakeCompositions = new ArrayList<>();

    private PublishSubject<InternalPlayerState> internalPlayerStateObservable = PublishSubject.create();

    @Before
    public void setUp() throws Exception {
        one.setFilePath("root/music/one");
        one.setId(1);
        fakeCompositions.add(one);

        two.setFilePath("root/music/two");
        two.setId(2);
        fakeCompositions.add(two);

        three.setFilePath("root/music/old/three");
        three.setId(3);
        fakeCompositions.add(three);

        four.setFilePath("root/music/old/to delete/four");
        four.setId(4);
        fakeCompositions.add(four);

        musicPlayerController = mock(MusicPlayerController.class);
        when(musicPlayerController.getPlayerStateObservable()).thenReturn(internalPlayerStateObservable);
        when(musicPlayerController.play(any())).thenReturn(Completable.complete());
        musicServiceController = mock(MusicServiceController.class);

        musicPlayerInteractor = new MusicPlayerInteractor(musicPlayerController, musicServiceController);
        musicPlayerInteractor.getPlayerStateObservable().subscribe(playerStateTestObserver);
        musicPlayerInteractor.getCurrentCompositionObservable().subscribe(currentCompositionTestObserver);
    }

    @Test
    public void startPlaying() throws Exception {
        musicPlayerInteractor.startPlaying(fakeCompositions);
        verify(musicServiceController).start();
        playerStateTestObserver.assertValues(IDLE, LOADING, PLAYING);
        currentCompositionTestObserver.assertValues(one);
        verify(musicPlayerController).play(eq(one));
    }

    @Test
    public void changePlayStateStart() throws Exception {
        musicPlayerInteractor.changePlayState();
        verify(musicServiceController).start();
        verify(musicPlayerController).resume();
        playerStateTestObserver.assertValues(IDLE, PLAYING);
    }

    @Test
    public void testSkip() throws Exception {
        musicPlayerInteractor.startPlaying(fakeCompositions);

        musicPlayerInteractor.skipToNext();
        verify(musicPlayerController).play(eq(two));

        musicPlayerInteractor.skipToPrevious();
        verify(musicPlayerController, times(2)).play(eq(one));
    }
}
