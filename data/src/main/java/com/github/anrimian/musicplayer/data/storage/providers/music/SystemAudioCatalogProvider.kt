package com.github.anrimian.musicplayer.data.storage.providers.music

import android.app.PendingIntent
import android.app.RecoverableSecurityException
import android.content.ContentProviderOperation
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.OperationApplicationException
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.RemoteException
import android.provider.MediaStore
import android.provider.MediaStore.Audio.Media
import android.webkit.MimeTypeMap
import androidx.annotation.RequiresApi
import com.github.anrimian.musicplayer.data.models.exceptions.FileConflictException
import com.github.anrimian.musicplayer.data.storage.exceptions.UpdateMediaStoreException
import com.github.anrimian.musicplayer.data.storage.providers.FileVolume
import com.github.anrimian.musicplayer.data.storage.providers.MediaStoreUtils
import com.github.anrimian.musicplayer.data.utils.db.CursorWrapper
import com.github.anrimian.musicplayer.data.utils.rx.content_observer.RxContentObserver
import com.github.anrimian.musicplayer.domain.Constants
import com.github.anrimian.musicplayer.domain.interactors.analytics.Analytics
import com.github.anrimian.musicplayer.domain.models.composition.FullComposition
import com.github.anrimian.musicplayer.domain.models.exceptions.NotAllowedPathException
import com.github.anrimian.musicplayer.domain.utils.FileUtils
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.BehaviorSubject
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.LinkedList
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class SystemAudioCatalogProvider(
    private val context: Context,
    private val analytics: Analytics,
) {

    private val contentResolver = context.contentResolver

    private val updateSubject = BehaviorSubject.createDefault<Any>(Constants.TRIGGER)

    private var isContentObserverEnabled = true
    private var hasDelayedUpdate = false

    fun scanMedia(uri: Uri?) {
        val scanFileIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri)
        context.sendBroadcast(scanFileIntent)
    }

    fun setContentObserverEnabled(enabled: Boolean) {
        isContentObserverEnabled = enabled
        if (enabled && hasDelayedUpdate) {
            hasDelayedUpdate = false
            updateSubject.onNext(Constants.TRIGGER)
        }
    }

    fun getChangeObservable(): Observable<Any> {
        var storageChangeObservable = RxContentObserver.getObservable(contentResolver, unsafeGetStorageUri())
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // on new composition content observer not called on android 10
            // but for some reason content observer is called for playlist items when new file added
            // so we create observer for non-existing playlist(!) and it works

            val playListChangeObservable = RxContentObserver.getObservable(
                contentResolver,
                MediaStore.Audio.Playlists.Members.getContentUri("external", 0)
            )
            // maybe filter often events?
            storageChangeObservable = Observable.merge(storageChangeObservable, playListChangeObservable)
        }
        return Observable.merge(storageChangeObservable, updateSubject)
            .filter {
                if (!isContentObserverEnabled) {
                    hasDelayedUpdate = true
                }
                isContentObserverEnabled
            }
    }

    fun getAudioFiles(
        minAudioDurationMillis: Long,
        showAllAudioFiles: Boolean,
        allowedExtensions: Set<String>
    ): HashMap<AudioFileKey, StorageAudioFile>? {
        val queryBuilder = mutableListOf(
            Media.ARTIST,
            Media.TITLE,
            Media.DISPLAY_NAME,
            Media.DURATION,
            Media.SIZE,
            Media._ID,
            Media.DATE_ADDED,
            Media.DATE_MODIFIED
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            queryBuilder.add(Media.RELATIVE_PATH)
            queryBuilder.add(Media.VOLUME_NAME)
        } else {
            queryBuilder.add(Media.DATA)
        }
        val query = queryBuilder.toTypedArray()

        // check how it works
        val uris = getStorageUris()
        if (uris.isEmpty()) {
            return null
        }

        val selectionBuilder = StringBuilder()
        val projection = LinkedList<String>()
        // also display unsupported or corrupted compositions
        selectionBuilder.append("(${Media.DURATION} >= ? OR ${Media.DURATION} IS NULL)")
        projection.add(minAudioDurationMillis.toString())

        if (!showAllAudioFiles) {
            selectionBuilder.append(" AND ")
            selectionBuilder.append(Media.IS_MUSIC)
            selectionBuilder.append(" = ?")
            projection.add(1.toString())
        }

        if (allowedExtensions.isNotEmpty()) {
            selectionBuilder.append(" AND ")
            selectionBuilder.append('(')
            for (i in allowedExtensions.indices) {
                selectionBuilder.append(Media.DISPLAY_NAME)
                selectionBuilder.append(" LIKE ?")
                if (i < allowedExtensions.size - 1) {
                    selectionBuilder.append(" OR ")
                }
            }
            selectionBuilder.append(')')
            for (extension in allowedExtensions) {
                projection.add("%.$extension")
            }
        }
        val selection = selectionBuilder.toString()

        val volumePaths = MediaStoreUtils.getVolumes(context)

        val compositions = HashMap<AudioFileKey, StorageAudioFile>()
        for (uri in uris) {
            query(uri, query, selection, projection.toTypedArray(), null)?.use { cursor ->
                val cursorWrapper = CursorWrapper(cursor)

                val artistIndex = cursor.getColumnIndex(Media.ARTIST)
                val titleIndex = cursor.getColumnIndex(Media.TITLE)
                val relativePathIndex = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    cursor.getColumnIndex(Media.RELATIVE_PATH)
                } else {
                    -1
                }
                val volumeNameIndex = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    cursor.getColumnIndex(Media.VOLUME_NAME)
                } else {
                    -1
                }
                val filePathIndex = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
                    cursor.getColumnIndex(Media.DATA)
                } else {
                    -1
                }
                val displayNameIndex = cursor.getColumnIndex(Media.DISPLAY_NAME)
                val durationIndex = cursor.getColumnIndex(Media.DURATION)
                val sizeIndex = cursor.getColumnIndex(Media.SIZE)
                val idIndex = cursor.getColumnIndex(Media._ID)
                val dateAddedIndex = cursor.getColumnIndex(Media.DATE_ADDED)
                val dateModifiedIndex = cursor.getColumnIndex(Media.DATE_MODIFIED)

                val volumeCompositions = HashMap<AudioFileKey, StorageAudioFile>(cursor.count)
                while (MediaStoreUtils.moveToNext(cursor)) {
                    val composition = buildStorageComposition(
                        artistIndex,
                        titleIndex,
                        relativePathIndex,
                        volumeNameIndex,
                        filePathIndex,
                        displayNameIndex,
                        durationIndex,
                        sizeIndex,
                        idIndex,
                        dateAddedIndex,
                        dateModifiedIndex,
                        cursorWrapper,
                        volumePaths
                    )
                    if (composition != null) {
                        volumeCompositions[composition.createKey()] = composition
                    }
                }
                compositions.putAll(volumeCompositions)
            }
        }
        return compositions
    }

    fun getCompositionFilePath(storageId: Long): String? {
        val uri = getCompositionUri(storageId)
        return getCompositionFilePath(uri)
    }

    fun getCompositionFilePath(uri: Uri): String? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            val query = arrayOf(Media.DATA)

            query(uri, query, null, null, null)?.use { cursor ->
                if (cursor.count == 0) {
                    return null
                }

                val cursorWrapper = CursorWrapper(cursor)
                if (cursor.moveToFirst()) {
                    return cursorWrapper.getString(Media.DATA)
                }
                return null
            }
            return null
        }

        val query = arrayOf(Media.VOLUME_NAME, Media.RELATIVE_PATH, Media.DISPLAY_NAME)
        query(uri, query, null, null, null)?.use { cursor ->
            if (cursor.count == 0) {
                return null
            }

            if (cursor.moveToFirst()) {
                val cursorWrapper = CursorWrapper(cursor)
                val volumeName = cursorWrapper.getString(Media.VOLUME_NAME)
                val relativePath = cursorWrapper.getString(Media.RELATIVE_PATH)
                    ?: return null
                val displayName = cursorWrapper.getString(Media.DISPLAY_NAME)
                    ?: return null

                val volumePaths = MediaStoreUtils.getVolumes(context)
                val dirPath = buildPath(
                    volumePaths[volumeName]?.path ?: "",
                    relativePath.trimEnd('/')
                )
                return dirPath + File.separator + displayName
            }
            return null
        }
        return null
    }

    fun findCompositionByPath(filePath: String): Long? {
        val query = arrayOf(Media._ID)

        query(
            getStorageUri(),
            query,
            "${Media.DATA} = ? ",
            arrayOf(filePath),
            null
        )?.use { cursor ->
            if (cursor.count == 0) {
                return null
            }
            val cursorWrapper = CursorWrapper(cursor)
            if (cursor.moveToFirst()) {
                return cursorWrapper.getLong(Media._ID)
            }
            return null
        }
        return null
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    fun findCompositionByNameAndRelativePath(name: String, relativePath: String): Long? {
        val query = arrayOf(Media._ID, Media.RELATIVE_PATH)

        query(
            getStorageUri(),
            query,
            "${Media.DISPLAY_NAME} = ? AND ${Media.RELATIVE_PATH} = ? ",
            arrayOf(name, relativePath),
            null
        )?.use { cursor ->
            if (cursor.count == 0) {
                return null
            }

            val cursorWrapper = CursorWrapper(cursor)
            if (cursor.moveToFirst()) {
                return cursorWrapper.getLong(Media._ID)
            }
            return null
        }
        return null
    }

    fun getCompositionFileName(storageId: Long): String? {
        val query = arrayOf(Media.DISPLAY_NAME)

        query(getCompositionUri(storageId), query, null, null, null)?.use { cursor ->
            if (cursor.count == 0) {
                return null
            }

            val cursorWrapper = CursorWrapper(cursor)
            if (cursor.moveToFirst()) {
                return cursorWrapper.getString(Media.DISPLAY_NAME)
            }
            return null
        }
        return null
    }

    fun deleteComposition(id: Long) {
        deleteCompositions(listOf(id))
    }

    fun deleteCompositions(ids: List<Long>) {
        val operations = ArrayList<ContentProviderOperation>()

        for (storageId in ids) {
            val operation = ContentProviderOperation
                .newDelete(getCompositionUri(storageId))
                .build()
            operations.add(operation)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                applyBatch(operations)
            } catch (e: RecoverableSecurityException) {
                val uris = ids.map { getCompositionUri(it) }
                val pIntent = createDeleteRequest(uris)
                throw RecoverableSecurityExceptionExt(pIntent, e.message)
            }
        } else {
            val paths = ids.mapNotNull(this::getCompositionFilePath)

            applyBatch(operations)

            scanFiles(paths.toTypedArray())
        }
    }

    fun updateCompositionArtist(id: Long, author: String?) {
        updateComposition(id, MediaStore.Audio.AudioColumns.ARTIST, author)
    }

    fun updateCompositionAlbum(id: Long, album: String?) {
        updateComposition(id, MediaStore.Audio.AudioColumns.ALBUM, album)
    }

    fun updateCompositionTitle(id: Long, title: String?) {
        updateComposition(id, MediaStore.Audio.AudioColumns.TITLE, title)
    }

    fun updateCompositionFileName(id: Long, name: String?) {
        updateComposition(id, MediaStore.Audio.AudioColumns.DISPLAY_NAME, name)
    }

    fun updateCompositionFilePath(id: Long, filePath: String?) {
        updateComposition(id, MediaStore.Audio.AudioColumns.DATA, filePath)
    }

    fun updateCompositionsFilePath(compositions: List<FilePathComposition>) {
        val operations = ArrayList<ContentProviderOperation>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val pathsToScan = ArrayList<String>()
            for (composition in compositions) {
                val storageId = composition.storageId ?: continue
                val newFullPath = composition.filePath
                pathsToScan.add(newFullPath)

                val parentPath = FileUtils.getParentDirPath(newFullPath)
                val relativePath = extractRelativePath(parentPath)
                val builder = ContentProviderOperation.newUpdate(getCompositionUri(storageId))
                    .withValue(Media.RELATIVE_PATH, relativePath)

                val newFileName = FileUtils.getFileName(newFullPath)
                if (newFileName != getCompositionFileName(storageId)) {
                    builder.withValue(Media.DISPLAY_NAME, newFileName)
                }
                operations.add(builder.build())
            }

            try {
                applyBatch(operations)
                // Android 14–16: applyBatch moves the file but row queries may stay stale until a scan reconciles them.
                scanFiles(pathsToScan.toTypedArray())
            } catch (e: RecoverableSecurityException) {
                val uris = compositions.mapNotNull { comp ->
                    comp.storageId?.let { id -> getCompositionUri(id) }
                }
                val pIntent = MediaStore.createWriteRequest(contentResolver, uris)
                throw RecoverableSecurityExceptionExt(pIntent, e.message)
            } catch (e: Exception) {
                processEditException(e)
                throw e
            }
        } else {
            val pathsToScan = ArrayList<String>()
            for (composition in compositions) {
                val storageId = composition.storageId ?: continue
                val uri = getCompositionUri(storageId)
                val oldPath = getCompositionFilePath(uri)
                if (oldPath != null) {
                    pathsToScan.add(oldPath)
                }
                val newPath = composition.filePath
                pathsToScan.add(newPath)
                val builder = ContentProviderOperation.newUpdate(uri)
                    .withValue(Media.DATA, newPath)

                val newFileName = FileUtils.getFileName(newPath)
                if (newFileName != getCompositionFileName(storageId)) {
                    builder.withValue(Media.DISPLAY_NAME, newFileName)
                }
                operations.add(builder.build())
            }
            applyBatch(operations)

            scanFiles(pathsToScan.toTypedArray())
        }
    }

    fun getCompositionUri(id: Long): Uri {
        return ContentUris.withAppendedId(getStorageUri(), id)
    }

    fun getCompositionStream(id: Long): InputStream {
        return contentResolver.openInputStream(getCompositionUri(id)) ?: throw FileNotFoundException()
    }

    fun openCompositionOutputStream(id: Long?): OutputStream? {
        if (id == null) {
            throw FileNotFoundException("can not open stream for file without media store id")
        }
        return openCompositionOutputStream(getCompositionUri(id))
    }

    fun openCompositionOutputStream(uri: Uri): OutputStream? {
        return contentResolver.openOutputStream(uri)
    }

    fun insertComposition(
        name: String,
        parentPath: String,
        composition: FullComposition,
        streamCallback: (OutputStream) -> Unit
    ): Uri {
        val cv = ContentValues().apply {
            put(Media.DISPLAY_NAME, name)
            put(Media.TITLE, composition.title)
            val ext = FileUtils.getExtension(name)
            var mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext)
            if (mimeType == null) {
                // since some update on android 15 media storage doesn't accept wildcards (audio/*)
                mimeType = "audio/mpeg"
            }
            put(Media.MIME_TYPE, mimeType)
            put(Media.ARTIST, composition.artist)
            put(Media.ALBUM, composition.album)
            put(Media.DURATION, composition.duration)
            put(Media.SIZE, composition.size)
            put(Media.DATE_ADDED, composition.addedTime / 1000)
            put(Media.DATE_MODIFIED, composition.modifiedTime / 1000)
        }
        putAudioFileType(cv, parentPath)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return insertCompositionApi30(name, parentPath, cv, streamCallback)
        }

        val parentFolder = File(parentPath)
        parentFolder.mkdirs()
        val finalFile = File(parentFolder, name)
        val absolutePath = finalFile.absolutePath

        val existingId = findCompositionByPath(absolutePath)
        if (existingId != null) {
            return updateComposition(existingId, cv, streamCallback)
        }

        // Write to a temporary file to avoid MediaScanner seeing an incomplete download.
        val tmpFile = File(parentFolder, "$name.tmp")
        try {
            FileOutputStream(tmpFile, false).use { outputStream ->
                streamCallback(outputStream)
            }
            val conflictingId = findCompositionByPath(absolutePath)
            if (conflictingId != null) {
                // RACE CONDITION DETECTED: Another app created the file while we were writing.
                // Delete our file and return other.
                return getCompositionUri(conflictingId)
            }

            if (!tmpFile.renameTo(finalFile)) {
                throw IOException("Failed to rename temporary file to final destination.")
            }
            cv.put(Media.DATA, absolutePath)
            val uri = contentResolver.insert(getStorageUriForInsertion(), cv)
                ?: throw IOException("Failed to insert into media store.")
            scanFiles(arrayOf(absolutePath))

            val id = ContentUris.parseId(uri)
            val actualName = getCompositionFileName(id)
            if (actualName != null && actualName != name) {
                throw FileConflictException(actualName, uri)
            }

            return uri
        } finally {
            if (tmpFile.exists()) {
                tmpFile.delete()
            }
        }
    }

    fun processStorageException(throwable: Throwable, uris: List<Uri>): Completable {
        return Completable.fromAction {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && throwable is RecoverableSecurityException) {
                val pIntent = createWriteRequest(uris)
                throw RecoverableSecurityExceptionExt(pIntent, throwable.message)
            }
            throw throwable
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    fun createWriteRequest(uris: List<Uri>): PendingIntent {
        return MediaStore.createWriteRequest(contentResolver, uris)
    }

    @RequiresApi(Build.VERSION_CODES.R)
    fun createDeleteRequest(uris: List<Uri>): PendingIntent {
        return MediaStore.createDeleteRequest(contentResolver, uris)
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    fun getUriByNameAndPath(name: String, parentPath: String): Uri? {
        val relativePath = extractRelativePath(parentPath)
        val id = findCompositionByNameAndRelativePath(name, relativePath) ?: return null
        return getCompositionUri(id)
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    private fun insertCompositionApi30(
        name: String,
        parentPath: String,
        contentValues: ContentValues,
        streamCallback: (OutputStream) -> Unit
    ): Uri {
        var uri: Uri? = null

        val relativePath = extractRelativePath(parentPath)

        val existingId = findCompositionByNameAndRelativePath(name, relativePath)
        if (existingId != null) {
            return updateComposition(existingId, contentValues, streamCallback)
        }

        var completed = false
        try {
            contentValues.put(Media.RELATIVE_PATH, relativePath)
            contentValues.put(Media.IS_PENDING, 1)
            uri = contentResolver.insert(getStorageUriForInsertion(), contentValues)
                ?: throw IOException("Failed to insert into media store.")

            contentResolver.openOutputStream(uri)?.use { outputStream ->
                streamCallback(outputStream)
            }
            val conflictingId = findCompositionByNameAndRelativePath(name, relativePath)
            if (conflictingId != null) {
                // RACE CONDITION DETECTED: Another app created the file while we were writing.
                // Delete our file and return other.
                return getCompositionUri(conflictingId)
            }

            contentValues.clear()
            contentValues.put(Media.IS_PENDING, 0)
            contentResolver.update(uri, contentValues, null, null)
            completed = true
        } catch (e: Exception) {
            processEditException(e)
            throw e
        } finally {
            if (!completed && uri != null) {
                contentResolver.delete(uri, null, null)
            }
        }
        val id = ContentUris.parseId(uri)
        val actualName = getCompositionFileName(id)
        if (actualName != null && actualName != name) {
            throw FileConflictException(actualName, uri)
        }
        return uri
    }

    private fun updateComposition(
        id: Long,
        contentValues: ContentValues,
        streamCallback: (OutputStream) -> Unit
    ): Uri {
        val uri = getCompositionUri(id)
        try {
            contentResolver.openOutputStream(uri)?.use { outputStream ->
                streamCallback(outputStream)
            }
        } catch (e: Exception) {
            processEditException(e)
            throw e
        }
        contentResolver.update(uri, contentValues, null, null)
        return uri
    }

    private fun updateComposition(id: Long, key: String, value: String?) {
        val cv = ContentValues()
        cv.put(key, value)
        contentResolver.update(getCompositionUri(id), cv, null, null)
    }

    private fun buildStorageComposition(
        artistIndex: Int,
        titleIndex: Int,
        relativePathIndex: Int,
        volumeNameIndex: Int,
        filePathIndex: Int,
        displayNameIndex: Int,
        durationIndex: Int,
        sizeIndex: Int,
        idIndex: Int,
        dateAddedIndex: Int,
        dateModifiedIndex: Int,
        cursorWrapper: CursorWrapper,
        volumePaths: Map<String, FileVolume>
    ): StorageAudioFile? {
        val displayName = cursorWrapper.getString(displayNameIndex)
        if (displayName.isNullOrEmpty()) {
            // can be without name, but we can't use it
            return null
        }
        val size = cursorWrapper.getLong(sizeIndex)
        if (size == 0L) {
            return null
        }

        val filePath: String
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val volumeName = cursorWrapper.getString(volumeNameIndex)
            var relativePath = cursorWrapper.getString(relativePathIndex) ?: ""
            if (relativePath.isNotEmpty()) {
                val lastCharIndex = relativePath.length - 1
                val lastChar = relativePath[lastCharIndex]
                if (lastChar == '/') {
                    relativePath = relativePath.substring(0, lastCharIndex)
                }
            }
            val volumePath = volumePaths[volumeName]?.path
            if (volumePath == null) {
                analytics.logMessage(
                    "Unknown volume name '$volumeName' for relative path '$relativePath', known volume names: ${volumePaths.keys}"
                )
                return null
            }
            filePath = buildPath(volumePath, relativePath)
        } else {
            val fullPath = cursorWrapper.getString(filePathIndex)
            if (fullPath.isNullOrEmpty()) {
                return null
            }
            // Resolve symlink-style path aliases (e.g. Huawei EMUI returns `/sdcard/...`
            // from Media.DATA while the framework's canonical form is `/storage/emulated/0/...`).
            // Canonicalization keeps the path in /storage/<vol>/... shape so the DAO layer
            // can derive the volume from it via FileVolume.fromCanonicalPath.
            val parentDir = FileUtils.getParentDirPath(fullPath)
            val canonicalParent = try {
                File(parentDir).canonicalPath
            } catch (_: IOException) {
                parentDir
            }
            filePath = canonicalParent.trimStart('/')
        }

        var artist = cursorWrapper.getString(artistIndex)
        val title = cursorWrapper.getString(titleIndex)
        // val album = cursorWrapper.getString(Media.ALBUM)

        // val albumKey = cursorWrapper.getString(MediaStore.Audio.Media.ALBUM_KEY)
        // val composer = cursorWrapper.getString(MediaStore.Audio.Media.COMPOSER)

        // val mimeType = cursorWrapper.getString(Media.MIME_TYPE)

        val duration = cursorWrapper.getLong(durationIndex)
        val id = cursorWrapper.getLong(idIndex)
        // val artistId = cursorWrapper.getLong(Media.ARTIST_ID)
        // val bookmark = cursorWrapper.getLong(Media.BOOKMARK)
        // val albumId = cursorWrapper.getLong(albumIdIndex)
        val dateAddedMillis = cursorWrapper.getLong(dateAddedIndex)
        val dateModifiedMillis = cursorWrapper.getLong(dateModifiedIndex)

        // val year: Int? = cursorWrapper.getInt(YEAR)

        val addedTime = if (dateAddedMillis == 0L) {
            System.currentTimeMillis()
        } else {
            dateAddedMillis * 1000L
        }
        val modifiedTime = if (dateModifiedMillis == 0L) {
            System.currentTimeMillis()
        } else {
            dateModifiedMillis * 1000L
        }

        if (artist == "<unknown>") {
            artist = null
        }

        val parentPath = FileVolume.canonicalize(filePath)
        if (FileVolume.fromCanonicalPathOrNull(parentPath) == null) {
            analytics.logMessage("Unparseable storage volume: $filePath, canonicalized: $parentPath")
            return null
        }

        return StorageAudioFile(
            artist,
            title,
            displayName,
            parentPath,
            duration,
            size,
            id,
            addedTime,
            modifiedTime,
        )
    }

    private fun putAudioFileType(cv: ContentValues, parentPath: String) {
        when {
            parentPath.isEmpty() || parentPath.contains(Environment.DIRECTORY_MUSIC, ignoreCase = true) -> {
                cv.put(Media.IS_MUSIC, true)
            }
            parentPath.contains(Environment.DIRECTORY_PODCASTS, ignoreCase = true) -> {
                cv.put(Media.IS_PODCAST, true)
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
                    parentPath.contains(Environment.DIRECTORY_AUDIOBOOKS, ignoreCase = true) -> {
                cv.put(Media.IS_AUDIOBOOK, true)
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                    parentPath.contains(Environment.DIRECTORY_RECORDINGS, ignoreCase = true) -> {
                cv.put(Media.IS_RECORDING, true)
            }
            parentPath.contains(Environment.DIRECTORY_NOTIFICATIONS, ignoreCase = true) -> {
                cv.put(Media.IS_NOTIFICATION, true)
            }
            parentPath.contains(Environment.DIRECTORY_ALARMS, ignoreCase = true) -> {
                cv.put(Media.IS_ALARM, true)
            }
            parentPath.contains(Environment.DIRECTORY_RINGTONES, ignoreCase = true) -> {
                cv.put(Media.IS_RINGTONE, true)
            }
            else -> {
                // default
                cv.put(Media.IS_MUSIC, true)
            }
        }
    }

    private fun unsafeGetStorageUri(): Uri {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            Media.EXTERNAL_CONTENT_URI
        }
    }

    private fun getStorageUri(): Uri {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStoreUtils.checkIfMediaStoreAvailable(context)
            Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            Media.EXTERNAL_CONTENT_URI
        }
    }

    private fun getStorageUriForInsertion(): Uri {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        } else {
            Media.EXTERNAL_CONTENT_URI
        }
    }

    private fun getStorageUris(): List<Uri> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val volumes = MediaStore.getExternalVolumeNames(context)
            volumes.map { volume -> Media.getContentUri(volume) }
        } else {
            listOf(Media.EXTERNAL_CONTENT_URI)
        }
    }

    private fun extractRelativePath(parentPath: String): String {
        val volume = FileVolume.fromCanonicalPathOrNull(parentPath)
            ?: throw NotAllowedPathException(MediaStoreUtils.getAllowedAudioFolders())
        val volumePath = volume.path
        if (parentPath == volumePath) {
            throw NotAllowedPathException(MediaStoreUtils.getAllowedAudioFolders())
        }
        val parentDirWithSlash = if (parentPath.endsWith('/')) parentPath else "$parentPath/"
        return parentDirWithSlash.substring(volumePath.length).trimStart('/')
    }

    private fun applyBatch(operations: ArrayList<ContentProviderOperation>) {
        try {
            contentResolver.applyBatch(MediaStore.AUTHORITY, operations)
        } catch (e: Exception) {
            when (e) {
                is OperationApplicationException,
                is RemoteException -> {
                    throw UpdateMediaStoreException(e)
                }
                else -> throw e
            }
        }
    }

    private fun query(
        uri: Uri,
        projection: Array<String>?,
        selection: String?,
        selectionArgs: Array<String>?,
        sortOrder: String?
    ) = MediaStoreUtils.query(contentResolver, uri, projection, selection, selectionArgs, sortOrder)

    private fun processEditException(e: Exception) {
        val message = e.message
        if (e is IllegalArgumentException && message != null && message.contains("not allowed for content")) {
            val indexOfFirstQuotes = message.indexOf('[') + 1
            val allowedFolders = message.substring(
                indexOfFirstQuotes,
                message.indexOf(']', indexOfFirstQuotes)
            )
            throw NotAllowedPathException(allowedFolders)
        }
    }

    private fun buildPath(volumePath: String, relativePath: String): String {
        return if (relativePath.isEmpty()) {
            volumePath
        } else {
            volumePath + File.separator + relativePath
        }
    }

    private fun scanFiles(paths: Array<String>) {
        if (paths.isEmpty()) {
            return
        }

        val latch = CountDownLatch(paths.size)
        MediaScannerConnection.scanFile(
            context,
            paths,
            null
        ) { _, _ ->
            latch.countDown()
        }

        try {
            latch.await(25, TimeUnit.SECONDS)
        } catch (_: InterruptedException) {
            Thread.currentThread().interrupt()
        }
    }

}
