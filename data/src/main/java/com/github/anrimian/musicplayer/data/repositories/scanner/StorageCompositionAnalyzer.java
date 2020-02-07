package com.github.anrimian.musicplayer.data.repositories.scanner;

import androidx.collection.LongSparseArray;

import com.github.anrimian.musicplayer.data.database.dao.compositions.CompositionsDaoWrapper;
import com.github.anrimian.musicplayer.data.database.dao.folders.FoldersDaoWrapper;
import com.github.anrimian.musicplayer.data.database.entities.folder.FolderEntity;
import com.github.anrimian.musicplayer.data.models.changes.Change;
import com.github.anrimian.musicplayer.data.repositories.scanner.nodes.AddedNode;
import com.github.anrimian.musicplayer.data.repositories.scanner.nodes.FolderInfo;
import com.github.anrimian.musicplayer.data.repositories.scanner.nodes.FolderTreeNode;
import com.github.anrimian.musicplayer.data.repositories.scanner.nodes.Node;
import com.github.anrimian.musicplayer.data.storage.providers.albums.StorageAlbum;
import com.github.anrimian.musicplayer.data.storage.providers.music.StorageComposition;
import com.github.anrimian.musicplayer.data.storage.providers.music.StorageFullComposition;
import com.github.anrimian.musicplayer.data.utils.collections.AndroidCollectionUtils;
import com.github.anrimian.musicplayer.domain.utils.Objects;
import com.github.anrimian.musicplayer.domain.utils.java.Callback;
import com.github.anrimian.musicplayer.domain.utils.validation.DateUtils;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import io.reactivex.Observable;

import static com.github.anrimian.musicplayer.domain.utils.Objects.requireNonNull;

class StorageCompositionAnalyzer {

    private final CompositionsDaoWrapper compositionsDao;
    private final FoldersDaoWrapper foldersDao;

    private final FolderTreeNode.Builder<StorageFullComposition, Long> folderTreeBuilder;

    StorageCompositionAnalyzer(CompositionsDaoWrapper compositionsDao,
                               FoldersDaoWrapper foldersDao) {
        this.compositionsDao = compositionsDao;
        this.foldersDao = foldersDao;

        folderTreeBuilder = new FolderTreeNode.Builder<>(
                StorageFullComposition::getRelativePath,
                StorageFullComposition::getId
        );
    }

    //we can't merge data by storageId in future, merge by path+filename?
    synchronized void applyCompositionsData(LongSparseArray<StorageFullComposition> newCompositions) {
        Node<String, Long> actualFolderTree = folderTreeBuilder.createFileTree(fromSparseArray(newCompositions));//add folder name to compositions?
        actualFolderTree = cutEmptyRootNodes(actualFolderTree);//save excluded part?

        excludeCompositions(actualFolderTree, newCompositions);//also remove node from tree

        Node<String, FolderEntity> existsFolders = createTreeFromIdMap(foldersDao.getAllFolders());

        List<Long> foldersToDelete = new LinkedList<>();
        List<AddedNode> foldersToInsert = new LinkedList<>();//not sure if it right filled
        mergeFolderTrees(actualFolderTree, existsFolders, foldersToDelete, foldersToInsert);

        Set<Long> movedCompositions = getAffectedCompositions(foldersToInsert);
        LongSparseArray<StorageComposition> currentCompositions = compositionsDao.selectAllAsStorageCompositions();

        List<StorageFullComposition> addedCompositions = new ArrayList<>();
        List<StorageComposition> deletedCompositions = new ArrayList<>();
        List<Change<StorageComposition, StorageFullComposition>> changedCompositions = new ArrayList<>();
        boolean hasChanges = AndroidCollectionUtils.processDiffChanges(currentCompositions,
                newCompositions,
                (first, second) -> hasActualChanges(first, second) || movedCompositions.contains(first.getStorageId()),//is in folder change
                deletedCompositions::add,
                addedCompositions::add,
                (oldItem, newItem) -> changedCompositions.add(new Change<>(oldItem, newItem)));

        if (hasChanges) {
            foldersDao.insertFolders(foldersToInsert);
            //apply folder ids
            compositionsDao.applyChanges(addedCompositions, deletedCompositions, changedCompositions);
            //delete old folders
        }
    }

    private Set<Long> getAffectedCompositions(List<AddedNode> nodes) {
        Set<Long> result = new LinkedHashSet<>();
        for (AddedNode addedNode: nodes) {
            result.addAll(getAllCompositionsInNode(addedNode.getNode()));
        }
        return result;
    }

    private void mergeFolderTrees(Node<String, Long> actualFolderNode,
                                  Node<String, FolderEntity> existsFoldersNode,
                                  List<Long> foldersToDelete,
                                  List<AddedNode> foldersToInsert) {
        for (Node<String, FolderEntity> existFolder : existsFoldersNode.getNodes()) {
            String key = existFolder.getKey();

            Node<String, Long> actualFolder = actualFolderNode.getChild(key);
            if (actualFolder == null) {
                foldersToDelete.add(existFolder.getData().getId());
            }
        }
        for (Node<String, Long> actualFolder : actualFolderNode.getNodes()) {
            String key = actualFolder.getKey();

            Node<String, FolderEntity> existFolder = existsFoldersNode.getChild(key);
            if (existFolder == null) {
                Long parentId = null;
                FolderEntity entity = existsFoldersNode.getData();
                if (entity != null) {
                    parentId = entity.getId();
                }

                AddedNode addedNode = new AddedNode(parentId, actualFolder);
                foldersToInsert.add(addedNode);
            } else {
                mergeFolderTrees(actualFolder, existsFoldersNode, foldersToDelete, foldersToInsert);
            }
        }
    }

