package com.github.anrimian.musicplayer.ui.library.folders;

import com.arellomobile.mvp.InjectViewState;
import com.arellomobile.mvp.MvpPresenter;
import com.github.anrimian.musicplayer.domain.business.library.LibraryFilesInteractor;
import com.github.anrimian.musicplayer.domain.business.playlists.PlayListsInteractor;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.composition.Order;
import com.github.anrimian.musicplayer.domain.models.composition.folders.FileSource;
import com.github.anrimian.musicplayer.domain.models.composition.folders.Folder;
import com.github.anrimian.musicplayer.domain.models.playlist.PlayList;
import com.github.anrimian.musicplayer.ui.common.error.ErrorCommand;
import com.github.anrimian.musicplayer.ui.common.error.parser.ErrorParser;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import io.reactivex.Scheduler;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

import static com.github.anrimian.musicplayer.data.utils.rx.RxUtils.dispose;
import static com.github.anrimian.musicplayer.domain.utils.ListUtils.asList;

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

    @Nullable
    private String path;

    private Folder folder;

    private List<FileSource> sourceList = new ArrayList<>();

    @Nullable
    private Composition compositionToAddToPlayList;

    @Nullable
    private String folderToAddToPlayList;

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
        getViewState().bindList(sourceList);

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
        interactor.deleteComposition(composition)
                .observeOn(uiScheduler)
                .subscribe();//TODO displayError
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
        compositionToAddToPlayList = composition;
        getViewState().showSelectPlayListDialog();
    }

    void onPlayListToAddingSelected(PlayList playList) {
        interactor.addCompositionToPlayList(compositionToAddToPlayList, playList)
                .observeOn(uiScheduler)
                .subscribe(() -> onAddingToPlayListCompleted(playList), this::onAddingToPlayListError);
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

    private void onAddingToPlayListError(Throwable throwable) {
        ErrorCommand errorCommand = errorParser.parseError(throwable);
        getViewState().showAddingToPlayListError(errorCommand);
    }

    private void onAddingToPlayListCompleted(PlayList playList) {
        getViewState().showAddingToPlayListComplete(playList, asList(compositionToAddToPlayList));
        compositionToAddToPlayList = null;
    }

    private void goBackToPreviousPath() {
        if (path != null) {
            String targetPath = null;
            int lastSlashIndex = path.lastIndexOf('/');
            int firstSlashIndex = path.indexOf("/");
            if (lastSlashIndex != -1 && firstSlashIndex != lastSlashIndex) {
                targetPath = path.substring(0, lastSlashIndex);
            }
            getViewState().goBackToMusicStorageScreen(targetPath);
        }
    }

    private void loadMusic() {
        getViewState().showLoading();
        interactor.getCompositionsInPath(path)
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
        presenterDisposable.add(folder.getSelfDeleteObservable()
                .observeOn(uiScheduler)
                .subscribe(this::onFolderDeleted));
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

    private void onMusicOnFoldersReceived(List<FileSource> sources) {
        List<FileSource> oldList = new ArrayList<>(sourceList);

        sourceList.clear();
        sourceList.addAll(sources);

        getViewState().updateList(oldList, sourceList);

        if (sourceList.isEmpty()) {
            getViewState().showEmptyList();
        } else {
            getViewState().showList();
        }
    }
}