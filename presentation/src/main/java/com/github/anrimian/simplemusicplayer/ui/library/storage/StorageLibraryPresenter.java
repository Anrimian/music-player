package com.github.anrimian.simplemusicplayer.ui.library.storage;

import com.arellomobile.mvp.InjectViewState;
import com.arellomobile.mvp.MvpPresenter;
import com.github.anrimian.simplemusicplayer.di.Components;
import com.github.anrimian.simplemusicplayer.domain.business.music.StorageLibraryInteractor;
import com.github.anrimian.simplemusicplayer.domain.models.Composition;
import com.github.anrimian.simplemusicplayer.domain.utils.tree.FileTree;
import com.github.anrimian.simplemusicplayer.domain.models.MusicFileSource;
import com.github.anrimian.simplemusicplayer.utils.error.ErrorCommand;
import com.github.anrimian.simplemusicplayer.utils.error.parser.ErrorParser;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;

import io.reactivex.android.schedulers.AndroidSchedulers;

import static com.github.anrimian.simplemusicplayer.di.app.ErrorModule.STORAGE_ERROR_PARSER;

/**
 * Created on 23.10.2017.
 */

@InjectViewState
public class StorageLibraryPresenter extends MvpPresenter<StorageLibraryView> {

    private StorageLibraryInteractor interactor;
    private ErrorParser errorParser;

    @Nullable
    private String path;

    public StorageLibraryPresenter(@Nullable String path,
                                   StorageLibraryInteractor interactor,
                                   ErrorParser errorParser) {
        this.path = path;
        this.interactor = interactor;
        this.errorParser = errorParser;
    }

    @Override
    protected void onFirstViewAttach() {
        super.onFirstViewAttach();
        loadMusic();
        getViewState().showBackPathButton(path);
    }

    void onTryAgaintButtonClicked() {
        loadMusic();
    }

    void onMusicSourceClicked(MusicFileSource musicFileSource) {
        if (musicFileSource.getComposition() == null) {
            getViewState().goToMusicStorageScreen(path + musicFileSource.getPath());
        } else {
            interactor.playMusic(musicFileSource);
        }
    }

    void onPlayAllButtonClicked() {
        interactor.playAllMusicInPath(path);
    }

    void onBackButtonClicked() {

    }

    private void loadMusic() {
        getViewState().showLoading();
        interactor.getMusicInPath(path)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onMusicLoaded, this::onMusicLoadingError);
    }

    private void onMusicLoaded(List<MusicFileSource> musicList) {
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
