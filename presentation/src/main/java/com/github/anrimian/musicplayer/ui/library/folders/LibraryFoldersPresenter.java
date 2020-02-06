package com.github.anrimian.musicplayer.ui.library.folders;

import android.annotation.SuppressLint;

import com.github.anrimian.musicplayer.domain.business.library.LibraryFoldersInteractor;
import com.github.anrimian.musicplayer.domain.business.player.MusicPlayerInteractor;
import com.github.anrimian.musicplayer.domain.business.settings.DisplaySettingsInteractor;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.composition.folders.FileSource;
import com.github.anrimian.musicplayer.domain.models.composition.folders.Folder;
import com.github.anrimian.musicplayer.domain.models.composition.folders.FolderFileSource;
import com.github.anrimian.musicplayer.domain.models.composition.folders.IgnoredFolder;
import com.github.anrimian.musicplayer.domain.models.composition.folders.MusicFileSource;
import com.github.anrimian.musicplayer.domain.models.composition.order.Order;
import com.github.anrimian.musicplayer.domain.models.play_queue.PlayQueueEvent;
import com.github.anrimian.musicplayer.domain.models.play_queue.PlayQueueItem;
import com.github.anrimian.musicplayer.domain.models.player.PlayerState;
import com.github.anrimian.musicplayer.domain.models.playlist.PlayList;
import com.github.anrimian.musicplayer.domain.utils.ListUtils;
import com.github.anrimian.musicplayer.domain.utils.TextUtils;
import com.github.anrimian.musicplayer.ui.common.error.ErrorCommand;
import com.github.anrimian.musicplayer.ui.common.error.parser.ErrorParser;

import java.util.ArrayList;
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
import static com.github.anrimian.musicplayer.data.utils.rx.RxUtils.isActive;
import static com.github.anrimian.musicplayer.data.utils.rx.RxUtils.isInactive;
import static com.github.anrimian.musicplayer.domain.utils.ListUtils.asList;

/**
 * Created on 23.10.2017.
 */

@InjectViewState
public class LibraryFoldersPresenter extends MvpPresenter<LibraryFoldersView> {

    private final LibraryFoldersInteractor interactor;
    private final MusicPlayerInteractor playerInteractor;
    private final DisplaySettingsInteractor displaySettingsInteractor;
    private final ErrorParser errorParser;
    private final Scheduler uiScheduler;

    private final CompositeDisposable presenterDisposable = new CompositeDisposable();
    private final CompositeDisposable presenterBatterySafeDisposable = new CompositeDisposable();

    private Disposable folderDisposable;
    private Disposable filesDisposable;
    private Disposable deleteSelfDisposable;
    private Disposable currentCompositionDisposable;

    private Disposable playActionDisposable;
    private Disposable deleteActionDisposable;
    private Disposable playlistActionDisposable;
    private Disposable shareActionDisposable;
    private Disposable fileActionDisposable;

    @Nullable
    private String path;

    private Folder folder;

    private List<FileSource> sourceList = new ArrayList<>();

    private final List<FileSource> filesForPlayList = new LinkedList<>();
    private final List<FileSource> filesToDelete = new LinkedList<>();
    private final LinkedHashSet<FileSource> selectedFiles = new LinkedHashSet<>();

    @Nullable
    private String folderToAddToPlayList;

    @Nullable
    private String searchText;

    @Nullable
    private FolderFileSource folderToDelete;

    @Nullable
    private Composition currentComposition;

    @Nullable
    private IgnoredFolder recentlyAddedIgnoredFolder;

    public LibraryFoldersPresenter(@Nullable String path,
                                   LibraryFoldersInteractor interactor,
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
        subscribeOnMoveEnabledState();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        presenterDisposable.dispose();
    }

    void onStart() {
        if (!sourceList.isEmpty()) {
            subscribeOnCurrentComposition();
            subscribeOnPlayState();
        }
    }

    void onStop() {
        presenterBatterySafeDisposable.clear();
    }

    void onTryAgainButtonClicked() {
        loadMusic();
    }

    void onCompositionClicked(int position, MusicFileSource musicFileSource) {
        processMultiSelectClick(position, musicFileSource, () -> {
            Composition composition = musicFileSource.getComposition();
            getViewState().showCompositionActionDialog(composition);
        });
    }

