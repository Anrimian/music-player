package com.github.anrimian.musicplayer.data.repositories.music.folders;

import com.github.anrimian.musicplayer.data.models.exceptions.FolderNodeNonExistException;
import com.github.anrimian.musicplayer.data.storage.providers.music.StorageMusicDataSource;
import com.github.anrimian.musicplayer.data.utils.folders.NodeData;
import com.github.anrimian.musicplayer.data.utils.folders.RxNode;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.composition.folders.FileSource;
import com.github.anrimian.musicplayer.domain.models.composition.folders.Folder;
import com.github.anrimian.musicplayer.domain.models.composition.folders.FolderFileSource;
import com.github.anrimian.musicplayer.domain.models.composition.folders.MusicFileSource;
import com.github.anrimian.musicplayer.domain.models.exceptions.StorageTimeoutException;
import com.github.anrimian.musicplayer.domain.utils.FileUtils;
import com.github.anrimian.musicplayer.domain.utils.changes.Change;
import com.github.anrimian.musicplayer.domain.utils.changes.ModifiedData;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;

import io.reactivex.Single;

import static com.github.anrimian.musicplayer.domain.Constants.TIMEOUTS.STORAGE_LOADING_TIMEOUT_SECONDS;
import static com.github.anrimian.musicplayer.domain.utils.FileUtils.getFileName;
import static com.github.anrimian.musicplayer.domain.utils.FileUtils.getParentDirPath;
import static com.github.anrimian.musicplayer.domain.utils.ListUtils.mapList;
import static com.github.anrimian.musicplayer.domain.utils.TextUtils.getLastPathSegment;
import static com.github.anrimian.musicplayer.domain.utils.TextUtils.indexOfEnd;
import static io.reactivex.Observable.fromIterable;

public class MusicFolderDataSource {

    private final FolderMapper folderMapper = new FolderMapper();

    private final StorageMusicDataSource storageMusicDataSource;

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
                        String secondaryPaths = path.substring(lastIndexOfRootPath + 1);
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

    public Single<List<Composition>> changeFolderName(String folderPath, String newPath) {
        return getMusicFileTree()
                .map(tree -> {
                    List<Composition> affectedCompositions = new LinkedList<>();

                    RxNode<String> node = findNodeByPath(folderPath, tree);
                    if (node != null) {
                        String newName = getLastPathSegment(newPath);
                        node.updateKey(newName);

                        //update paths
                        NodeData nodeData = node.getData();
                        if (nodeData instanceof FolderNode) {
                            FolderNode folderNode = (FolderNode) nodeData;
                            nodeData = new FolderNode(newPath,
                                    folderNode.getCompositionsCount(),
                                    folderNode.getLatestCreateDate(),
                                    folderNode.getEarliestCreateDate());
                            node.updateData(nodeData);
                        }
                        updateNodesPath(node, folderPath, newPath, affectedCompositions);
                    }
                    return affectedCompositions;
                });
    }

    public Single<List<Composition>> moveCompositionsTo(String fromFolderPath,
                                                        String toFolderPath,
                                                        List<FileSource> fileSources) {
        return getMusicFileTree()
                .map(tree -> {
                    List<Composition> affectedCompositions = new LinkedList<>();

                    RxNode<String> targetNode = findNodeByPath(toFolderPath, tree);
                    if (targetNode == null) {
                        String key = getLastPathSegment(toFolderPath);
                        targetNode = new RxNode<>(key, new FolderNode(toFolderPath));
                    }

                    for (FileSource fileSource: fileSources) {
                        String path = getPath(fileSource);
                        RxNode<String> node = findNodeByPath(path, tree);
                        if (node != null) {
                            RxNode<String> parentNode = node.getParent();
                            if (parentNode != null) {
                                parentNode.removeNode(node.getKey());
                            }
                            targetNode.addNode(node);

                            updateNodePaths(node, fromFolderPath, toFolderPath, affectedCompositions);
                        }
                    }
                    return affectedCompositions;
                });
    }

