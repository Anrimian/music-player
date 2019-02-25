package com.github.anrimian.musicplayer.ui.playlist_screens.playlist;

import com.arellomobile.mvp.InjectViewState;
import com.arellomobile.mvp.MvpPresenter;
import com.github.anrimian.musicplayer.domain.business.player.MusicPlayerInteractor;
import com.github.anrimian.musicplayer.domain.business.playlists.PlayListsInteractor;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.playlist.PlayList;
import com.github.anrimian.musicplayer.domain.models.playlist.PlayListItem;
import com.github.anrimian.musicplayer.domain.models.utils.PlayListItemHelper;
import com.github.anrimian.musicplayer.ui.common.error.ErrorCommand;
import com.github.anrimian.musicplayer.ui.common.error.parser.ErrorParser;
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.diff_utils.calculator.DiffCalculator;
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.diff_utils.calculator.ListUpdate;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.Nullable;

import io.reactivex.Scheduler;
import io.reactivex.disposables.CompositeDisposable;

import static com.github.anrimian.musicplayer.domain.utils.ListUtils.asList;
import static com.github.anrimian.musicplayer.domain.utils.ListUtils.mapList;
import static java.util.Objects.requireNonNull;

@InjectViewState
public class PlayListPresenter extends MvpPresenter<PlayListView> {

    private final MusicPlayerInteractor musicPlayerInteractor;
    private final PlayListsInteractor playListsInteractor;
    private final ErrorParser errorParser;
    private final Scheduler uiScheduler;

    private final CompositeDisposable presenterDisposable = new CompositeDisposable();

    private final long playListId;

    private List<PlayListItem> items = new ArrayList<>();

    private final DiffCalculator<PlayListItem> diffCalculator = new DiffCalculator<>(
            () -> items,
            PlayListItemHelper::areSourcesTheSame);

    @Nullable
    private PlayList playList;

    private final List<Composition> compositionsForPlayList = new LinkedList<>();
    private final List<Composition> compositionsToDelete = new LinkedList<>();

    public PlayListPresenter(long playListId,
                             MusicPlayerInteractor musicPlayerInteractor,
                             PlayListsInteractor playListsInteractor,
                             ErrorParser errorParser,
                             Scheduler uiScheduler) {
        this.playListId = playListId;
        this.musicPlayerInteractor = musicPlayerInteractor;
        this.playListsInteractor = playListsInteractor;
        this.errorParser = errorParser;
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

    void onDeleteCompositionButtonClicked(Composition composition) {
        compositionsToDelete.clear();
        compositionsToDelete.add(composition);
        getViewState().showConfirmDeleteDialog(compositionsToDelete);
    }

    void onDeleteCompositionsDialogConfirmed() {
        deletePreparedCompositions();
    }

    void onAddToPlayListButtonClicked(Composition composition) {
        compositionsForPlayList.clear();
        compositionsForPlayList.add(composition);
        getViewState().showSelectPlayListDialog();
    }

    void onPlayListToAddingSelected(PlayList playList) {
        addPreparedCompositionsToPlayList(playList);
    }

    void onDeleteFromPlayListButtonClicked(PlayListItem playListItem) {
        playListsInteractor.deleteItemFromPlayList(playListItem.getItemId(), playListId)
                .observeOn(uiScheduler)
                .subscribe(() -> onDeleteItemCompleted(playListItem), this::onDeleteItemError);
    }

    void onDeletePlayListButtonClicked() {
        getViewState().showConfirmDeletePlayListDialog(playList);
    }

    void onDeletePlayListDialogConfirmed() {
        playListsInteractor.deletePlayList(requireNonNull(playList).getId())
                .observeOn(uiScheduler)
                .subscribe(this::onPlayListDeleted, this::onPlayListDeletingError);
    }

    void onFragmentMovedToTop() {
        playListsInteractor.setSelectedPlayListScreen(playListId);
    }

    private void onPlayListDeletingError(Throwable throwable) {
        ErrorCommand errorCommand = errorParser.parseError(throwable);
        getViewState().showDeletePlayListError(errorCommand);
    }

    private void onPlayListDeleted() {
        getViewState().showPlayListDeleteSuccess(playList);
    }

    private void onDeleteItemCompleted(PlayListItem item) {
        if (playList != null) {
            getViewState().showDeleteItemCompleted(playList, asList(item));
        }
    }

    private void onDeleteItemError(Throwable throwable) {
        ErrorCommand errorCommand = errorParser.parseError(throwable);
        getViewState().showDeleteItemError(errorCommand);
    }

    private void addPreparedCompositionsToPlayList(PlayList playList) {
        playListsInteractor.addCompositionsToPlayList(compositionsForPlayList, playList)
                .observeOn(uiScheduler)
                .subscribe(() -> onAddingToPlayListCompleted(playList),
                        this::onAddingToPlayListError);
    }

    private void onAddingToPlayListError(Throwable throwable) {
        ErrorCommand errorCommand = errorParser.parseError(throwable);
        getViewState().showAddingToPlayListError(errorCommand);
    }

    private void onAddingToPlayListCompleted(PlayList playList) {
        getViewState().showAddingToPlayListComplete(playList, compositionsForPlayList);
        compositionsForPlayList.clear();
    }

    private void deletePreparedCompositions() {
        musicPlayerInteractor.deleteCompositions(compositionsToDelete)
                .observeOn(uiScheduler)
                .subscribe(this::onDeleteCompositionsSuccess, this::onDeleteCompositionsError);
    }

    private void onDeleteCompositionsSuccess() {
        getViewState().showDeleteCompositionMessage(compositionsToDelete);
        compositionsToDelete.clear();
    }

    private void onDeleteCompositionsError(Throwable throwable) {
        ErrorCommand errorCommand = errorParser.parseError(throwable);
        getViewState().showDeleteCompositionError(errorCommand);
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
                .subscribe(this::onPlayListInfoReceived,
                        t -> getViewState().closeScreen(),
                        getViewState()::closeScreen));
    }

    private void onPlayListInfoReceived(PlayList playList) {
        this.playList = playList;
        getViewState().showPlayListInfo(playList);
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
