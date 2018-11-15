package com.github.anrimian.musicplayer.ui.playlist_screens.playlist;

import com.arellomobile.mvp.InjectViewState;
import com.arellomobile.mvp.MvpPresenter;
import com.github.anrimian.musicplayer.domain.business.player.MusicPlayerInteractor;
import com.github.anrimian.musicplayer.domain.business.playlists.PlayListsInteractor;
import com.github.anrimian.musicplayer.domain.models.playlist.PlayListItem;
import com.github.anrimian.musicplayer.domain.models.utils.PlayListItemHelper;
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.diff_utils.calculator.DiffCalculator;
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.diff_utils.calculator.ListUpdate;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Scheduler;
import io.reactivex.disposables.CompositeDisposable;

import static com.github.anrimian.musicplayer.domain.utils.ListUtils.asList;
import static com.github.anrimian.musicplayer.domain.utils.ListUtils.mapList;

@InjectViewState
public class PlayListPresenter extends MvpPresenter<PlayListView> {

    private final MusicPlayerInteractor musicPlayerInteractor;
    private final PlayListsInteractor playListsInteractor;
    private final Scheduler uiScheduler;

    private final CompositeDisposable presenterDisposable = new CompositeDisposable();

    private final long playListId;

    private List<PlayListItem> items = new ArrayList<>();

    private final DiffCalculator<PlayListItem> diffCalculator = new DiffCalculator<>(
            () -> items,
            PlayListItemHelper::areSourcesTheSame);

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
        subscribeOnCompositions();
        subscribePlayList();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        presenterDisposable.dispose();
    }

    void onCompositionClicked(int position) {
        musicPlayerInteractor.startPlaying(mapList(items, PlayListItem::getComposition), position);
    }

    void onPlayAllButtonClicked() {
        musicPlayerInteractor.startPlaying(mapList(items, PlayListItem::getComposition));
    }

    private void subscribeOnCompositions() {
        getViewState().showLoading();
        presenterDisposable.add(playListsInteractor.getCompositionsObservable(playListId)
                .map(diffCalculator::calculateChange)
                .observeOn(uiScheduler)
                .subscribe(this::onPlayListsReceived,
                        t -> getViewState().closeScreen(),
                        getViewState()::closeScreen));
    }

    private void subscribePlayList() {
        presenterDisposable.add(playListsInteractor.getPlayListObservable(playListId)
                .observeOn(uiScheduler)
                .subscribe(getViewState()::showPlayListInfo,
                        t -> getViewState().closeScreen(),
                        getViewState()::closeScreen));
    }

    private void onPlayListsReceived(ListUpdate<PlayListItem> listUpdate) {
        items = listUpdate.getNewList();
        getViewState().updateItemsList(listUpdate);
        if (items.isEmpty()) {
            getViewState().showEmptyList();
        } else {
            getViewState().showList();
        }
    }
}
