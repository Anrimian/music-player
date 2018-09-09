package com.github.anrimian.musicplayer.ui.playlist_screens.playlists;

import com.arellomobile.mvp.InjectViewState;
import com.arellomobile.mvp.MvpPresenter;
import com.github.anrimian.musicplayer.domain.business.playlists.PlayListsInteractor;
import com.github.anrimian.musicplayer.domain.models.playlist.PlayList;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Scheduler;
import io.reactivex.disposables.CompositeDisposable;

@InjectViewState
public class PlayListsPresenter extends MvpPresenter<PlayListsView> {

    private final PlayListsInteractor playListsInteractor;
    private final Scheduler uiScheduler;

    private final CompositeDisposable presenterDisposable = new CompositeDisposable();

    private List<PlayList> playLists = new ArrayList<>();

    public PlayListsPresenter(PlayListsInteractor playListsInteractor, Scheduler uiScheduler) {
        this.playListsInteractor = playListsInteractor;
        this.uiScheduler = uiScheduler;
    }

    @Override
    protected void onFirstViewAttach() {
        super.onFirstViewAttach();
        getViewState().bindList(playLists);
        subscribeOnPlayLists();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        presenterDisposable.dispose();
    }

    private void subscribeOnPlayLists() {
        getViewState().showLoading();
        presenterDisposable.add(playListsInteractor.getPlayListsObservable()
                .observeOn(uiScheduler)
                .subscribe(this::onPlayListsReceived));
    }

    private void onPlayListsReceived(List<PlayList> newPlayLists) {
        List<PlayList> oldList = new ArrayList<>(playLists);

        playLists.clear();
        playLists.addAll(newPlayLists);

        getViewState().updateList(oldList, newPlayLists);

        if (newPlayLists.isEmpty()) {
            getViewState().showEmptyList();
        } else {
            getViewState().showList();
        }
    }
}
