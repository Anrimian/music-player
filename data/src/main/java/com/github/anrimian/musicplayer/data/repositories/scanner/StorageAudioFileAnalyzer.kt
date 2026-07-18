package com.github.anrimian.musicplayer.data.repositories.scanner

import androidx.collection.LongSparseArray
import com.github.anrimian.musicplayer.data.database.dao.compositions.CompositionsDaoWrapper
import com.github.anrimian.musicplayer.data.database.dao.compositions.StorageCompositionsInserter
import com.github.anrimian.musicplayer.data.database.dao.ignoredfolders.IgnoredFoldersDao
import com.github.anrimian.musicplayer.data.storage.providers.music.AudioFileKey
import com.github.anrimian.musicplayer.data.storage.providers.music.DBComposition
import com.github.anrimian.musicplayer.data.storage.providers.music.StorageAudioFile
import com.github.anrimian.musicplayer.domain.models.common.TimedChange
import com.github.anrimian.musicplayer.domain.models.composition.InitialSource
import com.github.anrimian.musicplayer.domain.models.composition.LocalFileStatus
import com.github.anrimian.musicplayer.domain.models.composition.change.ChangedCompositionPath
import com.github.anrimian.musicplayer.domain.models.scanner.StorageAnalyzeResult
import com.github.anrimian.musicplayer.domain.models.sync.FileKey
import com.github.anrimian.musicplayer.domain.repositories.SettingsRepository

