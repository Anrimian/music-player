package com.github.anrimian.musicplayer.data.repositories.scanner

import android.os.Build
import android.os.Environment
import androidx.collection.LongSparseArray
import com.github.anrimian.musicplayer.data.database.dao.compositions.CompositionsDaoWrapper
import com.github.anrimian.musicplayer.data.database.dao.compositions.StorageCompositionsInserter
import com.github.anrimian.musicplayer.data.database.dao.ignoredfolders.IgnoredFoldersDao
import com.github.anrimian.musicplayer.data.models.changes.Change
import com.github.anrimian.musicplayer.data.repositories.scanner.folders.FolderNode
import com.github.anrimian.musicplayer.data.repositories.scanner.folders.FolderTreeBuilder
import com.github.anrimian.musicplayer.data.storage.providers.music.StorageComposition
import com.github.anrimian.musicplayer.data.storage.providers.music.StorageFullComposition
import com.github.anrimian.musicplayer.data.utils.collections.AndroidCollectionUtils
import com.github.anrimian.musicplayer.domain.repositories.StateRepository
import com.github.anrimian.musicplayer.domain.utils.TextUtils
import com.github.anrimian.musicplayer.domain.utils.validation.DateUtils
import java.io.File
import java.util.LinkedList

class StorageCompositionAnalyzer(
    private val compositionsDao: CompositionsDaoWrapper,
    private val ignoredFoldersDao: IgnoredFoldersDao,
    private val stateRepository: StateRepository,
    private val compositionsInserter: StorageCompositionsInserter,
    private val maxCutDirPath: String = Environment.getExternalStorageDirectory().absolutePath,
) {

    private val actualTreeBuilder = FolderTreeBuilder(
        StorageFullComposition::getRelativePath,
        StorageFullComposition::getStorageId
    )

    @Synchronized
    fun applyCompositionsData(actualCompositions: LongSparseArray<StorageFullComposition>) {
        val currentCompositions = compositionsDao.selectAllAsStorageCompositions()

        actualTreeBuilder.createFileTree(actualCompositions)
            .cutCommonRoots(actualCompositions)
            .excludeCompositions(actualCompositions, ignoredFoldersDao.getIgnoredFolders())

        val addedCompositions = ArrayList<StorageFullComposition>()
        val deletedCompositions = ArrayList<StorageComposition>()
        val changedCompositions = ArrayList<Change<StorageComposition, StorageFullComposition>>()
        val hasChanges = AndroidCollectionUtils.processDiffChanges(
            currentCompositions,
            actualCompositions,
            this::hasActualChanges,
            deletedCompositions::add,
            addedCompositions::add
        ) { oldItem, newItem -> changedCompositions.add(Change(oldItem, newItem)) }
        if (hasChanges) {
            compositionsInserter.applyChanges(
                addedCompositions,
                deletedCompositions,
                changedCompositions
            )
        }
    }

    private fun hasActualChanges(
        first: StorageComposition,
        second: StorageFullComposition,
    ): Boolean {
        val isPathEqual = first.parentPath == second.relativePath
        val isFileNameEqual = first.fileName == second.fileName
        if (isPathEqual
            && isFileNameEqual
            && (!DateUtils.isAfter(second.dateModified, first.dateModified)
                    || !DateUtils.isAfter(second.dateModified, first.lastScanDate))
        ) {
            return false
        }
        var newAlbumName: String? = null
        var newAlbumArtist: String? = null
        val newAlbum = second.storageAlbum
        if (newAlbum != null) {
            newAlbumName = newAlbum.album
            newAlbumArtist = newAlbum.artist
        }
        return first.duration != second.duration
                || !isPathEqual
                || first.size != second.size
                || first.title != second.title
                || !isFileNameEqual
                || first.artist != second.artist
                || first.album != newAlbumName
                || first.albumArtist != newAlbumArtist
    }

    private fun FolderNode<Long>.excludeCompositions(
        compositions: LongSparseArray<StorageFullComposition>,
        excludedFolderPaths: Array<String>,
    ) {
        for (ignoredFolderPath in excludedFolderPaths) {
            val ignoreNode = findFolder(ignoredFolderPath) ?: continue
            ignoreNode.parentFolder?.removeFolder(ignoreNode.keyPath)
            for (id in getAllCompositionsInNode(ignoreNode)) {
                compositions.remove(id)
            }
        }
    }

    private fun FolderNode<Long>.findFolder(path: String): FolderNode<Long>? {
        var currentNode: FolderNode<Long> = this
        for (partialPath in path.split("/")) {
            currentNode = currentNode.getFolder(partialPath)
                ?: return null//perhaps we can implement find(). Find up and down on tree.
        }
        return currentNode
    }

    private fun getAllCompositionsInNode(parentNode: FolderNode<Long>): List<Long> {
        val result = LinkedList(parentNode.files)
        for (node in parentNode.folders) {
            result.addAll(getAllCompositionsInNode(node))
        }
        return result
    }

    private fun FolderNode<Long>.cutCommonRoots(
        actualCompositions: LongSparseArray<StorageFullComposition>,
    ): FolderNode<Long> {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return this
        }

        val sbRootPath = StringBuilder()
        val cutTree = cutUnnecessaryRootNodes(this, sbRootPath)

        val startIndex = sbRootPath.length
        if (startIndex > 0) {
            AndroidCollectionUtils.forEach(actualCompositions) { item ->
                val relativePath = item.relativePath
                item.relativePath = if (startIndex == relativePath.length) {
                    ""
                } else {
                    relativePath.substring(startIndex + 1).removePrefix("/")
                }
            }
        }

        stateRepository.rootFolderPath = TextUtils.toNullableString(sbRootPath)
        return cutTree
    }

    private fun cutUnnecessaryRootNodes(
        root: FolderNode<Long>,
        sbRootPath: StringBuilder,
    ): FolderNode<Long> {
        var found = root
        //we don't exclude more that external storage directory path
        val dirPath = maxCutDirPath.split(File.separator)
        var excludedFoldersCount = 0
        while (isEmptyFolderNode(found)) {
            if (excludedFoldersCount > 0 && excludedFoldersCount >= dirPath.size) {
                return found
            }
            found = found.firstFolder
            if (sbRootPath.isNotEmpty()) {
                sbRootPath.append('/')
            }
            val folderName = found.keyPath
            sbRootPath.append(folderName)
            if (dirPath[excludedFoldersCount] == folderName) {
                excludedFoldersCount++
            }
        }
        return found
    }

    private fun isEmptyFolderNode(node: FolderNode<Long>): Boolean {
        return node.folders.size == 1 && node.files.isEmpty()
    }

}