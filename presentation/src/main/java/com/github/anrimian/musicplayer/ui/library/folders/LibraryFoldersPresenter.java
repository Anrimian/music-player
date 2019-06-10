package com.github.anrimian.musicplayer.ui.library.folders;

import com.arellomobile.mvp.InjectViewState;
import com.arellomobile.mvp.MvpPresenter;
import com.github.anrimian.musicplayer.domain.business.library.LibraryFilesInteractor;
import com.github.anrimian.musicplayer.domain.business.player.MusicPlayerInteractor;
import com.github.anrimian.musicplayer.domain.business.settings.DisplaySettingsInteractor;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.composition.PlayQueueEvent;
import com.github.anrimian.musicplayer.domain.models.composition.PlayQueueItem;
import com.github.anrimian.musicplayer.domain.models.composition.folders.FileSource;
import com.github.anrimian.musicplayer.domain.models.composition.folders.Folder;
import com.github.anrimian.musicplayer.domain.models.composition.folders.FolderFileSource;
import com.github.anrimian.musicplayer.domain.models.composition.folders.MusicFileSource;
import com.github.anrimian.musicplayer.domain.models.composition.order.Order;
import com.github.anrimian.musicplayer.domain.models.playlist.PlayList;
import com.github.anrimian.musicplayer.domain.models.utils.FolderHelper;
import com.github.anrimian.musicplayer.domain.utils.ListUtils;
import com.github.anrimian.musicplayer.domain.utils.TextUtils;
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
import io.reactivex.disposables.Disposable;

import static com.github.anrimian.musicplayer.data.utils.rx.RxUtils.dispose;
import static com.github.anrimian.musicplayer.data.utils.rx.RxUtils.isInactive;
import static com.github.anrimian.musicplayer.domain.utils.ListUtils.asList;

/**
 * Created on 23.10.2017.
 */

@InjectViewState
public class LibraryFoldersPresenter extends MvpPresenter<LibraryFoldersView> {

    private final LibraryFilesInteractor interactor;
    private final MusicPlayerInteractor playerInteractor;
    private final DisplaySettingsInteractor displaySettingsInteractor;
    private final ErrorParser errorParser;
    private final Scheduler uiScheduler;

    private final CompositeDisposable presenterDisposable = new CompositeDisposable();
    private final CompositeDisposable presenterBatterySafeDisposable = new CompositeDisposable();

    private Disposable filesDisposable;
    private Disposable deleteSelfDisposable;
    private Disposable currentCompositionDisposable;

    @Nullable
    private String path;

    private Folder folder;

    private List<FileSource> sourceList = new ArrayList<>();

    private final List<Composition> compositionsForPlayList = new LinkedList<>();
    private final List<Composition> compositionsToDelete = new LinkedList<>();

    private final DiffCalculator<FileSource> diffCalculator = new DiffCalculator<>(
            () -> sourceList,
            FolderHelper::areSourcesTheSame,
            source -> {
                if (source instanceof MusicFileSource) {
                    MusicFileSource musicFileSource = (MusicFileSource) source;
                    Composition composition = musicFileSource.getComposition();
                    compositionsForPlayList.remove(composition);
                    compositionsToDelete.remove(composition);
                }
            });

    @Nullable
    private String folderToAddToPlayList;

    @Nullable
    private String searchText;

    @Nullable
    private FolderFileSource folderToDelete;

    @Nullable
    private Composition currentComposition;

    private Composition compositionInAction;

    public LibraryFoldersPresenter(@Nullable String path,
                                   LibraryFilesInteractor interactor,
                                   MusicPlayerInteractor playerInteractor,
                                   DisplaySettingsInteractor displaySettingsInteractor,
                                   ErrorParser errorParser,
                                   Scheduler uiScheduler) {
        this.path = path;
        this.interactor = interactor;
        this.playerInteractor = playerInteractor;
        this.displaySettingsInteractor = displaySettingsInteractor;
        this.errorParser = errorParser;
        this.uiScheduler = uiScheduler;
    }

    @Override
    protected void onFirstViewAttach() {
        super.onFirstViewAttach();
        if (path == null) {
            getViewState().hideBackPathButton();
        } else {
            getViewState().showBackPathButton(path);
        }
        getViewState().showQueueActions(false);
        getViewState().showSearchMode(false);

        loadMusic();
        subscribeOnUiSettings();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        presenterDisposable.dispose();
    }

