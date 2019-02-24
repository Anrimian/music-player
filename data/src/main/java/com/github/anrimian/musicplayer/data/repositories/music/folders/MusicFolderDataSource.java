package com.github.anrimian.musicplayer.data.repositories.music.folders;

import com.github.anrimian.musicplayer.data.models.exceptions.FolderNodeNonExistException;
import com.github.anrimian.musicplayer.data.storage.providers.music.StorageMusicDataSource;
import com.github.anrimian.musicplayer.data.utils.FileUtils;
import com.github.anrimian.musicplayer.data.utils.folders.NodeData;
import com.github.anrimian.musicplayer.data.utils.folders.RxNode;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.composition.folders.Folder;
import com.github.anrimian.musicplayer.domain.models.exceptions.StorageTimeoutException;
import com.github.anrimian.musicplayer.domain.utils.changes.Change;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;

import io.reactivex.Single;

import static com.github.anrimian.musicplayer.data.utils.FileUtils.getFileName;
import static com.github.anrimian.musicplayer.data.utils.FileUtils.getParentDirPath;
import static com.github.anrimian.musicplayer.domain.Constants.TIMEOUTS.STORAGE_LOADING_TIMEOUT_SECONDS;
import static com.github.anrimian.musicplayer.domain.utils.ListUtils.mapList;
import static com.github.anrimian.musicplayer.domain.utils.TextUtils.indexOfEnd;
import static io.reactivex.Observable.fromIterable;

public class MusicFolderDataSource {

    private final FolderMapper folderMapper = new FolderMapper();

    private final StorageMusicDataSource storageMusicDataSource;

    private Map<Long, String> pathIdMap = new HashMap<>();

    @Nullable
    private RxNode<String> root;

    public MusicFolderDataSource(StorageMusicDataSource storageMusicDataSource) {
        this.storageMusicDataSource = storageMusicDataSource;
    }

    public Single<Folder> getCompositionsInPath(@Nullable String path) {
        return getMusicFileTree()
                .map(tree -> getFolderInPath(path, tree));
    }

    public Single<List<String>> getAvailablePathsForPath(@Nullable String path) {
        return getMusicFileTree()
                .map(tree -> {
                    List<String> paths = new ArrayList<>();
                    paths.add(null);//root is always here
                    if (path == null) {//empty string?
                        return paths;
                    }

                    RxNode<String> root = findRootNode(tree);
                    FolderNode folderNode = (FolderNode) root.getData();
                    String rootPath = folderNode.getFullPath();
                    int lastIndexOfRootPath = indexOfEnd(path, rootPath);
                    RxNode<String> currentNode = root;
                    if (lastIndexOfRootPath != -1) {
                        //+1 - remove slash at start
                        String secondaryPaths = path.substring(lastIndexOfRootPath + 1, path.length());
                        for (String partialPath: secondaryPaths.split("/")) {
                            RxNode<String> child = currentNode.getChild(partialPath);
                            if (child == null) {
                                break;
                            }
                            NodeData nodeData = child.getData();
                            if (nodeData instanceof FolderNode) {
                                paths.add(((FolderNode) nodeData).getFullPath());
                                currentNode = child;
                            }
                        }
                    }

                    return paths;
                });
    }


    private Folder getFolderInPath(@Nullable String path, RxNode<String> root) {
        RxNode<String> node = findNodeByPath(path, root);
        if (node == null) {
            throw new FolderNodeNonExistException();
        }
        return folderMapper.toFolder(node);
    }

    @Nullable
    private RxNode<String> findNodeByPath(@Nullable String fullPath, RxNode<String> root) {
        if (fullPath == null) {
            return findRootNode(root);
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

    private RxNode<String> findRootNode(RxNode<String> root) {
        RxNode<String> found = root;
        while (isEmptyFolderNode(found)) {
            found = found.getNodes().get(0);
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
                subscribeOnCompositionsChanging();
            }
            return root;
        })//.delay(3, TimeUnit.SECONDS)//test timeout error
                .timeout(STORAGE_LOADING_TIMEOUT_SECONDS,
                        TimeUnit.SECONDS,
                        Single.error(new StorageTimeoutException()));
    }

