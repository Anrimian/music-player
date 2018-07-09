package com.github.anrimian.simplemusicplayer.ui.storage_library_screen;

import com.arellomobile.mvp.InjectViewState;
import com.arellomobile.mvp.MvpPresenter;
import com.github.anrimian.simplemusicplayer.domain.business.library.StorageLibraryInteractor;
import com.github.anrimian.simplemusicplayer.domain.models.composition.Composition;
import com.github.anrimian.simplemusicplayer.domain.models.composition.folders.FileSource;
import com.github.anrimian.simplemusicplayer.ui.common.error.ErrorCommand;
import com.github.anrimian.simplemusicplayer.ui.common.error.parser.ErrorParser;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import io.reactivex.Scheduler;

/**
 * Created on 23.10.2017.
 */

@InjectViewState
public class StorageLibraryPresenter extends MvpPresenter<StorageLibraryView> {

    private StorageLibraryInteractor interactor;
    private ErrorParser errorParser;
    private Scheduler uiScheduler;

    @Nullable
    private String path;

    private List<FileSource> sourceList = new ArrayList<>();

    public StorageLibraryPresenter(@Nullable String path,
                                   StorageLibraryInteractor interactor,
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
        String targetPath = null;
        int lastSlashIndex = path.lastIndexOf('/');
        int firstSlashIndex = path.indexOf("/");
        if (lastSlashIndex != -1 && firstSlashIndex != lastSlashIndex) {
            targetPath = path.substring(0, lastSlashIndex);
        }
        getViewState().goBackToMusicStorageScreen(targetPath);
    }

    private void loadMusic() {
        getViewState().showLoading();
        interactor.getMusicInPath(path)
                .observeOn(uiScheduler)
                .subscribe(this::onMusicLoaded, this::onMusicLoadingError);
    }

    private void onMusicLoaded(List<FileSource> musicList) {
        if (musicList.isEmpty()) {
            getViewState().showEmptyList();
            return;
        }
        sourceList.addAll(musicList);
        getViewState().showList();
        getViewState().notifyItemsLoaded(0, musicList.size());
    }

    private void onMusicLoadingError(Throwable throwable) {
        ErrorCommand errorCommand = errorParser.parseError(throwable);
        getViewState().showError(errorCommand);
    }
}