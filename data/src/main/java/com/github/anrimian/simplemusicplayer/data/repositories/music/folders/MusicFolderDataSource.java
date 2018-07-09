package com.github.anrimian.simplemusicplayer.data.repositories.music.folders;

import com.github.anrimian.simplemusicplayer.data.storage.StorageMusicDataSource;
import com.github.anrimian.simplemusicplayer.data.utils.FileUtils;
import com.github.anrimian.simplemusicplayer.data.utils.folders.NodeData;
import com.github.anrimian.simplemusicplayer.data.utils.folders.RxNode;
import com.github.anrimian.simplemusicplayer.domain.models.composition.Composition;
import com.github.anrimian.simplemusicplayer.domain.models.composition.folders.Folder;
import com.github.anrimian.simplemusicplayer.domain.models.composition.folders.FileSource;
import com.github.anrimian.simplemusicplayer.domain.models.composition.folders.FolderFileSource;
import com.github.anrimian.simplemusicplayer.domain.models.composition.folders.MusicFileSource;
import com.github.anrimian.simplemusicplayer.domain.utils.changes.Change;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import io.reactivex.Single;

import static com.github.anrimian.simplemusicplayer.data.utils.FileUtils.getFileName;
import static com.github.anrimian.simplemusicplayer.data.utils.FileUtils.getParentDirPath;
import static com.github.anrimian.simplemusicplayer.data.utils.Lists.mapList;
import static io.reactivex.Observable.fromIterable;

public class MusicFolderDataSource {

    private final StorageMusicDataSource storageMusicDataSource;

    private Map<Long, String> pathIdMap = new HashMap<>();

    @Nullable
    private RxNode<String> root;

    public MusicFolderDataSource(StorageMusicDataSource storageMusicDataSource) {
        this.storageMusicDataSource = storageMusicDataSource;
    }

    public Single<Folder> getMusicInPath(@Nullable String path) {
        return getMusicFileTree()
                .map(tree -> getFolderInPath(path));
    }

    private Folder getFolderInPath(@Nullable String path) {
        RxNode<String> node = findNodeByPath(path);
        return new Folder(getFilesListFromNode(node),
                node.getChildChangeObservable().map(this::toNodeDataChange),
                node.getSelfChangeObservable().map(this::toChangeFileSource));
    }

    private Change<List<FileSource>> toNodeDataChange(Change<List<RxNode<String>>> change) {
        List<FileSource> changedNodes = new ArrayList<>();
        for (RxNode<String> changedNode : change.getData()) {
            changedNodes.add(toFileSource(changedNode.getData()));
        }
        return new Change<>(change.getChangeType(), changedNodes);
    }

    private Change<FileSource> toChangeFileSource(Change<NodeData> change) {
        return new Change<>(change.getChangeType(), toFileSource(change.getData()));
    }

    private FileSource toFileSource(NodeData nodeData) {
        if (nodeData instanceof CompositionNode) {
            return new MusicFileSource(((CompositionNode) nodeData).getComposition());
        } else if (nodeData instanceof FolderNode){
            FolderNode node = (FolderNode) nodeData;
            return new FolderFileSource(node.getFullPath(), node.getCompositionsCount());
        }
        throw new IllegalStateException("unexpected type of node: " + nodeData);
    }

    private List<FileSource> getFilesListFromNode(RxNode<String> node) {
        List<FileSource> fileList = new ArrayList<>();
        for (RxNode<String> child : node.getNodes()) {
            fileList.add(toFileSource(child.getData()));
        }
        return fileList;
    }

    @Nullable
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
                subscribeOnCompositionsChanging();
            }
            return root;
        });
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
                findNodeByPath(getParentDirPath(path))
                        .removeNode(getFileName(composition.getFilePath()));
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
        RxNode<String> node = findNodeByPath(path);
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
        getNode(root, path, (node, partialPath) ->
                node.addNodes(mapList(compositions,
                        newComposition ->
                                new RxNode<>(partialPath, new CompositionNode(newComposition))
                ))
        );
    }

    private RxNode<String> createMusicFileTree() {
        RxNode<String> root = new RxNode<>(null, null);
//        fromIterable(storageMusicDataSource.getCompositionsMap().values())//TODO optimize tree creating
//                .groupBy(composition -> getParentDirPath(composition.getFilePath()))
//                .doOnNext(group -> group.collect(ArrayList<Composition>::new, List::add)
//                        .doOnSuccess(pathCompositions ->
//                                putCompositions(root, group.getKey(), pathCompositions))
//                        .subscribe())
//                .subscribe();
        for (Composition composition: storageMusicDataSource.getCompositionsMap().values()) {
            String path = composition.getFilePath();
            pathIdMap.put(composition.getId(), path);

            getParentForPath(root, path, (node, partialPath) ->
                    node.addNode(new RxNode<>(partialPath, new CompositionNode(composition)))
            );
        }
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