    void onStart() {
        if (!sourceList.isEmpty()) {
            subscribeOnCurrentComposition();
        }
    }

    void onStop() {
        presenterBatterySafeDisposable.clear();
    }

    void onTryAgainButtonClicked() {
        loadMusic();
    }

    void onCompositionClicked(int position, Composition composition) {
        if (currentComposition != null) {
            compositionInAction = composition;
            getViewState().showCompositionActionDialog(composition);
        } else {
            interactor.play(path, composition);
            getViewState().showCurrentPlayingComposition(composition);
        }
    }

    void onCompositionIconClicked(int position, Composition composition) {
        if (composition.equals(currentComposition)) {
            playerInteractor.playOrPause();
        } else {
            interactor.play(path, composition);
            getViewState().showCurrentPlayingComposition(composition);
        }
    }

    void onPlayActionSelected() {
        interactor.play(path, compositionInAction);
    }

    void onPlayNextFolderClicked(FolderFileSource folder) {
        interactor.getAllCompositionsInPath(folder.getFullPath())
                .flatMapCompletable(playerInteractor::addCompositionsToPlayNext)
                .observeOn(uiScheduler)
                .subscribe(() -> {}, this::onDefaultError);
    }

    void onAddToQueueFolderClicked(FolderFileSource folder) {
        interactor.getAllCompositionsInPath(folder.getFullPath())
                .flatMapCompletable(playerInteractor::addCompositionsToPlayNext)
                .observeOn(uiScheduler)
                .subscribe(() -> {}, this::onDefaultError);
    }

    void onPlayNextActionSelected() {
        addCompositionsToPlayNext(asList(compositionInAction));
    }

    void onAddToQueueActionSelected() {
        addCompositionsToEnd(asList(compositionInAction));
    }

    void onPlayAllButtonClicked() {
        interactor.playAllMusicInPath(path);
    }

    void onBackPathButtonClicked() {
        if (path == null) {
            throw new IllegalStateException("can not go back in root screen");
        }
        goBackToPreviousPath();
    }

    void onDeleteCompositionButtonClicked(Composition composition) {
        compositionsToDelete.clear();
        compositionsToDelete.add(composition);
        getViewState().showConfirmDeleteDialog(compositionsToDelete);
    }

    void onDeleteFolderButtonClicked(FolderFileSource folder) {
        folderToDelete = folder;
        getViewState().showConfirmDeleteDialog(folder);
    }

    void onDeleteCompositionsDialogConfirmed() {
        deletePreparedCompositions();
    }

    void onDeleteFolderDialogConfirmed() {
        interactor.deleteFolder(folderToDelete)
                .observeOn(uiScheduler)
                .subscribe(this::onDeleteFolderSuccess, this::onDeleteCompositionsError);
    }

    void onOrderMenuItemClicked() {
        getViewState().showSelectOrderScreen(interactor.getFolderOrder());
    }

    void onOrderSelected(Order order) {
        interactor.setFolderOrder(order);
    }

    void onAddToPlayListButtonClicked(Composition composition) {
        compositionsForPlayList.clear();
        compositionsForPlayList.add(composition);
        getViewState().showSelectPlayListDialog();
    }

    void onPlayListToAddingSelected(PlayList playList) {
        addPreparedCompositionsToPlayList(playList);
    }

    void onAddFolderToPlayListButtonClicked(String path) {
        folderToAddToPlayList = path;
        getViewState().showSelectPlayListForFolderDialog();
    }

    void onPlayListForFolderSelected(PlayList playList) {
        interactor.addCompositionsToPlayList(folderToAddToPlayList, playList)
                .observeOn(uiScheduler)
                .subscribe(addedCompositions ->
                                getViewState().showAddingToPlayListComplete(playList, addedCompositions),
                        this::onAddingToPlayListError);
    }

    void onSearchTextChanged(String text) {
        if (!TextUtils.equals(searchText, text)) {
            searchText = text;
            loadMusic();
        }
    }

    void onSearchButtonClicked() {
        getViewState().showSearchMode(true);
    }

