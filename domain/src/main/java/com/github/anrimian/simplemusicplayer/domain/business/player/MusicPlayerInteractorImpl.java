package com.github.anrimian.simplemusicplayer.domain.business.player;

import com.github.anrimian.simplemusicplayer.domain.controllers.MusicPlayerController;
import com.github.anrimian.simplemusicplayer.domain.models.Composition;

import java.util.List;

/**
 * Created on 02.11.2017.
 */

public class MusicPlayerInteractorImpl implements MusicPlayerInteractor {

    private MusicPlayerController musicPlayerController;

    public MusicPlayerInteractorImpl(MusicPlayerController musicPlayerController) {
        this.musicPlayerController = musicPlayerController;
    }

    @Override
    public void startPlaying(List<Composition> compositions) {
//        setState(PLAYING);
        musicPlayerController.play(compositions);
    }

    @Override
    public void stopPlaying() {
//        setState(STOP);
        musicPlayerController.pause();
    }

    @Override
    public void resumePlaying() {
//        setState(PLAYING);
        musicPlayerController.resume();
    }
}
