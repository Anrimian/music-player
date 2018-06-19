package com.github.anrimian.simplemusicplayer.data.repositories.music.folders;

import com.github.anrimian.simplemusicplayer.data.storage.StorageMusicDataSource;
import com.github.anrimian.simplemusicplayer.data.utils.folders.NodeData;
import com.github.anrimian.simplemusicplayer.data.utils.folders.RxNode;
import com.github.anrimian.simplemusicplayer.domain.models.composition.Composition;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import io.reactivex.Single;

public class MusicFolderDataSource {

    private final StorageMusicDataSource storageMusicDataSource;

    @Nullable
    private RxNode<String> root;

    public MusicFolderDataSource(StorageMusicDataSource storageMusicDataSource) {
        this.storageMusicDataSource = storageMusicDataSource;
    }

    public Single<List<NodeData>> getMusicInPath(@Nullable String path) {
        return getMusicFileTree()
                .map(tree -> getFilesListByPath(path));
    }

    private List<NodeData> getFilesListByPath(@Nullable String path) {
        RxNode<String> targetNode = findNodeByPath(path);
        List<NodeData> fileList = new ArrayList<>();
        for (RxNode<String> node : targetNode.getNodes()) {
            fileList.add(node.getData());
        }
        return fileList;
    }

    private RxNode<String> findNodeByPath(@Nullable String fullPath) {
        if (fullPath == null) {
            RxNode<String> found = root;
            while (isEmptyFolderNode(found)) {
                found = found.getNodes().get(0);
            }
            return found;
        }
        RxNode<String> target = root;
        RxNode<String> found = null;
        for (String partialPath: fullPath.split("/")) {
            found = target.getChild(partialPath);
            if (found != null) {
                target = found;
            }
        }
        return found;
    }

    private boolean isEmptyFolderNode(RxNode<String> node) {
        List<RxNode<String>> nodes = node.getNodes();
        return nodes.size() == 1 && nodes.get(0).getData() instanceof FolderNode;
    }

    private Single<RxNode<String>> getMusicFileTree() {
        return Single.fromCallable(() -> {
            if (root == null) {
                root = createMusicFileTree();
            }
            return root;
        });
    }

    private RxNode<String> createMusicFileTree() {
        RxNode<String> root = new RxNode<>(null, null);
        for (Composition composition: storageMusicDataSource.getCompositionsMap().values()) {
            String path = composition.getFilePath();

            getParentForPath(root, path, (node, partialPath) ->
                    node.addNode(new RxNode<>(partialPath, new CompositionNode(composition)))
            );
        }
        return root;
    }

    private void getParentForPath(RxNode<String> root,
                                  String path,
                                  NodeCallback nodeCallback) {
        RxNode<String> target = root;

        String[] partialPaths = path.split("/");
        for (int i = 0; i < partialPaths.length - 1; i++) {
            String partialPath = partialPaths[i];
            RxNode<String> child = target.getChild(partialPath);
            if (child == null) {
                String folderPath = getPath(partialPaths, i);
                child = new RxNode<>(partialPath, new FolderNode(folderPath));
                target.addNode(child);
            }
            target = child;
        }
        nodeCallback.onNodeFound(target, partialPaths[partialPaths.length - 1]);
    }

    private String getPath(String[] paths, int index) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i <= index; i++) {
            if (i != 0) {
                sb.append("/");
            }
            sb.append(paths[i]);
        }
        return sb.toString();
    }

    private interface NodeCallback {

        void onNodeFound(RxNode<String> node, String partialPath);
    }

}