    void onCompositionIconClicked(int position, Composition composition) {
        if (composition.equals(currentComposition)) {
            playerInteractor.playOrPause();
        } else {
            interactor.play(path, composition);
            getViewState().showCurrentPlayingComposition(composition);
        }
    }

    void onPlayActionSelected(Composition composition) {
        interactor.play(path, composition);
    }

    void onPlayNextCompositionClicked(Composition composition) {
        addCompositionsToPlayNext(asList(composition));
    }

    void onAddToQueueCompositionClicked(Composition composition) {
        addCompositionsToEnd(asList(composition));
    }

    void onPlayNextFolderClicked(FolderFileSource folder) {
        dispose(playActionDisposable, presenterDisposable);
        playActionDisposable = interactor.getAllCompositionsInPath(folder.getPath())
                .flatMapCompletable(playerInteractor::addCompositionsToPlayNext)
                .observeOn(uiScheduler)
                .subscribe(() -> {}, this::onDefaultError);
        presenterDisposable.add(playActionDisposable);
    }

    void onAddToQueueFolderClicked(FolderFileSource folder) {
        dispose(playActionDisposable, presenterDisposable);
        playActionDisposable = interactor.getAllCompositionsInPath(folder.getPath())
                .flatMapCompletable(playerInteractor::addCompositionsToPlayNext)
                .observeOn(uiScheduler)
                .subscribe(() -> {}, this::onDefaultError);
        presenterDisposable.add(playActionDisposable);
    }

    void onPlayAllButtonClicked() {
        if (selectedFiles.isEmpty()) {
            interactor.playAllMusicInPath(path);
        } else {
            playSelectedCompositions();
        }
    }

    void onBackPathButtonClicked() {
        if (path == null) {
            throw new IllegalStateException("can not go back in root screen");
        }
        closeSelectionMode();
        goBackToPreviousPath();
    }

    void onDeleteCompositionButtonClicked(Composition composition) {
        filesToDelete.clear();
        filesToDelete.add(new MusicFileSource(composition));
        getViewState().showConfirmDeleteDialog(asList(composition));
    }

    void onDeleteFolderButtonClicked(FolderFileSource folder) {
        folderToDelete = folder;
        getViewState().showConfirmDeleteDialog(folder);
    }

    void onDeleteCompositionsDialogConfirmed() {
        deletePreparedCompositions();
    }

    void onDeleteFolderDialogConfirmed() {
        dispose(deleteActionDisposable, presenterDisposable);
        deleteActionDisposable = interactor.deleteFolder(folderToDelete)
                .observeOn(uiScheduler)
                .subscribe(this::onDeleteFolderSuccess, this::onDeleteCompositionsError);
        presenterDisposable.add(deleteActionDisposable);
    }

    void onOrderMenuItemClicked() {
        getViewState().showSelectOrderScreen(interactor.getFolderOrder());
    }

    void onOrderSelected(Order order) {
        interactor.setFolderOrder(order);
    }

    void onAddToPlayListButtonClicked(Composition composition) {
        filesForPlayList.clear();
        filesForPlayList.add(new MusicFileSource(composition));
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
        dispose(playlistActionDisposable, presenterDisposable);
        playlistActionDisposable = interactor.addCompositionsToPlayList(folderToAddToPlayList, playList)
                .observeOn(uiScheduler)
                .subscribe(addedCompositions ->
                                getViewState().showAddingToPlayListComplete(playList, addedCompositions),
                        this::onAddingToPlayListError);
        presenterDisposable.add(playlistActionDisposable);
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
        shareFileSources(asList(folder));
    }

    void onFolderClicked(int position, FolderFileSource folder) {
        processMultiSelectClick(position, folder, () ->
                getViewState().goToMusicStorageScreen(folder.getPath())
        );
    }

    void onItemLongClick(int position, FileSource folder) {
        interactor.stopMoveMode();
        getViewState().updateMoveFilesList();

        selectedFiles.add(folder);
        getViewState().showSelectionMode(selectedFiles.size());
        getViewState().onItemSelected(folder, position);
    }

    void onFragmentDisplayed() {
        interactor.saveCurrentPath(path);
    }

    void onRenameFolderClicked(String folderPath) {
        getViewState().showInputFolderNameDialog(folderPath);
    }

    void onNewFolderNameInputed(String path, String name) {
        if (isActive(fileActionDisposable)) {
            return;
        }
        dispose(fileActionDisposable);
        fileActionDisposable = interactor.renameFolder(path, name)
                .observeOn(uiScheduler)
                .subscribe(() -> {}, this::onDefaultError);
    }

