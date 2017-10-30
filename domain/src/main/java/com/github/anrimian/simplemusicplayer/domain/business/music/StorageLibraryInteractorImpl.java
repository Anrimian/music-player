package com.github.anrimian.simplemusicplayer.domain.business.music;

import com.github.anrimian.simplemusicplayer.domain.models.Composition;
import com.github.anrimian.simplemusicplayer.domain.models.MusicFileSource;
import com.github.anrimian.simplemusicplayer.domain.models.exceptions.FileNodeNotFoundException;
import com.github.anrimian.simplemusicplayer.domain.repositories.MusicProviderRepository;
import com.github.anrimian.simplemusicplayer.domain.utils.tree.FileTree;
import com.github.anrimian.simplemusicplayer.domain.utils.tree.visitors.CollectVisitor;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.Nullable;

import io.reactivex.Single;

/**
 * Created on 24.10.2017.
 */

public class StorageLibraryInteractorImpl implements StorageLibraryInteractor {

    @Nullable
    private FileTree<Composition> musicFileTree;

    private MusicProviderRepository musicProviderRepository;

    public StorageLibraryInteractorImpl(MusicProviderRepository musicProviderRepository) {
        this.musicProviderRepository = musicProviderRepository;
    }

    @Override
    public void playAllMusicInPath(@Nullable String path) {
        getAllMusicInPath(path).map(this::toList);
        //TODO play music
    }

    @Override
    public Single<List<MusicFileSource>> getMusicInPath(@Nullable String path) {
        return getAllMusicInPath(path)
                .map(this::getFilesOnTop);
    }

    @Override
    public void playMusic(MusicFileSource musicFileSource) {
        //TODO play music
    }

    private Single<FileTree<Composition>> getAllMusicInPath(@Nullable String path) {
        return getMusicFileTree()
                .map(tree -> findNodeByPath(tree, path));
    }

    private List<Composition> toList(FileTree<Composition> compositionFileTree) {
        List<Composition> compositions = new LinkedList<>();
        compositionFileTree.accept(new CollectVisitor<>(compositions));
        return compositions;
    }

    private List<MusicFileSource> getFilesOnTop(FileTree<Composition> compositionFileTree) {
        List<MusicFileSource> musicList = new ArrayList<>();
        for (FileTree<Composition> node : compositionFileTree.getChildren()) {
            MusicFileSource musicFileSource = new MusicFileSource();
            musicFileSource.setComposition(node.getData());
            musicFileSource.setPath(node.getPath());
            musicList.add(musicFileSource);
        }
        return musicList;
    }

    private FileTree<Composition> findNodeByPath(FileTree<Composition> tree, @Nullable String path) {
        if (path == null) {
            return tree;
        }
        FileTree<Composition> result = tree.findNodeByPath(path);
        if (result == null) {
            throw new FileNodeNotFoundException("node not found for path: " + path);
        }
        return result;
    }

    private Single<FileTree<Composition>> getMusicFileTree() {
        if (musicFileTree == null) {
            return createMusicFileTree().doOnSuccess(musicFileTree -> this.musicFileTree = musicFileTree);
        } else {
            return Single.just(musicFileTree);
        }
    }

    private Single<FileTree<Composition>> createMusicFileTree() {
        return musicProviderRepository.getAllCompositions()
                .map(compositions -> {
                    FileTree<Composition> musicFileTree = new FileTree<>(null);

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
