package com.github.anrimian.musicplayer.ui.library.compositions;

import com.arellomobile.mvp.InjectViewState;
import com.arellomobile.mvp.MvpPresenter;
import com.github.anrimian.musicplayer.domain.business.library.LibraryCompositionsInteractor;
import com.github.anrimian.musicplayer.domain.business.player.MusicPlayerInteractor;
import com.github.anrimian.musicplayer.domain.business.playlists.PlayListsInteractor;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.composition.Order;
import com.github.anrimian.musicplayer.domain.models.composition.PlayQueueEvent;
import com.github.anrimian.musicplayer.domain.models.composition.PlayQueueItem;
import com.github.anrimian.musicplayer.domain.models.playlist.PlayList;
import com.github.anrimian.musicplayer.domain.models.utils.CompositionHelper;
import com.github.anrimian.musicplayer.domain.utils.TextUtils;
import com.github.anrimian.musicplayer.ui.common.error.ErrorCommand;
import com.github.anrimian.musicplayer.ui.common.error.parser.ErrorParser;
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.diff_utils.calculator.DiffCalculator;
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.diff_utils.calculator.ListUpdate;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.Nullable;

import io.reactivex.Scheduler;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

import static com.github.anrimian.musicplayer.data.utils.rx.RxUtils.dispose;
import static com.github.anrimian.musicplayer.data.utils.rx.RxUtils.isInactive;

@InjectViewState
public class LibraryCompositionsPresenter extends MvpPresenter<LibraryCompositionsView> {

    private final LibraryCompositionsInteractor interactor;
    private final PlayListsInteractor playListsInteractor;
    private final MusicPlayerInteractor playerInteractor;
    private final ErrorParser errorParser;
    private final Scheduler uiScheduler;

    private final CompositeDisposable presenterDisposable = new CompositeDisposable();
    private final CompositeDisposable presenterBatterySafeDisposable = new CompositeDisposable();
    private Disposable compositionsDisposable;
    private Disposable currentCompositionDisposable;

    private List<Composition> compositions = new ArrayList<>();
    private final LinkedHashSet<Composition> selectedCompositions = new LinkedHashSet<>();

    private final DiffCalculator<Composition> diffCalculator = new DiffCalculator<>(
            () -> compositions,
            CompositionHelper::areSourcesTheSame,
            selectedCompositions::remove);

    private final List<Composition> compositionsForPlayList = new LinkedList<>();
    private final List<Composition> compositionsToDelete = new LinkedList<>();

    @Nullable
    private String searchText;

    @Nullable
    private Composition currentComposition;

    public LibraryCompositionsPresenter(LibraryCompositionsInteractor interactor,
                                        PlayListsInteractor playListsInteractor,
                                        MusicPlayerInteractor playerInteractor,
                                        ErrorParser errorParser,
                                        Scheduler uiScheduler) {
        this.interactor = interactor;
        this.playListsInteractor = playListsInteractor;
        this.playerInteractor = playerInteractor;
        this.errorParser = errorParser;
        this.uiScheduler = uiScheduler;
    }

    @Override
    protected void onFirstViewAttach() {
        super.onFirstViewAttach();
        subscribeOnCompositions();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        presenterDisposable.dispose();
    }

    void onStart() {
        if (!compositions.isEmpty()) {
            subscribeOnCurrentComposition();
        }
    }

    void onStop() {
        presenterBatterySafeDisposable.clear();
    }

    void onTryAgainLoadCompositionsClicked() {
        subscribeOnCompositions();
    }

    void onCompositionClicked(int position, Composition composition) {
        if (selectedCompositions.isEmpty()) {
            if (composition.equals(currentComposition)) {
                playerInteractor.playOrPause();
            } else {
                interactor.play(compositions, position);
                getViewState().showCurrentPlayingComposition(composition);
            }
        } else {
            if (selectedCompositions.contains(composition)) {
                selectedCompositions.remove(composition);
                getViewState().onCompositionUnselected(composition, position);
            } else {
                selectedCompositions.add(composition);
                getViewState().onCompositionSelected(composition, position);
            }
            getViewState().showSelectionMode(selectedCompositions.size());
        }
    }

    void onPlayAllButtonClicked() {
        if (selectedCompositions.isEmpty()) {
            interactor.play(compositions);
        } else {
            playSelectedCompositions();
        }
    }

    void onDeleteCompositionButtonClicked(Composition composition) {
        compositionsToDelete.clear();
        compositionsToDelete.add(composition);
        getViewState().showConfirmDeleteDialog(compositionsToDelete);
    }

    void onDeleteSelectedCompositionButtonClicked() {
        compositionsToDelete.clear();
        compositionsToDelete.addAll(selectedCompositions);
        getViewState().showConfirmDeleteDialog(compositionsToDelete);
    }

    void onDeleteCompositionsDialogConfirmed() {
        deletePreparedCompositions();
    }

    void onOrderMenuItemClicked() {
        getViewState().showSelectOrderScreen(interactor.getOrder());
    }

