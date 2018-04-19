package com.github.anrimian.simplemusicplayer.domain.business.player;

import com.github.anrimian.simplemusicplayer.domain.controllers.MusicPlayerController;
import com.github.anrimian.simplemusicplayer.domain.models.Composition;
import com.github.anrimian.simplemusicplayer.domain.models.player.PlayerState;
import com.github.anrimian.simplemusicplayer.domain.models.player.events.ErrorEvent;
import com.github.anrimian.simplemusicplayer.domain.models.player.events.FinishedEvent;
import com.github.anrimian.simplemusicplayer.domain.models.player.events.PlayerEvent;
import com.github.anrimian.simplemusicplayer.domain.repositories.PlayQueueRepository;
import com.github.anrimian.simplemusicplayer.domain.repositories.SettingsRepository;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.subjects.BehaviorSubject;

import static com.github.anrimian.simplemusicplayer.domain.models.player.PlayerState.IDLE;
import static com.github.anrimian.simplemusicplayer.domain.models.player.PlayerState.LOADING;
import static com.github.anrimian.simplemusicplayer.domain.models.player.PlayerState.PLAY;
import static com.github.anrimian.simplemusicplayer.domain.models.player.PlayerState.STOP;
import static io.reactivex.subjects.BehaviorSubject.createDefault;
import static java.util.Collections.singletonList;

/**
 * Created on 02.11.2017.
 */

public class MusicPlayerInteractorNew {

    private final MusicPlayerController musicPlayerController;
    //    private SystemMusicController systemMusicController;
    private final SettingsRepository settingsRepository;
//    private UiStateRepository uiStateRepository;
    private final PlayQueueRepository playQueueRepository;

    private final BehaviorSubject<PlayerState> playerStateSubject = createDefault(IDLE);
    private final CompositeDisposable playerDisposable = new CompositeDisposable();

    public MusicPlayerInteractorNew(MusicPlayerController musicPlayerController,
                                    SettingsRepository settingsRepository,
//                                    SystemMusicController systemMusicController,
//                                    SettingsRepository settingsRepository,
//                                    UiStateRepository uiStateRepository,
                                    PlayQueueRepository playQueueRepository) {
        this.musicPlayerController = musicPlayerController;
//        this.systemMusicController = systemMusicController;
        this.settingsRepository = settingsRepository;
//        this.uiStateRepository = uiStateRepository;
        this.playQueueRepository = playQueueRepository;

    }

    public Completable startPlaying(List<Composition> compositions) {
        return playQueueRepository.setPlayQueue(compositions)
                .doOnSubscribe(d -> playerStateSubject.onNext(LOADING))
                .doOnError(t -> playerStateSubject.onNext(STOP))
                .doOnComplete(this::play);
    }

    public void play() {
        if (playerStateSubject.getValue() != PLAY) {
            musicPlayerController.resume();
            playerStateSubject.onNext(PLAY);

            playerDisposable.add(playQueueRepository.getCurrentCompositionObservable()
                    .doOnNext(musicPlayerController::prepareToPlayIgnoreError)
                    .subscribe());

            playerDisposable.add(musicPlayerController.getEventsObservable()
                    .subscribe(this::onMusicPlayerEventReceived));
        }
    }

    public void stop() {
        musicPlayerController.stop();
        playerStateSubject.onNext(STOP);
        playerDisposable.clear();
//        systemMusicController.abandonAudioFocus();
    }

    public Observable<PlayerState> getPlayerStateObservable() {
        return playerStateSubject.distinctUntilChanged();
    }

    private void onMusicPlayerEventReceived(PlayerEvent playerEvent) {
        if (playerEvent instanceof FinishedEvent) {
            onCompositionPlayFinished();
        } else if (playerEvent instanceof ErrorEvent) {
            onCompositionPlayFinished();
            //write error about composition...
        }
    }

    private void onCompositionPlayFinished() {
        int currentPosition = playQueueRepository.skipToNext();
        if (currentPosition != 0 || settingsRepository.isInfinitePlayingEnabled()) {
            //possible bug with stopping from end to start. Check on live app
            musicPlayerController.resume();
        } else {
            stop();
        }
    }


//    private class PlayerCallback implements MusicPlayerCallback {
//
//        @Override
//        public void onFinished() {
//            //skip to next...
//        }
//
//        @Override
//        public void onError(Throwable throwable) {
//            //skip to next and write error about composition...
//        }
//    }

}
