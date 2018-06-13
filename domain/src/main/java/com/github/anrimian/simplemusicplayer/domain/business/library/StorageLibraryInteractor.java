package com.github.anrimian.simplemusicplayer.domain.business.library;

import com.github.anrimian.simplemusicplayer.domain.business.player.MusicPlayerInteractor;
import com.github.anrimian.simplemusicplayer.domain.models.composition.Composition;
import com.github.anrimian.simplemusicplayer.domain.models.exceptions.FileNodeNotFoundException;
import com.github.anrimian.simplemusicplayer.domain.models.files.FileSource;
import com.github.anrimian.simplemusicplayer.domain.models.files.FolderFileSource;
import com.github.anrimian.simplemusicplayer.domain.models.files.MusicFileSource;
import com.github.anrimian.simplemusicplayer.domain.repositories.MusicProviderRepository;
import com.github.anrimian.simplemusicplayer.domain.utils.tree.FileTree;
import com.github.anrimian.simplemusicplayer.domain.utils.tree.visitors.CollectVisitor;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.Nullable;

import io.reactivex.Completable;
import io.reactivex.Single;

/**
 * Created on 24.10.2017.
 */

public class StorageLibraryInteractor {

    @Nullable
    private FileTree<Composition> musicFileTree;

    private MusicProviderRepository musicProviderRepository;
    private MusicPlayerInteractor musicPlayerInteractor;

    public StorageLibraryInteractor(MusicProviderRepository musicProviderRepository,
                                    MusicPlayerInteractor musicPlayerInteractor) {
        this.musicProviderRepository = musicProviderRepository;
        this.musicPlayerInteractor = musicPlayerInteractor;
    }

    public Completable playAllMusicInPath(@Nullable String path) {
        return getMusicFileTree().map(tree -> findNodeByPath(tree, path))
                .map(this::getAllCompositions)
                .flatMapCompletable(musicPlayerInteractor::startPlaying);
    }

    public Single<List<FileSource>> getMusicInPath(@Nullable String path) {
        return getMusicFileTree()
                .map(tree -> getFilesListByPath(tree, path))
                .map(this::applyOrder);
    }

    public Completable playMusic(Composition composition) {
        List<Composition> list = new ArrayList<>();
        list.add(composition);
        return musicPlayerInteractor.startPlaying(list);
    }

    private List<FileSource> applyOrder(List<FileSource> FileSources) {
        List<FileSource> sortedList = new ArrayList<>();
        List<FileSource> musicList = new ArrayList<>();
        for (FileSource fileSource: FileSources) {
            if (fileSource instanceof FolderFileSource) {
                sortedList.add(fileSource);
            } else {
                musicList.add(fileSource);
            }
        }
        sortedList.addAll(musicList);
        return sortedList;
    }

    private List<Composition> getAllCompositions(FileTree<Composition> compositionFileTree) {
        List<Composition> compositions = new LinkedList<>();
        compositionFileTree.accept(new CollectVisitor<>(compositions));
        return compositions;
    }

    private List<FileSource> getFilesListByPath(FileTree<Composition> tree, @Nullable String path) {
        FileTree<Composition> compositionFileTree = findNodeByPath(tree, path);
        List<FileSource> musicList = new ArrayList<>();
        for (FileTree<Composition> node : compositionFileTree.getChildren()) {
            FileSource fileSource;
            Composition data = node.getData();
            if (data == null) {
                fileSource = new FolderFileSource(tree.getFullPathOfNode(node), node.getDataChildCount());
            } else {
                fileSource = new MusicFileSource(data);
            }
            musicList.add(fileSource);
        }
        return musicList;
    }

    private FileTree<Composition> findNodeByPath(FileTree<Composition> tree, @Nullable String path) {
        FileTree<Composition> result = tree.findNodeByPath(path);
        if (result == null) {
            throw new FileNodeNotFoundException("node not found for path: " + path);
        }
        return result;
    }

    private Single<FileTree<Composition>> getMusicFileTree() {
        if (musicFileTree == null) {
            return createMusicFileTree()
                    .doOnSuccess(musicFileTree -> this.musicFileTree = musicFileTree);
        } else {
            return Single.just(musicFileTree);
        }
    }

    private Single<FileTree<Composition>> createMusicFileTree() {
        return musicProviderRepository.getAllCompositions()
                .map(this::filterCorruptedCompositions)
                .map(this::createTree)
                .map(this::removeUnusedRootComponents);
    }

    private List<Composition> filterCorruptedCompositions(List<Composition> compositions) {
        for (Composition composition: new ArrayList<>(compositions)) {
            if (composition.getDuration() <= 0) {
                System.out.println("corrupted composition: " + composition);//TODO another tracking for this
                compositions.remove(composition);
            }
        }
        return compositions;
    }

    private FileTree<Composition> createTree(List<Composition> compositions) {
        FileTree<Composition> musicFileTree = new FileTree<>(null);
        for (Composition composition: compositions) {
            String filePath = composition.getFilePath();
            musicFileTree.addFile(composition, filePath);
        }
        return musicFileTree;
    }

    private FileTree<Composition> removeUnusedRootComponents(FileTree<Composition> tree) {
        FileTree<Composition> root = tree;
        while (root.getData() == null && root.getChildCount() <= 1) {
            root = root.getFirstChild();
        }
        return root;
    }
}
