package com.github.anrimian.musicplayer.data.repositories.scanner;

import androidx.collection.LongSparseArray;

import com.github.anrimian.musicplayer.data.database.dao.compositions.CompositionsDaoWrapper;
import com.github.anrimian.musicplayer.data.database.dao.folders.FoldersDaoWrapper;
import com.github.anrimian.musicplayer.data.models.changes.Change;
import com.github.anrimian.musicplayer.data.repositories.scanner.nodes.FolderNode;
import com.github.anrimian.musicplayer.data.repositories.scanner.nodes.FolderTreeBuilder;
import com.github.anrimian.musicplayer.data.storage.providers.albums.StorageAlbum;
import com.github.anrimian.musicplayer.data.storage.providers.music.StorageComposition;
import com.github.anrimian.musicplayer.data.storage.providers.music.StorageFullComposition;
import com.github.anrimian.musicplayer.data.utils.collections.AndroidCollectionUtils;
import com.github.anrimian.musicplayer.domain.utils.Objects;
import com.github.anrimian.musicplayer.domain.utils.validation.DateUtils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.Nullable;

import io.reactivex.Observable;

class StorageCompositionAnalyzer {

    private final CompositionsDaoWrapper compositionsDao;
    private final FoldersDaoWrapper foldersDaoWrapper;

    private final FolderTreeBuilder<StorageFullComposition, Long> folderTreeBuilder;

    StorageCompositionAnalyzer(CompositionsDaoWrapper compositionsDao,
                               FoldersDaoWrapper foldersDaoWrapper) {
        this.compositionsDao = compositionsDao;
        this.foldersDaoWrapper = foldersDaoWrapper;

        folderTreeBuilder = new FolderTreeBuilder<>(
                StorageFullComposition::getRelativePath,
                StorageFullComposition::getId
        );
    }

    //we can't merge data by storageId in future, merge by path+filename?
    synchronized void applyCompositionsData(LongSparseArray<StorageFullComposition> newCompositions) {
        FolderNode<Long> folderTree = folderTreeBuilder.createFileTree(fromSparseArray(newCompositions));
        folderTree = cutEmptyRootNodes(folderTree);

        excludeCompositions(folderTree, newCompositions);

        LongSparseArray<StorageComposition> currentCompositions = compositionsDao.selectAllAsStorageCompositions();

        List<StorageFullComposition> addedCompositions = new ArrayList<>();
        List<StorageComposition> deletedCompositions = new ArrayList<>();
        List<Change<StorageComposition, StorageFullComposition>> changedCompositions = new ArrayList<>();

        boolean hasChanges = AndroidCollectionUtils.processDiffChanges(currentCompositions,
                newCompositions,
                this::hasActualChanges,
                deletedCompositions::add,
                addedCompositions::add,
                (oldItem, newItem) -> changedCompositions.add(new Change<>(oldItem, newItem)));

        if (hasChanges) {
            compositionsDao.applyChanges(addedCompositions, deletedCompositions, changedCompositions);
        }
    }

    private void excludeCompositions(FolderNode<Long> folderTree,
                                     LongSparseArray<StorageFullComposition> compositions) {
        String[] ignoresFolders = foldersDaoWrapper.getIgnoredFolders();
        for (String ignoredFoldersPath: ignoresFolders) {
            //find and remove
            FolderNode<Long> ignoreNode = findFolder(folderTree, ignoredFoldersPath);
            if (ignoreNode == null) {
                continue;
            }
            for (Long id: getAllCompositionsInNode(ignoreNode)) {
                compositions.remove(id);
            }
        }
    }

    private FolderNode<Long> cutEmptyRootNodes(FolderNode<Long> root) {
        FolderNode<Long> found = root;
        while (isEmptyFolderNode(found)) {
            found = found.getFirstFolder();
        }
        return found;
    }

    private boolean isEmptyFolderNode(FolderNode<Long> node) {
        return node.getFolders().size() == 1 && node.getFiles().isEmpty();
    }

    @Nullable
    private FolderNode<Long> findFolder(FolderNode<Long> folderTree, String path) {
        FolderNode<Long> currentNode = folderTree;
        for (String partialPath: path.split("/")) {
            currentNode = currentNode.getFolder(partialPath);

            if (currentNode == null) {
                //perhaps we can implement find. Find up and down on tree.
                return null;
            }
        }
        return currentNode;
    }

    private List<Long> getAllCompositionsInNode(FolderNode<Long> parentNode) {
        LinkedList<Long> result = new LinkedList<>(parentNode.getFiles());
        for (FolderNode<Long> node: parentNode.getFolders()) {
            result.addAll(getAllCompositionsInNode(node));
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
