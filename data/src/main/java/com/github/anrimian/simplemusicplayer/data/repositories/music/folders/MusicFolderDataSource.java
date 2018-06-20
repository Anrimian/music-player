package com.github.anrimian.simplemusicplayer.data.repositories.music.folders;

import com.github.anrimian.simplemusicplayer.data.storage.StorageMusicDataSource;
import com.github.anrimian.simplemusicplayer.data.utils.folders.NodeData;
import com.github.anrimian.simplemusicplayer.data.utils.folders.RxNode;
import com.github.anrimian.simplemusicplayer.domain.models.composition.Composition;
import com.github.anrimian.simplemusicplayer.domain.utils.changes.Change;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import io.reactivex.Single;

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
                break;
            }
            case MODIFY: {
                break;
            }
        }
    }

    private void processAddChange(List<Composition> compositions) {
        fromIterable(compositions)
                .groupBy(Composition::getFilePath)
                .doOnNext(group -> group.collect(ArrayList<Composition>::new, List::add)
                        .doOnSuccess(pathCompositions ->
                                putCompositions(group.getKey(), pathCompositions))
                        .subscribe())
                .subscribe();
    }

    private void putCompositions(String path, List<Composition> compositions) {
        getParentForPath(root, path, (node, partialPath) ->
                node.addNodes(mapList(compositions,
                        newComposition ->
                        new RxNode<>(partialPath, new CompositionNode(newComposition))
                ))
        );
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
