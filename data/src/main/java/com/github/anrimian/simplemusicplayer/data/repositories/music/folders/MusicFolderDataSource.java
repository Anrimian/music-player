package com.github.anrimian.simplemusicplayer.data.repositories.music.folders;

import com.github.anrimian.simplemusicplayer.data.storage.StorageMusicDataSource;
import com.github.anrimian.simplemusicplayer.data.utils.FileUtils;
import com.github.anrimian.simplemusicplayer.data.utils.folders.NodeData;
import com.github.anrimian.simplemusicplayer.data.utils.folders.RxNode;
import com.github.anrimian.simplemusicplayer.domain.models.composition.Composition;
import com.github.anrimian.simplemusicplayer.domain.utils.changes.Change;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import io.reactivex.Single;

import static com.github.anrimian.simplemusicplayer.data.utils.FileUtils.getParentDirPath;
import static com.github.anrimian.simplemusicplayer.data.utils.Lists.mapList;
import static io.reactivex.Observable.fromIterable;

public class MusicFolderDataSource {

    private final StorageMusicDataSource storageMusicDataSource;

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
                node.getSelfChangeObservable());
    }

    private Change<List<NodeData>> toNodeDataChange(Change<List<RxNode<String>>> change) {
        List<NodeData> changedNodes = new ArrayList<>();
        for (RxNode<String> changedNode : change.getData()) {
            changedNodes.add(changedNode.getData());
        }
        return new Change<>(change.getChangeType(), changedNodes);
    }

    private List<NodeData> getFilesListFromNode(RxNode<String> node) {
        List<NodeData> fileList = new ArrayList<>();
        for (RxNode<String> child : node.getNodes()) {
            fileList.add(child.getData());
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
            getParentForPath(root, composition.getFilePath(), (node, partialPath) ->
                    node.updateNode(partialPath, new CompositionNode(composition))
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