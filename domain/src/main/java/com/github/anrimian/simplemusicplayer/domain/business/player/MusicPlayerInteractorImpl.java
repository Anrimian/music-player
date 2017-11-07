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
        musicPlayerController.play(compositions);
    }

    @Override
    public void changePlayState() {
        musicPlayerController.changePlayState();
    }

    @Override
    public void skipToPrevious() {
        musicPlayerController.skipToPrevious();
    }

    @Override
    public void skipToNext() {
        musicPlayerController.skipToNext();
    }
}
