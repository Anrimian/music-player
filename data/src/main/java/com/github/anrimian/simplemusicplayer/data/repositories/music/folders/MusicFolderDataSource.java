package com.github.anrimian.simplemusicplayer.data.repositories.music.folders;

import com.github.anrimian.simplemusicplayer.data.storage.StorageMusicDataSource;
import com.github.anrimian.simplemusicplayer.data.utils.folders.RxNode;
import com.github.anrimian.simplemusicplayer.domain.models.composition.Composition;
import com.github.anrimian.simplemusicplayer.domain.models.files.FileSource;
import com.github.anrimian.simplemusicplayer.domain.models.files.FolderFileSource;
import com.github.anrimian.simplemusicplayer.domain.models.files.MusicFileSource;
import com.github.anrimian.simplemusicplayer.domain.utils.tree.FileTree;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import io.reactivex.Single;

public class MusicFolderDataSource {

    private final StorageMusicDataSource storageMusicDataSource;

    @Nullable
    private RxNode<String, Composition> root;

    public MusicFolderDataSource(StorageMusicDataSource storageMusicDataSource) {
        this.storageMusicDataSource = storageMusicDataSource;
    }

    public Single<List<FileSource>> getMusicInPath(@Nullable String path) {
        return getMusicFileTree()
                .map(tree -> getFilesListByPath(path));
    }

    private List<FileSource> getFilesListByPath(@Nullable String path) {
        RxNode<String, Composition> targetNode = findNodeByPath(path);
        List<FileSource> musicList = new ArrayList<>();
        for (RxNode<String, Composition> node : targetNode.getNodes()) {
            FileSource fileSource;
            Composition data = node.getData();
            if (data == null) {
                fileSource = new FolderFileSource(node.getKey(), 0);
            } else {
                fileSource = new MusicFileSource(data);
            }
            musicList.add(fileSource);
        }
        return musicList;
    }

    private RxNode<String, Composition> findNodeByPath(@Nullable String fullPath) {
        if (fullPath == null) {
            return root;
        }
        RxNode<String, Composition> target = root;
        RxNode<String, Composition> found = null;
        for (String partialPath: fullPath.split("/")) {
            found = target.getChild(partialPath);
            if (found != null) {
                target = found;
            }
        }
        return found;
    }

    private Single<RxNode<String, Composition>> getMusicFileTree() {
        return Single.fromCallable(() -> {
            if (root == null) {
                root = createMusicFileTree();
            }
            return root;
        });
    }

    private RxNode<String, Composition> createMusicFileTree() {
        RxNode<String, Composition> root = new RxNode<>(null, null);
        for (Composition composition: storageMusicDataSource.getCompositionsMap().values()) {
            String path = composition.getFilePath();

            getParentForPath(root, path, (node, partialPath) ->
                    node.addNode(new RxNode<>(partialPath, composition))
            );
        }
        return root;
    }

    private void getParentForPath(RxNode<String, Composition> root,
                                  String path,
                                  NodeCallback nodeCallback) {
        RxNode<String, Composition> target = root;

        String[] partialPaths = path.split("/");
        for (int i = 0; i < partialPaths.length - 1; i++) {
            String partialPath = partialPaths[i];
            RxNode<String, Composition> child = target.getChild(partialPath);
            if (child == null) {
                child = new RxNode<>(partialPath, null);
                target.addNode(child);
            }
            target = child;
        }
        nodeCallback.onNodeFound(target, partialPaths[partialPaths.length - 1]);
    }

    private interface NodeCallback {

        void onNodeFound(RxNode<String, Composition> node, String partialPath);
    }

}