    void onOrderSelected(Order order) {
        interactor.setOrder(order);
        subscribeOnCompositions();
    }

    void onAddToPlayListButtonClicked(Composition composition) {
        compositionsForPlayList.clear();
        compositionsForPlayList.add(composition);
        getViewState().showSelectPlayListDialog();
    }

    void onAddSelectedCompositionToPlayListClicked() {
        compositionsForPlayList.clear();
        compositionsForPlayList.addAll(selectedCompositions);
        getViewState().showSelectPlayListDialog();
    }

    void onPlayListToAddingSelected(PlayList playList) {
        playListsInteractor.addCompositionsToPlayList(compositionsForPlayList, playList)
                .observeOn(uiScheduler)
                .subscribe(() -> onAddingToPlayListCompleted(playList),
                        this::onAddingToPlayListError);
    }

    void onSearchTextChanged(String text) {
        if (!TextUtils.equals(searchText, text)) {
            searchText = text;
            subscribeOnCompositions();
        }
    }

    void onCompositionLongClick(int position, Composition composition) {
        selectedCompositions.add(composition);
        getViewState().showSelectionMode(selectedCompositions.size());
        getViewState().onCompositionSelected(composition, position);
    }

    void onSelectionModeBackPressed() {
        closeSelectionMode();
    }

    void onShareSelectedCompositionsClicked() {
        getViewState().shareCompositions(selectedCompositions);
    }

    void onPlayAllSelectedClicked() {
        playSelectedCompositions();
    }

    void onSelectAllButtonClicked() {
        selectedCompositions.clear();
        selectedCompositions.addAll(compositions);
        getViewState().showSelectionMode(compositions.size());
        getViewState().setItemsSelected(true);
    }

    private void playSelectedCompositions() {
        interactor.play(new ArrayList<>(selectedCompositions));
        closeSelectionMode();
    }

    private void closeSelectionMode() {
        selectedCompositions.clear();
        getViewState().showSelectionMode(0);
        getViewState().setItemsSelected(false);
    }

    private void deletePreparedCompositions() {
        interactor.deleteCompositions(compositionsToDelete)
                .observeOn(uiScheduler)
                .subscribe(this::onDeleteCompositionsSuccess, this::onDeleteCompositionError);
    }

    private void onDeleteCompositionsSuccess() {
        getViewState().showDeleteCompositionMessage(compositionsToDelete);
        compositionsToDelete.clear();

        if (!selectedCompositions.isEmpty()) {
            closeSelectionMode();
        }
    }

    private void onDeleteCompositionError(Throwable throwable) {
        ErrorCommand errorCommand = errorParser.parseError(throwable);
        getViewState().showDeleteCompositionError(errorCommand);
    }

    private void onAddingToPlayListError(Throwable throwable) {
        ErrorCommand errorCommand = errorParser.parseError(throwable);
        getViewState().showAddingToPlayListError(errorCommand);
    }

    private void onAddingToPlayListCompleted(PlayList playList) {
        getViewState().showAddingToPlayListComplete(playList, compositionsForPlayList);
        compositionsForPlayList.clear();

        if (!selectedCompositions.isEmpty()) {
            closeSelectionMode();
        }
    }

    private void subscribeOnCurrentComposition() {
        currentCompositionDisposable = playerInteractor.getCurrentCompositionObservable()
                .observeOn(uiScheduler)
                .subscribe(this::onCurrentCompositionReceived, errorParser::logError);
        presenterBatterySafeDisposable.add(currentCompositionDisposable);
    }

    private void onCurrentCompositionReceived(PlayQueueEvent playQueueEvent) {
        PlayQueueItem queueItem = playQueueEvent.getPlayQueueItem();
        if (queueItem != null) {
            currentComposition = queueItem.getComposition();
        } else {
            currentComposition = null;
        }
        getViewState().showCurrentPlayingComposition(currentComposition);
    }

    private void subscribeOnCompositions() {
        if (compositions.isEmpty()) {
            getViewState().showLoading();
        }
        dispose(compositionsDisposable, presenterDisposable);
        compositionsDisposable = interactor.getCompositionsObservable(searchText)
                .map(diffCalculator::calculateChange)
                .observeOn(uiScheduler)
                .subscribe(this::onCompositionsReceived, this::onCompositionsReceivingError);
        presenterDisposable.add(compositionsDisposable);
    }

    private void onCompositionsReceivingError(Throwable throwable) {
        ErrorCommand errorCommand = errorParser.parseError(throwable);
        getViewState().showLoadingError(errorCommand);
    }

    private void onCompositionsReceived(ListUpdate<Composition> listUpdate) {
        compositions = listUpdate.getNewList();
        getViewState().updateList(listUpdate, selectedCompositions);
        if (compositions.isEmpty()) {
            if (TextUtils.isEmpty(searchText)) {
                getViewState().showEmptyList();
            } else {
                getViewState().showEmptySearchResult();
            }
        } else {
            getViewState().showList();

            if (isInactive(currentCompositionDisposable)) {
                subscribeOnCurrentComposition();
            }
        }
    }
}