    void onSelectionModeBackPressed() {
        closeSelectionMode();
    }

    void onPlayAllSelectedClicked() {
        playSelectedCompositions();
    }

    void onSelectAllButtonClicked() {
        selectedFiles.clear();//reselect previous feature
        selectedFiles.addAll(sourceList);
        getViewState().showSelectionMode(selectedFiles.size());
        getViewState().setItemsSelected(true);
    }

    private void playSelectedCompositions() {
        interactor.play(new ArrayList<>(selectedFiles));
        closeSelectionMode();
    }

    void onPlayNextSelectedSourcesClicked() {
        interactor.addCompositionsToPlayNext(new ArrayList<>(selectedFiles));
        closeSelectionMode();
    }

    void onAddToQueueSelectedSourcesClicked() {
        interactor.addCompositionsToEnd(new ArrayList<>(selectedFiles));
        closeSelectionMode();
    }

    void onAddSelectedSourcesToPlayListClicked() {
        filesForPlayList.clear();
        filesForPlayList.addAll(selectedFiles);
        getViewState().showSelectPlayListDialog();
    }

    void onShareSelectedSourcessClicked() {
        shareFileSources(new ArrayList<>(selectedFiles));
    }

    void onDeleteSelectedCompositionButtonClicked() {
        filesToDelete.clear();
        filesToDelete.addAll(selectedFiles);
        deleteActionDisposable = interactor.getAllCompositionsInFileSources(new ArrayList<>(selectedFiles))
                .observeOn(uiScheduler)
                .subscribe(getViewState()::showConfirmDeleteDialog, this::onDefaultError);
    }

    void onMoveSelectedFoldersButtonClicked() {
        interactor.addFilesToMove(path, selectedFiles);
        closeSelectionMode();
        getViewState().updateMoveFilesList();
    }

    void onCopySelectedFoldersButtonClicked() {
        interactor.addFilesToCopy(path, selectedFiles);
        closeSelectionMode();
    }

    void onCloseMoveMenuClicked() {
        if (isActive(fileActionDisposable)) {
            return;
        }
        interactor.stopMoveMode();
        getViewState().updateMoveFilesList();
    }

    void onPasteButtonClicked() {
        if (isActive(fileActionDisposable)) {
            return;
        }
        dispose(fileActionDisposable);
        fileActionDisposable = interactor.copyFilesTo(path)
                .observeOn(uiScheduler)
                .subscribe(getViewState()::updateMoveFilesList, this::onDefaultError);
    }

    void onPasteInNewFolderButtonClicked() {
        getViewState().showInputNewFolderNameDialog();
    }

    void onNewFileNameForPasteEntered(String name) {
        if (isActive(fileActionDisposable)) {
            return;
        }
        dispose(fileActionDisposable);
        fileActionDisposable = interactor.moveFilesToNewFolder(path, name)
                .observeOn(uiScheduler)
                .subscribe(getViewState()::updateMoveFilesList, this::onDefaultError);
    }

    @SuppressLint("CheckResult")
    void onExcludeFolderClicked(FolderFileSource folder) {
        //noinspection ResultOfMethodCallIgnored
        interactor.addFolderToIgnore(folder)
                .observeOn(uiScheduler)
                .subscribe(this::onIgnoreFolderAdded, this::onDefaultError);
    }

    @SuppressLint("CheckResult")
    void onRemoveIgnoredFolderClicked() {
        //noinspection ResultOfMethodCallIgnored
        interactor.deleteIgnoredFolder(recentlyAddedIgnoredFolder)
                .observeOn(uiScheduler)
                .subscribe(() -> {}, this::onDefaultError);
    }

    LinkedHashSet<FileSource> getSelectedFiles() {
        return selectedFiles;
    }

    LinkedHashSet<FileSource> getSelectedMoveFiles() {
        return interactor.getFilesToMove();
    }

    private void onIgnoreFolderAdded(IgnoredFolder folder) {
        recentlyAddedIgnoredFolder = folder;
        getViewState().showAddedIgnoredFolderMessage(folder);
    }

    private void shareFileSources(List<FileSource> fileSources) {
        dispose(shareActionDisposable, presenterDisposable);
        shareActionDisposable = interactor.getAllCompositionsInFileSources(fileSources)
                .map(compositions -> ListUtils.mapList(compositions, Composition::getFilePath))
                .observeOn(uiScheduler)
                .subscribe(getViewState()::sendCompositions, this::onReceiveCompositionsError);
        presenterDisposable.add(shareActionDisposable);
    }