    private Node<String, FolderEntity> createTreeFromIdMap(List<FolderEntity> folders) {
        LongSparseArray<List<FolderEntity>> idMap = new LongSparseArray<>();

        FolderTreeNode<FolderEntity> rootNode = new FolderTreeNode<>(null, null);

        for (FolderEntity entity: folders) {
            Long parentId = entity.getParentId();
            if (parentId == null) {
                rootNode.addNode(new Node<>(entity.getName(), entity));
            } else {
                List<FolderEntity> childList = idMap.get(parentId);
                if (childList == null) {
                    childList = new LinkedList<>();
                    idMap.put(parentId, childList);
                }
                childList.add(entity);
            }
        }

        fillIdTree(rootNode, idMap);

        if (!idMap.isEmpty()) {
            throw new IllegalStateException("found missed folders");
        }

        return rootNode;
    }

    private void fillIdTree(Node<String, FolderEntity> targetNode,
                            LongSparseArray<List<FolderEntity>> idMap) {
        for (Node<String, FolderEntity> childNode: targetNode.getNodes()) {
            FolderEntity folderEntity = childNode.getData();
            long id = folderEntity.getId();
            List<FolderEntity> childList = idMap.get(id);
            if (childList == null) {
                break;
            }
            for (FolderEntity entity: childList) {
                Node<String, FolderEntity> node = new Node<>(entity.getName(), entity);
                childNode.addNode(node);
                fillIdTree(node, idMap);
            }
            idMap.remove(id);
        }
    }

    private void excludeCompositions(Node<String, Long> folderTree,
                                     LongSparseArray<StorageFullComposition> compositions) {
        String[] ignoresFolders = foldersDao.getIgnoredFolders();
        for (String ignoredFoldersPath: ignoresFolders) {
            Node<String, Long> ignoreNode = findNode(folderTree, ignoredFoldersPath);
            if (ignoreNode == null) {
                continue;
            }
            for (Long id: getAllCompositionsInNode(ignoreNode)) {
                compositions.remove(id);
            }
        }
    }

    private Node<String, Long> cutEmptyRootNodes(Node<String, Long> root) {
        Node<String, Long> found = root;
        while (isEmptyFolderNode(found)) {
            found = found.getFirstChild();
        }
        return found;
    }

    private boolean isEmptyFolderNode(Node<String, Long> node) {
        return node.getNodes().size() == 1 && node.getData() == null;
    }

    @Nullable
    private Node<String, Long> findNode(Node<String, Long> folderTree, String path) {
        Node<String, Long> currentNode = folderTree;
        for (String partialPath: path.split("/")) {
            currentNode = currentNode.getChild(partialPath);

            if (currentNode == null) {
                //perhaps we can implement find. Find up and down on tree.
                return null;
            }
        }
        return currentNode;
    }

    private List<FolderInfo> getAllFoldersInTree(Node<String, Long> parentNode) {
        LinkedList<FolderInfo> result = new LinkedList<>();
        result.add(new FolderInfo(parentNode.getKey(), null, getCompositionsInNode(parentNode)));
        for (Node<String, Long> node: parentNode.getNodes()) {
            if (node.getData() == null) {
                result.add(new FolderInfo(node.getKey(),
                        requireNonNull(node.getParent()).getKey(),
                        getCompositionsInNode(node))
                );
            } else {
                result.addAll(getAllFoldersInTree(node));
            }
        }
        return result;
    }

    private void forEachNode(Node<String, Long> parentNode, Callback<Node<String, Long>> action) {
        for (Node<String, Long> node: parentNode.getNodes()) {
            action.call(node);
            if (node.getData() == null) {
                forEachNode(node, action);
            }
        }
    }

    private List<Long> getCompositionsInNode(Node<String, Long> parentNode) {
        LinkedList<Long> result = new LinkedList<>();
        for (Node<String, Long> node: parentNode.getNodes()) {
            if (node.getData() != null) {
                result.add(node.getData());
            }
        }
        return result;
    }

    private Set<Long> getAllCompositionsInNode(Node<String, Long> parentNode) {
        Set<Long> result = new LinkedHashSet<>();
        for (Node<String, Long> node: parentNode.getNodes()) {
            if (node.getData() == null) {
                result.addAll(getAllCompositionsInNode(node));
            } else {
                result.add(node.getData());
            }
        }
        return result;
    }

    private Observable<StorageFullComposition> fromSparseArray(
            LongSparseArray<StorageFullComposition> sparseArray) {
        return Observable.create(emitter -> {
            for(int i = 0, size = sparseArray.size(); i < size; i++) {
                StorageFullComposition existValue = sparseArray.valueAt(i);
                emitter.onNext(existValue);
            }
            emitter.onComplete();
        });
    }

    private boolean hasActualChanges(StorageComposition first, StorageFullComposition second) {
        if (!DateUtils.isAfter(second.getDateModified(), first.getDateModified())) {
            return false;
        }

        String newAlbumName = null;
        String newAlbumArtist = null;
        StorageAlbum newAlbum = second.getStorageAlbum();
        if (newAlbum != null) {
            newAlbumName = newAlbum.getAlbum();
            newAlbumArtist = newAlbum.getArtist();
        }

        return !(Objects.equals(first.getDateAdded(), second.getDateAdded())
                && first.getDuration() == second.getDuration()
                && Objects.equals(first.getFilePath(), second.getFilePath())
                && first.getSize() == second.getSize()
                && Objects.equals(first.getTitle(), second.getTitle())
                && Objects.equals(first.getArtist(), second.getArtist())
                && Objects.equals(first.getAlbum(), newAlbumName)
                && Objects.equals(first.getAlbumArtist(), newAlbumArtist));
    }
}
