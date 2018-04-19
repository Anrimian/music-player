package com.github.anrimian.simplemusicplayer.domain.business.player;

import com.github.anrimian.simplemusicplayer.domain.controllers.MusicPlayerController;
import com.github.anrimian.simplemusicplayer.domain.models.Composition;
import com.github.anrimian.simplemusicplayer.domain.models.player.PlayerState;
import com.github.anrimian.simplemusicplayer.domain.models.player.events.PlayerEvent;
import com.github.anrimian.simplemusicplayer.domain.repositories.PlayQueueRepository;

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
//    private SettingsRepository settingsRepository;
//    private UiStateRepository uiStateRepository;
    private final PlayQueueRepository playQueueRepository;

    private final BehaviorSubject<PlayerState> playerStateSubject = createDefault(IDLE);
    private final CompositeDisposable playerDisposable = new CompositeDisposable();

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

    }

    public Completable startPlaying(Composition composition) {
        return playQueueRepository.setPlayQueue(singletonList(composition))
                .doOnSubscribe(d -> playerStateSubject.onNext(LOADING))
                .andThen(playQueueRepository.getCurrentComposition())
                .flatMapCompletable(musicPlayerController::prepareToPlay)
                .doOnComplete(this::onCompositionReadyToPlay)
                .doOnError(t -> stop());
    }

    public Completable startPlaying(List<Composition> compositions) {
        return playQueueRepository.setPlayQueue(compositions)
                .doOnSubscribe(d -> playerStateSubject.onNext(LOADING))
                .andThen(playQueueRepository.getCurrentComposition())
                .doOnSuccess(musicPlayerController::prepareToPlayIgnoreError)
                .toCompletable()
                .doOnComplete(this::onCompositionReadyToPlay);
    }

    public Completable play() {
        return playQueueRepository.getCurrentComposition()
                .doOnSubscribe(d -> playerStateSubject.onNext(LOADING))
                .doOnSuccess(musicPlayerController::prepareToPlayIgnoreError)
                .toCompletable()
                .doOnComplete(this::onCompositionReadyToPlay);
        //subscribe on next compositions?
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

    private void onCompositionReadyToPlay() {
        musicPlayerController.resume();
        playerStateSubject.onNext(PLAY);

//        subscribeOnAudioFocusChanges();
        playerDisposable.add(musicPlayerController.getEventsObservable()
                .subscribe(this::onMusicPlayerEventReceived));
    }

    private void onMusicPlayerEventReceived(PlayerEvent playerEvent) {

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
