package com.github.anrimian.simplemusicplayer.domain.business.player;

import com.github.anrimian.simplemusicplayer.domain.controllers.MusicPlayerCallback;
import com.github.anrimian.simplemusicplayer.domain.controllers.MusicPlayerController;
import com.github.anrimian.simplemusicplayer.domain.models.Composition;
import com.github.anrimian.simplemusicplayer.domain.models.player.PlayerState;
import com.github.anrimian.simplemusicplayer.domain.repositories.PlayQueueRepository;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;

import static com.github.anrimian.simplemusicplayer.domain.models.player.PlayerState.IDLE;
import static com.github.anrimian.simplemusicplayer.domain.models.player.PlayerState.LOADING;
import static com.github.anrimian.simplemusicplayer.domain.models.player.PlayerState.PLAY;
import static com.github.anrimian.simplemusicplayer.domain.models.player.PlayerState.STOP;
import static java.util.Collections.singletonList;

/**
 * Created on 02.11.2017.
 */

public class MusicPlayerInteractorNew {

    private final MusicPlayerController musicPlayerController;
    //    private SystemMusicController systemMusicController;
//    private SettingsRepository settingsRepository;
//    private UiStateRepository uiStateRepository;
    private final PlayQueueRepository playQueueRepository;

    private final BehaviorSubject<PlayerState> playerStateSubject = BehaviorSubject.createDefault(IDLE);
    private final MusicPlayerCallback musicPlayerCallback = new PlayerCallback();
//    private CompositeDisposable playerDisposable = new CompositeDisposable();

    public MusicPlayerInteractorNew(MusicPlayerController musicPlayerController,
//                                    SystemMusicController systemMusicController,
//                                    SettingsRepository settingsRepository,
//                                    UiStateRepository uiStateRepository,
                                    PlayQueueRepository playQueueRepository) {
        this.musicPlayerController = musicPlayerController;
//        this.systemMusicController = systemMusicController;
//        this.settingsRepository = settingsRepository;
//        this.uiStateRepository = uiStateRepository;
        this.playQueueRepository = playQueueRepository;

        musicPlayerController.setMusicPlayerCallback(musicPlayerCallback);
//        subscribeOnAudioFocusChanges();
//        subscribeOnInternalPlayerState();//PlayerEvent
    }

    public Completable startPlaying(Composition composition) {
        return playQueueRepository.setPlayQueue(singletonList(composition))
                .andThen(playQueueRepository.getCurrentComposition())
                .flatMapCompletable(musicPlayerController::prepareToPlay)
                .doOnComplete(musicPlayerController::resume)
                .doOnSubscribe(d -> playerStateSubject.onNext(LOADING))
                .doOnComplete(() -> playerStateSubject.onNext(PLAY))
                .doOnError(t -> stop());
    }

    public Completable startPlaying(List<Composition> compositions) {
        return playQueueRepository.setPlayQueue(compositions)
                .andThen(playQueueRepository.getCurrentComposition())
                .doOnSuccess(musicPlayerController::prepareToPlayIgnoreError)
                .toCompletable()
                .doOnComplete(musicPlayerController::resume)
                .doOnSubscribe(d -> playerStateSubject.onNext(LOADING))
                .doOnComplete(() -> playerStateSubject.onNext(PLAY));
    }

    public Completable play() {
        return playQueueRepository.getCurrentComposition()
                .doOnSuccess(musicPlayerController::prepareToPlayIgnoreError)
                .toCompletable()
                .doOnComplete(musicPlayerController::resume)
                .doOnSubscribe(d -> playerStateSubject.onNext(LOADING))
                .doOnComplete(() -> playerStateSubject.onNext(PLAY));
        //subscribe on next compositions?
    }

    public void stop() {
        musicPlayerController.stop();
        playerStateSubject.onNext(STOP);
//        systemMusicController.abandonAudioFocus();
    }

    public Observable<PlayerState> getPlayerStateObservable() {
        return playerStateSubject.distinctUntilChanged();
    }

    private class PlayerCallback implements MusicPlayerCallback {

        @Override
        public void onFinished() {
            //skip to next...
        }

        @Override
        public void onError(Throwable throwable) {
            //skip to next and write error about composition...
        }
    }

}
