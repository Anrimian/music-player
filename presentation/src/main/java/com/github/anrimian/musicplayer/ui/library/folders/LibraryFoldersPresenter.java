package com.github.anrimian.musicplayer.ui.library.folders;

import com.arellomobile.mvp.InjectViewState;
import com.arellomobile.mvp.MvpPresenter;
import com.github.anrimian.musicplayer.domain.business.library.LibraryFilesInteractor;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.composition.Order;
import com.github.anrimian.musicplayer.domain.models.composition.folders.FileSource;
import com.github.anrimian.musicplayer.domain.models.composition.folders.Folder;
import com.github.anrimian.musicplayer.domain.models.composition.folders.FolderFileSource;
import com.github.anrimian.musicplayer.domain.models.playlist.PlayList;
import com.github.anrimian.musicplayer.domain.models.utils.FolderHelper;
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

/**
 * Created on 23.10.2017.
 */

@InjectViewState
public class LibraryFoldersPresenter extends MvpPresenter<LibraryFoldersView> {

    private final LibraryFilesInteractor interactor;
    private final ErrorParser errorParser;
    private final Scheduler uiScheduler;

    private final CompositeDisposable presenterDisposable = new CompositeDisposable();
    private Disposable filesDisposable;
    private Disposable deleteSelfDisposable;

    @Nullable
    private String path;

    private Folder folder;

    private List<FileSource> sourceList = new ArrayList<>();

    private final DiffCalculator<FileSource> diffCalculator = new DiffCalculator<>(
            () -> sourceList,
            FolderHelper::areSourcesTheSame);

    @Nullable
    private String folderToAddToPlayList;

    @Nullable
    private String searchText;

    @Nullable
    private FolderFileSource folderToDelete;

    private final List<Composition> compositionsForPlayList = new LinkedList<>();
    private final List<Composition> compositionsToDelete = new LinkedList<>();

    public LibraryFoldersPresenter(@Nullable String path,
                                   LibraryFilesInteractor interactor,
                                   ErrorParser errorParser,
                                   Scheduler uiScheduler) {
        this.path = path;
        this.interactor = interactor;
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
        getViewState().showSearchMode(false);

        loadMusic();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        presenterDisposable.dispose();
    }

    void onTryAgainButtonClicked() {
        loadMusic();
    }

    void onCompositionClicked(Composition composition) {
        interactor.playMusic(composition)
                .subscribe();//TODO handle error later
    }

    void onPlayAllButtonClicked() {
        interactor.playAllMusicInPath(path)
                .subscribe();//TODO handle error later
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
        if (folder != null) {
            subscribeOnFolderData(folder);
        }
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
            String targetPath = null;
            int lastSlashIndex = path.lastIndexOf('/');
            int firstSlashIndex = path.indexOf("/");
            if (lastSlashIndex != -1 && firstSlashIndex != lastSlashIndex) {
                targetPath = path.substring(0, lastSlashIndex);//TODO root path check
            }
            getViewState().goBackToMusicStorageScreen(targetPath);
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
        ErrorCommand errorCommand = errorParser.parseError(throwable);
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
        }
    }
}