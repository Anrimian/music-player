package com.github.anrimian.musicplayer.ui.library.compositions;

import com.github.anrimian.musicplayer.domain.business.library.LibraryCompositionsInteractor;
import com.github.anrimian.musicplayer.domain.business.player.MusicPlayerInteractor;
import com.github.anrimian.musicplayer.domain.business.playlists.PlayListsInteractor;
import com.github.anrimian.musicplayer.domain.business.settings.DisplaySettingsInteractor;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.composition.PlayQueueEvent;
import com.github.anrimian.musicplayer.domain.models.composition.PlayQueueItem;
import com.github.anrimian.musicplayer.domain.models.composition.order.Order;
import com.github.anrimian.musicplayer.domain.models.player.PlayerState;
import com.github.anrimian.musicplayer.domain.models.playlist.PlayList;
import com.github.anrimian.musicplayer.domain.utils.TextUtils;
import com.github.anrimian.musicplayer.ui.common.error.ErrorCommand;
import com.github.anrimian.musicplayer.ui.common.error.parser.ErrorParser;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.Nullable;

import io.reactivex.Scheduler;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import moxy.InjectViewState;
import moxy.MvpPresenter;

import static com.github.anrimian.musicplayer.data.utils.rx.RxUtils.dispose;
import static com.github.anrimian.musicplayer.data.utils.rx.RxUtils.isInactive;
import static com.github.anrimian.musicplayer.domain.utils.ListUtils.asList;

@InjectViewState
public class LibraryCompositionsPresenter extends MvpPresenter<LibraryCompositionsView> {

    private final LibraryCompositionsInteractor interactor;
    private final PlayListsInteractor playListsInteractor;
    private final MusicPlayerInteractor playerInteractor;
    private final DisplaySettingsInteractor displaySettingsInteractor;
    private final ErrorParser errorParser;
    private final Scheduler uiScheduler;

    private final CompositeDisposable presenterDisposable = new CompositeDisposable();
    private final CompositeDisposable presenterBatterySafeDisposable = new CompositeDisposable();
    private Disposable compositionsDisposable;
    private Disposable currentCompositionDisposable;

    private List<Composition> compositions = new ArrayList<>();
    private final LinkedHashSet<Composition> selectedCompositions = new LinkedHashSet<>();

    private final List<Composition> compositionsForPlayList = new LinkedList<>();
    private final List<Composition> compositionsToDelete = new LinkedList<>();

    @Nullable
    private String searchText;

    @Nullable
    private Composition currentComposition;

    public LibraryCompositionsPresenter(LibraryCompositionsInteractor interactor,
                                        PlayListsInteractor playListsInteractor,
                                        MusicPlayerInteractor playerInteractor,
                                        DisplaySettingsInteractor displaySettingsInteractor,
                                        ErrorParser errorParser,
                                        Scheduler uiScheduler) {
        this.interactor = interactor;
        this.playListsInteractor = playListsInteractor;
        this.playerInteractor = playerInteractor;
        this.displaySettingsInteractor = displaySettingsInteractor;
        this.errorParser = errorParser;
        this.uiScheduler = uiScheduler;
    }

    @Override
    protected void onFirstViewAttach() {
        super.onFirstViewAttach();
        getViewState().showQueueActions(false);
        subscribeOnCompositions();
        subscribeOnUiSettings();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        presenterDisposable.dispose();
    }

    void onStart() {
        if (!compositions.isEmpty()) {
            subscribeOnCurrentComposition();
            subscribeOnPlayState();
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
            getViewState().showCompositionActionDialog(composition, position);
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

    void onCompositionIconClicked(int position, Composition composition) {
        if (composition.equals(currentComposition)) {
            playerInteractor.playOrPause();
        } else {
            playerInteractor.startPlaying(compositions, position);
            getViewState().showCurrentPlayingComposition(composition);
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

    void onPlayNextCompositionClicked(Composition composition) {
        addCompositionsToPlayNext(asList(composition));
    }

    void onAddToQueueCompositionClicked(Composition composition) {
        addCompositionsToEnd(asList(composition));
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
        selectedCompositions.clear();//reselect previous feature
        selectedCompositions.addAll(compositions);
        getViewState().showSelectionMode(compositions.size());
        getViewState().setItemsSelected(true);
    }

    void onPlayNextSelectedCompositionsClicked() {
        addCompositionsToPlayNext(new ArrayList<>(selectedCompositions));
        closeSelectionMode();
    }

    void onAddToQueueSelectedCompositionsClicked() {
        addCompositionsToEnd(new ArrayList<>(selectedCompositions));
        closeSelectionMode();
    }

    void onPlayActionSelected(int position) {
        interactor.play(compositions, position);
    }

    HashSet<Composition> getSelectedCompositions() {
        return selectedCompositions;
    }

    private void addCompositionsToPlayNext(List<Composition> compositions) {
        playerInteractor.addCompositionsToPlayNext(compositions)
                .observeOn(uiScheduler)
                .subscribe(() -> {}, this::onDefaultError);
    }

    private void addCompositionsToEnd(List<Composition> compositions) {
        playerInteractor.addCompositionsToEnd(compositions)
                .observeOn(uiScheduler)
                .subscribe(() -> {}, this::onDefaultError);
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

    private void onDefaultError(Throwable throwable) {
        ErrorCommand errorCommand = errorParser.parseError(throwable);
        getViewState().showErrorMessage(errorCommand);
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
        getViewState().showQueueActions(currentComposition != null);
        getViewState().showCurrentPlayingComposition(currentComposition);
    }

    private void subscribeOnCompositions() {
        if (compositions.isEmpty()) {
            getViewState().showLoading();
        }
        dispose(compositionsDisposable, presenterDisposable);
        compositionsDisposable = interactor.getCompositionsObservable(searchText)
                .observeOn(uiScheduler)
                .subscribe(this::onCompositionsReceived, this::onCompositionsReceivingError);
        presenterDisposable.add(compositionsDisposable);
    }

    private void onCompositionsReceivingError(Throwable throwable) {
        ErrorCommand errorCommand = errorParser.parseError(throwable);
        getViewState().showLoadingError(errorCommand);
    }

    private void onCompositionsReceived(List<Composition> compositions) {
        this.compositions = compositions;
        getViewState().updateList(compositions);
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
                subscribeOnPlayState();
            }
        }
    }

    private void subscribeOnUiSettings() {
        presenterDisposable.add(displaySettingsInteractor.getCoversEnabledObservable()
                .observeOn(uiScheduler)
                .subscribe(this::onUiSettingsReceived, errorParser::logError));
    }

    private void onUiSettingsReceived(boolean isCoversEnabled) {
        getViewState().setDisplayCoversEnabled(isCoversEnabled);
    }

    private void subscribeOnPlayState() {
        presenterBatterySafeDisposable.add(playerInteractor.getPlayerStateObservable()
                .observeOn(uiScheduler)
                .subscribe(this::onPlayerStateReceived));
    }

    private void onPlayerStateReceived(PlayerState state) {
        getViewState().showPlayState(state == PlayerState.PLAY);
    }
}
