package com.github.anrimian.simplemusicplayer.domain.business.player;

import com.github.anrimian.simplemusicplayer.domain.controllers.MusicPlayerController;
import com.github.anrimian.simplemusicplayer.domain.controllers.SystemMusicController;
import com.github.anrimian.simplemusicplayer.domain.models.player.PlayerState;
import com.github.anrimian.simplemusicplayer.domain.repositories.PlayQueueRepository;
import com.github.anrimian.simplemusicplayer.domain.repositories.SettingsRepository;
import com.github.anrimian.simplemusicplayer.domain.repositories.UiStateRepository;

import io.reactivex.subjects.BehaviorSubject;

import static com.github.anrimian.simplemusicplayer.domain.models.player.PlayerState.IDLE;

/**
 * Created on 02.11.2017.
 */

public class MusicPlayerInteractorNew {

    private MusicPlayerController musicPlayerController;
    private SystemMusicController systemMusicController;
    private SettingsRepository settingsRepository;
    private UiStateRepository uiStateRepository;
    private PlayQueueRepository playQueueRepository;

    private BehaviorSubject<PlayerState> playerStateSubject = BehaviorSubject.createDefault(IDLE);

    public MusicPlayerInteractorNew(MusicPlayerController musicPlayerController,
                                    SystemMusicController systemMusicController,
                                    SettingsRepository settingsRepository,
                                    UiStateRepository uiStateRepository,
                                    PlayQueueRepository playQueueRepository) {
        this.musicPlayerController = musicPlayerController;
        this.systemMusicController = systemMusicController;
        this.settingsRepository = settingsRepository;
        this.uiStateRepository = uiStateRepository;
        this.playQueueRepository = playQueueRepository;

//        subscribeOnAudioFocusChanges();
//        subscribeOnInternalPlayerState();
//        restorePlaylistState();
    }

//    public Completable startPlaying(List<Composition> compositions) {//TODO return Completable
//        return playQueueRepository.setPlayQueue(compositions)
//                .subscribe(() -> prepareToPlayPosition(true));
//    }

}