    private void processMultiSelectClick(int position, FileSource folder, Runnable onClick) {
        if (selectedFiles.isEmpty()) {
            onClick.run();
            closeSelectionMode();
        } else {
            if (selectedFiles.contains(folder)) {
                selectedFiles.remove(folder);
                getViewState().onItemUnselected(folder, position);
            } else {
                selectedFiles.add(folder);
                getViewState().onItemSelected(folder, position);
            }
            getViewState().showSelectionMode(selectedFiles.size());
        }
    }

    private void closeSelectionMode() {
        selectedFiles.clear();
        getViewState().showSelectionMode(0);
        getViewState().setItemsSelected(false);
    }

    private void addCompositionsToPlayNext(List<Composition> compositions) {
        dispose(playActionDisposable, presenterDisposable);
        playActionDisposable = playerInteractor.addCompositionsToPlayNext(compositions)
                .observeOn(uiScheduler)
                .subscribe(() -> {}, this::onDefaultError);
        presenterDisposable.add(playActionDisposable);
    }

    private void addCompositionsToEnd(List<Composition> compositions) {
        dispose(playActionDisposable, presenterDisposable);
        playActionDisposable = playerInteractor.addCompositionsToEnd(compositions)
                .observeOn(uiScheduler)
                .subscribe(() -> {}, this::onDefaultError);
        presenterDisposable.add(playActionDisposable);
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
        dispose(deleteActionDisposable, presenterDisposable);
        deleteActionDisposable = interactor.deleteCompositions(filesToDelete)
                .observeOn(uiScheduler)
                .subscribe(this::onDeleteCompositionsSuccess, this::onDeleteCompositionsError);
        presenterDisposable.add(deleteActionDisposable);
    }

    private void onDeleteFolderSuccess(List<Composition> deletedCompositions) {
        getViewState().showDeleteCompositionMessage(deletedCompositions);
        folderToDelete = null;
    }

    private void onDeleteCompositionsSuccess(List<Composition> compositions) {
        getViewState().showDeleteCompositionMessage(compositions);
        filesToDelete.clear();

        if (!selectedFiles.isEmpty()) {
            closeSelectionMode();
        }
    }

    private void onDeleteCompositionsError(Throwable throwable) {
        ErrorCommand errorCommand = errorParser.parseError(throwable);
        getViewState().showDeleteCompositionError(errorCommand);
    }

    private void addPreparedCompositionsToPlayList(PlayList playList) {
        dispose(playlistActionDisposable, presenterDisposable);
        playlistActionDisposable = interactor.addCompositionsToPlayList(filesForPlayList, playList)
                .observeOn(uiScheduler)
                .subscribe(compositions -> onAddingToPlayListCompleted(compositions, playList),
                        this::onAddingToPlayListError);
        presenterDisposable.add(playlistActionDisposable);
    }

    private void onAddingToPlayListError(Throwable throwable) {
        ErrorCommand errorCommand = errorParser.parseError(throwable);
        getViewState().showAddingToPlayListError(errorCommand);
    }

    private void onAddingToPlayListCompleted(List<Composition> compositions, PlayList playList) {
        getViewState().showAddingToPlayListComplete(playList, compositions);
        filesForPlayList.clear();

        if (!selectedFiles.isEmpty()) {
            closeSelectionMode();
        }
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
        dispose(folderDisposable, presenterDisposable);
        folderDisposable = interactor.getCompositionsInPath(path, searchText)
                .observeOn(uiScheduler)
                .subscribe(this::onMusicLoaded, this::onMusicLoadingError);
        presenterDisposable.add(folderDisposable);
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
                .observeOn(uiScheduler)
                .subscribe(this::onMusicOnFoldersReceived);
        presenterDisposable.add(filesDisposable);
    }

    private void onMusicOnFoldersReceived(List<FileSource> sourceList) {
        this.sourceList = sourceList;
        getViewState().updateList(sourceList);
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
                subscribeOnPlayState();
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

    private void subscribeOnMoveEnabledState() {
        presenterDisposable.add(interactor.getMoveModeObservable()
                .observeOn(uiScheduler)
                .subscribe(getViewState()::showMoveFileMenu));
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