    void onShareFolderClicked(FolderFileSource folder) {
        interactor.getAllCompositionsInPath(folder.getFullPath())
                .map(compositions -> ListUtils.mapList(compositions, Composition::getFilePath))
                .observeOn(uiScheduler)
                .subscribe(getViewState()::sendCompositions, this::onReceiveCompositionsError);
    }

    void onFolderClicked(String path) {
        getViewState().goToMusicStorageScreen(path);
    }

    void onFragmentDisplayed() {
        interactor.saveCurrentPath(path);
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

    private void onDefaultError(Throwable throwable) {
        ErrorCommand errorCommand = errorParser.parseError(throwable);
        getViewState().showErrorMessage(errorCommand);
    }

    private void onReceiveCompositionsError(Throwable throwable) {
        ErrorCommand errorCommand = errorParser.parseError(throwable);
        getViewState().showReceiveCompositionsForSendError(errorCommand);
    }

    private void deletePreparedCompositions() {
        interactor.deleteCompositions(compositionsToDelete)
                .observeOn(uiScheduler)
                .subscribe(this::onDeleteCompositionsSuccess, this::onDeleteCompositionsError);
    }

    private void onDeleteFolderSuccess(List<Composition> deletedCompositions) {
        getViewState().showDeleteCompositionMessage(deletedCompositions);
        folderToDelete = null;
    }

    private void onDeleteCompositionsSuccess() {
        getViewState().showDeleteCompositionMessage(compositionsToDelete);
        compositionsToDelete.clear();
    }

    private void onDeleteCompositionsError(Throwable throwable) {
        ErrorCommand errorCommand = errorParser.parseError(throwable);
        getViewState().showDeleteCompositionError(errorCommand);
    }

    private void addPreparedCompositionsToPlayList(PlayList playList) {
        interactor.addCompositionsToPlayList(compositionsForPlayList, playList)
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

    private void goBackToPreviousPath() {
        if (path != null) {
            getViewState().goBackToParentFolderScreen();
        }
    }

    private void loadMusic() {
        if (folder == null) {
            getViewState().showLoading();
        }
        interactor.getCompositionsInPath(path, searchText)
                .observeOn(uiScheduler)
                .subscribe(this::onMusicLoaded, this::onMusicLoadingError);
    }

    private void onMusicLoaded(Folder folder) {
        this.folder = folder;
        subscribeOnFolderData(folder);
        subscribeOnSelfDeleting(folder);
    }

    private void onMusicLoadingError(Throwable throwable) {
        ErrorCommand errorCommand = errorParser.parseError(throwable);//FolderNodeNonExistException
        getViewState().showError(errorCommand);
    }

    private void subscribeOnSelfDeleting(Folder folder) {
        if (deleteSelfDisposable == null) {
            deleteSelfDisposable = folder.getSelfDeleteObservable()
                    .observeOn(uiScheduler)
                    .subscribe(this::onFolderDeleted);
            presenterDisposable.add(deleteSelfDisposable);
        }
    }

    @SuppressWarnings("unused")
    private void onFolderDeleted(Object o) {
        if (path == null) {
            getViewState().showEmptyList();
        } else {
            goBackToPreviousPath();
        }
    }

    private void subscribeOnFolderData(Folder folder) {
        dispose(filesDisposable, presenterDisposable);
        filesDisposable = folder.getFilesObservable()
                .map(diffCalculator::calculateChange)
                .observeOn(uiScheduler)
                .subscribe(this::onMusicOnFoldersReceived);
        presenterDisposable.add(filesDisposable);
    }

    private void onMusicOnFoldersReceived(ListUpdate<FileSource> listUpdate) {
        sourceList = listUpdate.getNewList();
        getViewState().updateList(listUpdate);
        if (sourceList.isEmpty()) {
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

    private void subscribeOnUiSettings() {
        presenterDisposable.add(displaySettingsInteractor.getCoversEnabledObservable()
                .observeOn(uiScheduler)
                .subscribe(this::onUiSettingsReceived, errorParser::logError));
    }

    private void onUiSettingsReceived(boolean isCoversEnabled) {
        getViewState().setDisplayCoversEnabled(isCoversEnabled);
    }
}