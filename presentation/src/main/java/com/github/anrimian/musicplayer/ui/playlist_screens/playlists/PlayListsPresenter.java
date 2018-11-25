package com.github.anrimian.musicplayer.ui.playlist_screens.playlists;

import com.arellomobile.mvp.InjectViewState;
import com.arellomobile.mvp.MvpPresenter;
import com.github.anrimian.musicplayer.domain.business.playlists.PlayListsInteractor;
import com.github.anrimian.musicplayer.domain.models.playlist.PlayList;
import com.github.anrimian.musicplayer.domain.models.utils.PlayListHelper;
import com.github.anrimian.musicplayer.ui.common.error.ErrorCommand;
import com.github.anrimian.musicplayer.ui.common.error.parser.ErrorParser;
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.diff_utils.calculator.DiffCalculator;
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.diff_utils.calculator.ListUpdate;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Scheduler;
import io.reactivex.disposables.CompositeDisposable;

@InjectViewState
public class PlayListsPresenter extends MvpPresenter<PlayListsView> {

    private final PlayListsInteractor playListsInteractor;
    private final Scheduler uiScheduler;
    private final ErrorParser errorParser;

    private final CompositeDisposable presenterDisposable = new CompositeDisposable();

    private List<PlayList> playLists = new ArrayList<>();

    private final DiffCalculator<PlayList> diffCalculator = new DiffCalculator<>(
            () -> playLists,
            PlayListHelper::areSourcesTheSame);

    private PlayList playListInMenu;
    private PlayList playListToDelete;

    public PlayListsPresenter(PlayListsInteractor playListsInteractor,
                              Scheduler uiScheduler,
                              ErrorParser errorParser) {
        this.playListsInteractor = playListsInteractor;
        this.uiScheduler = uiScheduler;
        this.errorParser = errorParser;
    }

    @Override
    protected void onFirstViewAttach() {
        super.onFirstViewAttach();
        subscribeOnPlayLists();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        presenterDisposable.dispose();
    }

    void onPlayListLongClick(PlayList playList) {
        playListInMenu = playList;
        getViewState().showPlayListMenu(playList);
    }

    void onDeletePlayListButtonClicked() {
        playListToDelete = playListInMenu;
        getViewState().showConfirmDeletePlayListDialog(playListToDelete);
        playListInMenu = null;
    }

    void onDeletePlayListDialogConfirmed() {
        playListsInteractor.deletePlayList(playListToDelete.getId())
                .observeOn(uiScheduler)
                .subscribe(this::onPlayListDeleted, this::onPlayListDeletingError);
    }

    private void onPlayListDeletingError(Throwable throwable) {
        ErrorCommand errorCommand = errorParser.parseError(throwable);
        getViewState().showDeletePlayListError(errorCommand);
        playListToDelete = null;
    }

    private void onPlayListDeleted() {
        getViewState().showPlayListDeleteSuccess(playListToDelete);
        playListToDelete = null;
    }

    private void subscribeOnPlayLists() {
        getViewState().showLoading();
        presenterDisposable.add(playListsInteractor.getPlayListsObservable()
                .map(diffCalculator::calculateChange)
                .observeOn(uiScheduler)
                .subscribe(this::onPlayListsReceived));
    }

    private void onPlayListsReceived(ListUpdate<PlayList> listUpdate) {
        playLists = listUpdate.getNewList();
        getViewState().updateList(listUpdate);
        if (playLists.isEmpty()) {
            getViewState().showEmptyList();
        } else {
            getViewState().showList();
        }
    }
}
