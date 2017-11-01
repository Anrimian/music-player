package com.github.anrimian.simplemusicplayer.ui.library.storage;

import com.arellomobile.mvp.InjectViewState;
import com.arellomobile.mvp.MvpPresenter;
import com.github.anrimian.simplemusicplayer.domain.business.music.StorageLibraryInteractor;
import com.github.anrimian.simplemusicplayer.domain.models.Composition;
import com.github.anrimian.simplemusicplayer.domain.models.files.FileSource;
import com.github.anrimian.simplemusicplayer.utils.error.ErrorCommand;
import com.github.anrimian.simplemusicplayer.utils.error.parser.ErrorParser;

import java.util.List;

import javax.annotation.Nonnull;
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

        loadMusic();
    }

    void onTryAgainButtonClicked() {
        loadMusic();
    }

    void onFolderClicked(@Nonnull String filePath) {
        getViewState().goToMusicStorageScreen(filePath);
    }

    void onCompositionClicked(Composition composition) {
        interactor.playMusic(composition);
    }

    void onPlayAllButtonClicked() {
        interactor.playAllMusicInPath(path);
    }

    void onBackPathButtonClicked() {
        if (path == null) {
            throw new IllegalStateException("can not go back in root screen");
        }
        String targetPath = null;
        int lastSlashIndex = path.lastIndexOf('/');
        if (lastSlashIndex != -1) {
            targetPath = path.substring(0, lastSlashIndex);
        }
        getViewState().goToMusicStorageScreen(targetPath);
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
        getViewState().showMusicList(musicList);
    }

    private void onMusicLoadingError(Throwable throwable) {
        ErrorCommand errorCommand = errorParser.parseError(throwable);
        getViewState().showError(errorCommand);
    }
}