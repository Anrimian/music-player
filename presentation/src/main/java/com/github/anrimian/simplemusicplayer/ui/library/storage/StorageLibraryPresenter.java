package com.github.anrimian.simplemusicplayer.ui.library.storage;

import com.arellomobile.mvp.InjectViewState;
import com.arellomobile.mvp.MvpPresenter;
import com.github.anrimian.simplemusicplayer.di.Components;
import com.github.anrimian.simplemusicplayer.domain.business.music.MusicProviderInteractor;
import com.github.anrimian.simplemusicplayer.domain.models.Composition;
import com.github.anrimian.simplemusicplayer.domain.utils.tree.FileTree;
import com.github.anrimian.simplemusicplayer.ui.library.storage.models.MusicFileSource;
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

    @Inject
    MusicProviderInteractor interactor;

    @Inject
    @Named(STORAGE_ERROR_PARSER)
    ErrorParser errorParser;

    @Nullable
    private String path;

    public StorageLibraryPresenter(@Nullable String path) {
        Components.getLibraryComponent().inject(this);
        this.path = path;
    }

    @Override
    protected void onFirstViewAttach() {
        super.onFirstViewAttach();
        loadMusic();
    }

    void onTryAgaintButtonClicked() {
        loadMusic();
    }

    void onMusicSourceClicked(MusicFileSource musicFileSource) {
        if (musicFileSource.getComposition() == null) {
            getViewState().goToMusicStorageScreen(musicFileSource.getPath());
        } else {
            //TODO start playing music
        }
    }

    private void loadMusic() {
        getViewState().showLoading();
        interactor.getAllMusicInPath(path)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onMusicLoaded, this::onMusicLoadingError);
    }

    private void onMusicLoaded(FileTree<Composition> compositionFileTree) {
        if (compositionFileTree.isEmpty()) {
            getViewState().showEmptyList();
            return;
        }
        List<MusicFileSource> musicList = new ArrayList<>();//TODO maybe move this to interactor?
        for (FileTree<Composition> node : compositionFileTree.getChildren()) {
            MusicFileSource musicFileSource = new MusicFileSource();
            musicFileSource.setComposition(node.getData());
            musicFileSource.setPath(node.getPath());
            musicList.add(musicFileSource);
        }
        getViewState().showMusicList(musicList);
    }

    private void onMusicLoadingError(Throwable throwable) {
        ErrorCommand errorCommand = errorParser.parseError(throwable);
        getViewState().showError(errorCommand);
    }
}