    private void subscribeOnCompositionsChanging() {
        storageMusicDataSource.getChangeObservable()
                .doOnNext(this::onCompositionsChanged)
                .subscribe();
    }

    private void onCompositionsChanged(Change<List<Composition>> change) {
        switch (change.getChangeType()) {
            case ADDED: {
                processAddChange(change.getData());
                break;
            }
            case DELETED: {
                processDeleteChange(change.getData());
                break;
            }
            case MODIFY: {
                processModifyChange(change.getData());
                break;
            }
        }
    }

    private void processModifyChange(List<Composition> compositions) {
        for (Composition composition : compositions) {
            String path = pathIdMap.get(composition.getId());
            if (path != null && !path.equals(composition.getFilePath())) {
                RxNode<String> node = findNodeByPath(getParentDirPath(path), root);
                if (node != null) {
                    node.removeNode(getFileName(composition.getFilePath()));
                }
            }
            pathIdMap.put(composition.getId(), composition.getFilePath());
            getParentForPath(root, composition.getFilePath(), (node, partialPath) ->
                    node.updateNode(partialPath, new CompositionNode(composition))
            );
        }
    }

    private void processDeleteChange(List<Composition> compositions) {
        fromIterable(compositions)
                .doOnNext(composition -> pathIdMap.remove(composition.getId()))
                .groupBy(composition -> getParentDirPath(composition.getFilePath()))
                .doOnNext(group -> group.map(Composition::getFilePath)
                        .map(FileUtils::getFileName)
                        .collect(ArrayList<String>::new, List::add)
                        .doOnSuccess(pathCompositions ->
                                removeCompositions(group.getKey(), pathCompositions))
                        .subscribe())
                .subscribe();
    }

    private void removeCompositions(String path, List<String> paths) {
        RxNode<String> node = findNodeByPath(path, root);
        if (node != null) {
            node.removeNodes(paths);

            clearEmptyNode(node);
        }
    }

    private void clearEmptyNode(RxNode<String> node) {
        if (node.getNodes().isEmpty()) {
            RxNode<String> parent = node.getParent();
            if (parent != null) {
                parent.removeNode(node.getKey());
                clearEmptyNode(parent);
            }
        }
    }

    private void processAddChange(List<Composition> compositions) {
        fromIterable(compositions)
                .doOnNext(composition -> pathIdMap.put(composition.getId(), composition.getFilePath()))
                .groupBy(composition -> getParentDirPath(composition.getFilePath()))
                .doOnNext(group -> group.collect(ArrayList<Composition>::new, List::add)
                        .doOnSuccess(pathCompositions ->
                                putCompositions(root, group.getKey(), pathCompositions))
                        .subscribe())
                .subscribe();
    }

    private void putCompositions(RxNode<String> root, String path, List<Composition> compositions) {
        getNode(root, path, (node, partialPath) -> node.addNodes(toNodeList(compositions)));
    }

    private List<RxNode<String>> toNodeList(List<Composition> compositions) {
        return mapList(compositions, newComposition -> new RxNode<>(
                        getFileName(newComposition.getFilePath()),
                        new CompositionNode(newComposition)
                )
        );
    }

    private RxNode<String> createMusicFileTree() {
        RxNode<String> root = new RxNode<>(null, null);
        fromIterable(storageMusicDataSource.getCompositionsMap().values())
                .doOnNext(composition -> pathIdMap.put(composition.getId(), composition.getFilePath()))
                .groupBy(composition -> getParentDirPath(composition.getFilePath()))
                .doOnNext(group -> group.collect(ArrayList<Composition>::new, List::add)
                        .doOnSuccess(pathCompositions ->
                                putCompositions(root, group.getKey(), pathCompositions))
                        .subscribe())
                .subscribe();
        return root;
    }

    private void getNode(RxNode<String> root,
                         String path,
                         NodeCallback nodeCallback) {
        RxNode<String> target = root;

        String[] partialPaths = path.split("/");
        for (int i = 0; i < partialPaths.length; i++) {
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
