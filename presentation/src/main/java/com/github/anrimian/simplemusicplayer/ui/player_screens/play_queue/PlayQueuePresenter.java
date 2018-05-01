package com.github.anrimian.simplemusicplayer.ui.player_screens.play_queue;

import com.arellomobile.mvp.MvpPresenter;
import com.github.anrimian.simplemusicplayer.domain.business.player.MusicPlayerInteractor;
import com.github.anrimian.simplemusicplayer.domain.models.composition.Composition;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Scheduler;
import io.reactivex.disposables.CompositeDisposable;

/**
 * Created on 11.02.2018.
 */

public class PlayQueuePresenter extends MvpPresenter<PlayQueueView>{

    private final MusicPlayerInteractor musicPlayerInteractor;
    private final Scheduler uiScheduler;

    private final CompositeDisposable presenterDisposable = new CompositeDisposable();

    private final List<Composition> currentPlayList = new ArrayList<>();

    public PlayQueuePresenter(MusicPlayerInteractor musicPlayerInteractor, Scheduler uiScheduler) {
        this.musicPlayerInteractor = musicPlayerInteractor;
        this.uiScheduler = uiScheduler;
    }

    @Override
    protected void onFirstViewAttach() {
        super.onFirstViewAttach();
        getViewState().bindPlayList(currentPlayList);
    }
}