class StorageAudioFileAnalyzer(
    private val compositionsDao: CompositionsDaoWrapper,
    private val ignoredFoldersDao: IgnoredFoldersDao,
    private val settingsRepository: SettingsRepository,
    private val compositionsInserter: StorageCompositionsInserter,
    private val fileFilter: FileFilter,
) {

    @Synchronized
    fun applyCompositionsData(
        actualCompositions: HashMap<AudioFileKey, StorageAudioFile>
    ): StorageAnalyzeResult {
        val currentCompositions = compositionsDao.selectAllAsStorageCompositions()
        val ignoredFolders = ignoredFoldersDao.getIgnoredFolders()

        val excludedCompositions = excludeCompositions(actualCompositions, ignoredFolders)

        val addedCompositions = ArrayList<StorageAudioFile>()
        val restoredCompositions = ArrayList<Pair<DBComposition, StorageAudioFile>>()
        val missedCompositions = ArrayList<DBComposition>()
        val deletedCompositions = ArrayList<DBComposition>()
        val changedCompositions = ArrayList<TimedChange<DBComposition, StorageAudioFile>>()
        val currentTime = System.currentTimeMillis()
        if (currentCompositions.isEmpty() && actualCompositions.isNotEmpty()) {
            // fast insert case
            addedCompositions.addAll(actualCompositions.values)
            compositionsInserter.applyChanges(
                addedCompositions,
                restoredCompositions,
                missedCompositions,
                deletedCompositions,
                changedCompositions
            )
            return StorageAnalyzeResult(
                disappearedFiles = emptyList(),
                reappearedFiles = emptyList(),
                movedFiles = emptyList(),
                modifyTime = currentTime,
                hasChanges = true
            )
        }

        val hasChanges = processStorageChanges(
            oldMap = currentCompositions,
            newMap = actualCompositions,
            excludedKeys = excludedCompositions,
            currentTime = currentTime,
            isNowFiltered = this::isNowFiltered,
            isChanged = this::hasActualChanges,
            isMissedCompositionIsTooOld = { dbComposition ->
                val missingTime = dbComposition.missingTime
                missingTime > 0L
                        && System.currentTimeMillis() - missingTime > MISSED_RECORD_KEEP_TIME_MILLIS
            },
            isDbCompositionAvailable = { dbComposition ->
                dbComposition.localFileStatus == LocalFileStatus.AVAILABLE
            },
            isRemote = { dBComposition -> dBComposition.initialSource == InitialSource.REMOTE },
            onDeleteCallback = deletedCompositions::add,
            onMissedCallback = missedCompositions::add,
            onAddedCallback = addedCompositions::add,
            onRestoredCallback = restoredCompositions::add,
            onModifyCallback = changedCompositions::add
        )
        val movedFiles = ArrayList<ChangedCompositionPath>()
        if (hasChanges) {
            if (addedCompositions.isNotEmpty()) {
                // case for for external path change
                // do not insert&remove, just update folderId and fileName, if necessary
                val idMap = LongSparseArray<DBComposition>(currentCompositions.size)
                currentCompositions.forEach { (_, composition) ->
                    idMap.put(composition.storageId, composition)
                }
                val addedCompositionsToRemove = HashSet<StorageAudioFile>()
                for (addedComposition in addedCompositions) {
                    val dbComposition = idMap[addedComposition.storageId]
                    if (dbComposition != null) {
                        movedFiles.add(ChangedCompositionPath(
                            dbComposition.toFileKey(),
                            addedComposition.toFileKey(),
                            dbComposition.pathModifyTime
                        ))
                        addedCompositionsToRemove.add(addedComposition)
                        missedCompositions.remove(dbComposition)
                        changedCompositions.add(TimedChange(dbComposition, addedComposition, currentTime))
                    }
                }
                addedCompositions.removeAll(addedCompositionsToRemove)
            }
            compositionsInserter.applyChanges(
                addedCompositions,
                restoredCompositions,
                missedCompositions,
                deletedCompositions,
                changedCompositions
            )
        }
        return StorageAnalyzeResult(
            disappearedFiles = missedCompositions.map { c -> c.toFileKey() },
            reappearedFiles = restoredCompositions.map { c -> c.first.toFileKey() },
            movedFiles = movedFiles,
            modifyTime = currentTime,
            hasChanges = hasChanges
        )
    }

    private fun excludeCompositions(
        compositions: HashMap<AudioFileKey, StorageAudioFile>,
        ignoredFolders: Array<String>
    ): Set<AudioFileKey> {
        if (ignoredFolders.isEmpty()) {
            return emptySet()
        }
        val excludedKeys = HashSet<AudioFileKey>()
        val iterator = compositions.iterator()
        while (iterator.hasNext()) {
            val (key, value) = iterator.next()
            for (ignoredPath in ignoredFolders) {
                val parentPath = value.parentPath
                if (parentPath == ignoredPath || parentPath.startsWith("$ignoredPath/")) {
                    iterator.remove()
                    excludedKeys.add(key)
                    break
                }
            }
        }
        return excludedKeys
    }

    /**
     * We receive already filtered list from provider.
     * This check prevents filtered composition marked as disappeared.
     */
    private fun isNowFiltered(composition: DBComposition): Boolean {
        if (composition.localFileStatus == LocalFileStatus.LIBRARY_ENTRY_ONLY) {
            return false // ignore this type of records from filter
        }
        return composition.duration < settingsRepository.audioFileMinDurationMillis
                || !fileFilter.isFileExtensionAllowed(composition.fileName)
    }

    private fun hasActualChanges(
        dbComposition: DBComposition,
        storageAudioFile: StorageAudioFile,
    ): Boolean {
        return storageAudioFile.modifiedTime > dbComposition.storageModifyTime
                || storageAudioFile.modifiedTime > dbComposition.lastScanTime
                || dbComposition.duration != storageAudioFile.duration
                || dbComposition.storageId != storageAudioFile.storageId
    }

    private inline fun <K, V1, V2> processStorageChanges(
        oldMap: Map<K, V1>,
        newMap: Map<K, V2>,
        excludedKeys: Set<K>,
        currentTime: Long,
        crossinline isNowFiltered: (V1) -> Boolean,
        crossinline isChanged: (V1, V2) -> Boolean,
        crossinline isMissedCompositionIsTooOld: (V1) -> Boolean,
        crossinline isDbCompositionAvailable: (V1) -> Boolean,
        crossinline isRemote: (V1) -> Boolean,
        crossinline onDeleteCallback: (V1) -> Unit,
        crossinline onMissedCallback: (V1) -> Unit,
        crossinline onAddedCallback: (V2) -> Unit,
        crossinline onRestoredCallback: (Pair<V1, V2>) -> Unit,
        crossinline onModifyCallback: (TimedChange<V1, V2>) -> Unit,
    ): Boolean {
        var hasChanges = false

        for ((existKey, existValue) in oldMap) {
            val value = newMap[existKey]
            val isExcluded = excludedKeys.contains(existKey)
            if (value == null) {
                if (
                    (isExcluded && !isRemote(existValue))
                    || (!isDbCompositionAvailable(existValue) && isMissedCompositionIsTooOld(existValue))
                    || isNowFiltered(existValue)
                ) {
                    onDeleteCallback(existValue)
                    hasChanges = true
                } else if (isDbCompositionAvailable(existValue) && !isExcluded) {
                    onMissedCallback(existValue)
                    hasChanges = true
                }
            } else if (!isDbCompositionAvailable(existValue)) {
                onRestoredCallback(existValue to value)
                hasChanges = true
            }
        }

        for ((newKey, newValue) in newMap) {
            val existValue = oldMap[newKey]
            if (existValue == null) {
                onAddedCallback(newValue)
                hasChanges = true
            } else if (isChanged(existValue, newValue)) {
                onModifyCallback(TimedChange(existValue, newValue, currentTime))
                hasChanges = true
            }
        }
        return hasChanges
    }

    private fun DBComposition.toFileKey() = FileKey(fileName, parentPath)

    private fun StorageAudioFile.toFileKey() = FileKey(fileName, parentPath)

    private companion object Companion {
        const val MISSED_RECORD_KEEP_TIME_MILLIS = 90 * 24 * 60 * 60 * 1000L // 90 days
    }

}
