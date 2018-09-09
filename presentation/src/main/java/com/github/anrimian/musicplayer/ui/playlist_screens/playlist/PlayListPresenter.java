package com.github.anrimian.musicplayer.ui.playlist_screens.playlist;

import com.arellomobile.mvp.InjectViewState;
import com.arellomobile.mvp.MvpPresenter;
import com.github.anrimian.musicplayer.domain.business.player.MusicPlayerInteractor;
import com.github.anrimian.musicplayer.domain.business.playlists.PlayListsInteractor;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.playlist.PlayList;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Scheduler;
import io.reactivex.disposables.CompositeDisposable;

import static com.github.anrimian.musicplayer.domain.utils.ListUtils.asList;

@InjectViewState
public class PlayListPresenter extends MvpPresenter<PlayListView> {

    private final MusicPlayerInteractor musicPlayerInteractor;
    private final PlayListsInteractor playListsInteractor;
    private final Scheduler uiScheduler;

    private final CompositeDisposable presenterDisposable = new CompositeDisposable();

    private final long playListId;

    private List<Composition> compositions = new ArrayList<>();

    public PlayListPresenter(long playListId,
                             MusicPlayerInteractor musicPlayerInteractor,
                             PlayListsInteractor playListsInteractor,
                             Scheduler uiScheduler) {
        this.playListId = playListId;
        this.musicPlayerInteractor = musicPlayerInteractor;
        this.playListsInteractor = playListsInteractor;
        this.uiScheduler = uiScheduler;
    }

    @Override
    protected void onFirstViewAttach() {
        super.onFirstViewAttach();
        getViewState().bindList(compositions);
        subscribeOnCompositions();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        presenterDisposable.dispose();
    }

    void onCompositionClicked(Composition composition) {
        musicPlayerInteractor.startPlaying(asList(composition))
                .subscribe();//TODO handle error later
    }

    void onPlayAllButtonClicked() {
        musicPlayerInteractor.startPlaying(compositions)
                .subscribe();//TODO handle error later
    }

    private void subscribeOnCompositions() {
        getViewState().showLoading();
        presenterDisposable.add(playListsInteractor.getCompositionsObservable(playListId)
                .observeOn(uiScheduler)
                .subscribe(this::onPlayListsReceived));
    }

    private void onPlayListsReceived(List<Composition> newCompositions) {
        List<Composition> oldList = new ArrayList<>(compositions);

        compositions.clear();
        compositions.addAll(newCompositions);

        getViewState().updateList(oldList, newCompositions);

        if (newCompositions.isEmpty()) {
            getViewState().showEmptyList();
        } else {
            getViewState().showList();
        }
    }
}
