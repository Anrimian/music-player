package com.github.anrimian.simplemusicplayer.domain.business.music;

import com.github.anrimian.simplemusicplayer.domain.models.Composition;
import com.github.anrimian.simplemusicplayer.domain.models.MusicFileSource;
import com.github.anrimian.simplemusicplayer.domain.repositories.MusicProviderRepository;
import com.github.anrimian.simplemusicplayer.domain.utils.FileTree;

import javax.annotation.Nullable;

import io.reactivex.Single;

/**
 * Created on 24.10.2017.
 */

public class MusicProviderInteractorImpl implements MusicProviderInteractor {

    @Nullable
    private FileTree<MusicFileSource> musicFileTree;

    private MusicProviderRepository musicProviderRepository;

    public MusicProviderInteractorImpl(MusicProviderRepository musicProviderRepository) {
        this.musicProviderRepository = musicProviderRepository;
    }

    @Override
    public Single<FileTree<MusicFileSource>> getAllMusicInPath(@Nullable String path) {
        return getMusicFileTree();
    }

    private Single<FileTree<MusicFileSource>> getMusicFileTree() {
        if (musicFileTree == null) {
            return createMusicFileTree();
        } else {
            return Single.just(musicFileTree);
        }
    }

    private Single<FileTree<MusicFileSource>> createMusicFileTree() {
        return musicProviderRepository.getAllCompositions()
                .map(compositions -> {
                    musicFileTree = new FileTree<>(null);

                    for (Composition composition: compositions) {
                        String filePath = composition.getFilePath();
                        MusicFileSource musicFileSource = new MusicFileSource();
                        musicFileSource.setComposition(composition);
                        musicFileTree.addFile(musicFileSource, filePath);
                    }
                    return musicFileTree;
                })
                .map(this::removeUnusedRootComponents);
    }

    private FileTree<MusicFileSource> removeUnusedRootComponents(FileTree<MusicFileSource> tree) {
        FileTree<MusicFileSource> root = tree;
        while (root.getData() == null && root.getChildCount() <= 1) {
            root = root.getFirstChild();
        }
        return root;
    }
}