    private void updateNodePaths(RxNode<String> node,
                                 String fromFolderPath,
                                 String toFolderPath,
                                 List<Composition> outAffectedCompositions) {
        NodeData nodeData = node.getData();
        if (nodeData instanceof CompositionNode) {
            CompositionNode compositionNode = (CompositionNode) nodeData;
            Composition composition = compositionNode.getComposition();
            composition = composition.copy(composition.getFilePath().replace(fromFolderPath, toFolderPath));
            node.updateData(new CompositionNode(composition));
            outAffectedCompositions.add(composition);
        }
        if (nodeData instanceof FolderNode) {
            FolderNode folderNode = (FolderNode) nodeData;
            nodeData = new FolderNode(folderNode.getFullPath().replace(fromFolderPath, toFolderPath),
                    folderNode.getCompositionsCount(),
                    folderNode.getLatestCreateDate(),
                    folderNode.getEarliestCreateDate());
            node.updateData(nodeData);

            updateNodesPath(node, fromFolderPath, toFolderPath, outAffectedCompositions);
        }
    }

    private String getPath(FileSource fileSource) {
        if (fileSource instanceof FolderFileSource) {
            return ((FolderFileSource) fileSource).getFullPath();
        }
        if (fileSource instanceof MusicFileSource) {
            return ((MusicFileSource) fileSource).getComposition().getFilePath();
        }
        throw new IllegalStateException("unexpected file source: " + fileSource);
    }

    private void updateNodesPath(RxNode<String> parentNode,
                                 String folderPath,
                                 String newPath,
                                 List<Composition> outAffectedCompositions) {
        for (RxNode<String> node: parentNode.getNodes()) {
            Composition affectedComposition = updateNodePathData(node, folderPath, newPath);
            if (affectedComposition != null) {
                outAffectedCompositions.add(affectedComposition);
            }
            updateNodesPath(node, folderPath, newPath, outAffectedCompositions);
        }
    }

    @Nullable
    private Composition updateNodePathData(RxNode<String> node, String oldPath, String newPath) {
        NodeData nodeData = node.getData();
        if (nodeData instanceof FolderNode) {
            FolderNode folderNode = (FolderNode) nodeData;
            nodeData = new FolderNode(folderNode.getFullPath().replace(oldPath, newPath),
                    folderNode.getCompositionsCount(),
                    folderNode.getLatestCreateDate(),
                    folderNode.getEarliestCreateDate());
            node.updateData(nodeData);
            return null;
        }
        if (nodeData instanceof CompositionNode) {
            CompositionNode compositionNode = (CompositionNode) nodeData;
            Composition composition = compositionNode.getComposition();
            composition = composition.copy(composition.getFilePath().replace(oldPath, newPath));
            node.updateData(new CompositionNode(composition));
            return composition;
        }
        return null;
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

    private void onCompositionsChanged(Change<Composition> change) {
        if (change instanceof Change.AddChange) {
            processAddChange(((Change.AddChange<Composition>) change).getData());
            return;
        }
        if (change instanceof Change.DeleteChange) {
            processDeleteChange(((Change.DeleteChange<Composition>) change).getData());
            return;
        }
        if (change instanceof Change.ModifyChange) {
            processModifyChange(((Change.ModifyChange<Composition>) change).getData());
        }
    }

    private void processModifyChange(List<ModifiedData<Composition>> compositions) {
        for (ModifiedData<Composition> modifiedData : compositions) {
            Composition oldComposition = modifiedData.getOldData();
            Composition newComposition = modifiedData.getNewData();

            String path = oldComposition.getFilePath();
            String compositionPath = newComposition.getFilePath();
            if (path != null && !path.equals(compositionPath)) {
                RxNode<String> node = findNodeByPath(getParentDirPath(path), root);
                if (node != null) {
                    node.removeNode(getFileName(path));
                }
            }
            getParentForPath(root, compositionPath, (node, partialPath) ->
                    node.updateNode(partialPath, new CompositionNode(newComposition))
            );
        }
    }

    private void processDeleteChange(List<Composition> compositions) {
        fromIterable(compositions)
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
        }
    }

    private void processAddChange(List<Composition> compositions) {
        fromIterable(compositions)
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
