package com.github.anrimian.simplemusicplayer.domain.business.music;

import com.github.anrimian.simplemusicplayer.domain.models.Composition;
import com.github.anrimian.simplemusicplayer.domain.models.exceptions.FileNodeNotFoundException;
import com.github.anrimian.simplemusicplayer.domain.repositories.MusicProviderRepository;
import com.github.anrimian.simplemusicplayer.domain.utils.tree.FileTree;

import javax.annotation.Nullable;

import io.reactivex.Single;

/**
 * Created on 24.10.2017.
 */

public class MusicProviderInteractorImpl implements MusicProviderInteractor {

    @Nullable
    private FileTree<Composition> musicFileTree;

    private MusicProviderRepository musicProviderRepository;

    public MusicProviderInteractorImpl(MusicProviderRepository musicProviderRepository) {
        this.musicProviderRepository = musicProviderRepository;
    }

    @Override
    public Single<FileTree<Composition>> getAllMusicInPath(@Nullable String path) {
        return getMusicFileTree()
                .map(tree -> findNodeByPath(tree, path));
    }

    private FileTree<Composition> findNodeByPath(FileTree<Composition> tree, @Nullable String path) {
        if (path == null) {
            return tree;
        }
        FileTree<Composition> result = tree.findNodeByPath(path);
        if (result == null) {
            throw new FileNodeNotFoundException();
        }
        return result;
    }

    private Single<FileTree<Composition>> getMusicFileTree() {
        if (musicFileTree == null) {
            return createMusicFileTree();
        } else {
            return Single.just(musicFileTree);
        }
    }

    private Single<FileTree<Composition>> createMusicFileTree() {
        return musicProviderRepository.getAllCompositions()
                .map(compositions -> {
                    musicFileTree = new FileTree<>(null);

                    for (Composition composition: compositions) {
                        String filePath = composition.getFilePath();
                        musicFileTree.addFile(composition, filePath);
                    }
                    return musicFileTree;
                })
                .map(this::removeUnusedRootComponents);
    }

    private FileTree<Composition> removeUnusedRootComponents(FileTree<Composition> tree) {
        FileTree<Composition> root = tree;
        while (root.getData() == null && root.getChildCount() <= 1) {
            root = root.getFirstChild();
        }
        return root;
    }
}
