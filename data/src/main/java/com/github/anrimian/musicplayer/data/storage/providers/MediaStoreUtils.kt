package com.github.anrimian.musicplayer.data.storage.providers

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.storage.StorageManager
import android.provider.MediaStore
import androidx.core.content.ContextCompat
import com.github.anrimian.musicplayer.data.storage.exceptions.ContentResolverQueryException
import com.github.anrimian.musicplayer.data.storage.exceptions.UnavailableMediaStoreException
import com.github.anrimian.musicplayer.data.storage.providers.FileVolume.Companion.fromCanonicalPathOrNull
import com.github.anrimian.musicplayer.domain.models.exceptions.InvalidVolumeException
import java.io.File
import java.io.IOException

class FileVolume(val storageKey: String, val path: String, val isPrimary: Boolean) {

    companion object {

        /**
         * Derives the volume root from a canonicalized path produced by the framework.
         * Android's storage layout is a contract: every external storage path is either
         *   - "storage/emulated/<userId>/..."  (primary external; <userId> is the multi-user id,
         *     0 for the main user, 10+ for work-profile / Huawei dual-app);
         *   - "storage/<UUID-or-label>/..."    (removable SD/OTG; FAT UUID like "8AB5-181D" or
         *     a named label on old AOSP).
         *
         * Input can be with or without a leading slash.
         *
         * Throws [InvalidVolumeException] when the input does not match the framework shape.
         * Use [fromCanonicalPathOrNull] when the caller needs to handle the failure with a
         * different exception type (e.g. a UX-facing one).
         */
        @JvmStatic
        fun fromCanonicalPath(canonicalPath: String): FileVolume {
            return fromCanonicalPathOrNull(canonicalPath)
                ?: throw InvalidVolumeException("Unable to extract volume path for: $canonicalPath")
        }

        /**
         * Normalizes a filesystem path before it is emitted as a composition parent path or
         * persisted in the database. Leading slashes are stripped, and any of the documented
         * Android *raw mount* representations of a volume are rewritten to the user-facing
         * `storage/<vol>/...` bind-mount form so that one physical volume produces exactly
         * one row in the `volumes` table regardless of which subsystem reported the path.
         */
        @JvmStatic
        fun canonicalize(path: String): String {
            val trimmed = path.trimStart('/')
            return when {
                trimmed.startsWith("mnt/media_rw/") -> {
                    "storage/" + trimmed.removePrefix("mnt/media_rw/")
                }
                trimmed.startsWith("mnt/expand/") -> {
                    "storage/" + trimmed.removePrefix("mnt/expand/")
                }
                trimmed.startsWith("mnt/runtime/") -> {
                    // mnt/runtime/<mode>/<vol>/...  — strip the mode segment.
                    // If the input is malformed (no second segment), leave it alone
                    // and let fromCanonicalPath reject it downstream.
                    val afterMode = trimmed.removePrefix("mnt/runtime/").substringAfter('/', "")
                    if (afterMode.isNotEmpty()) "storage/$afterMode" else trimmed
                }
                trimmed.startsWith("data/media/") -> {
                    "storage/emulated/" + trimmed.removePrefix("data/media/")
                }
                else -> trimmed
            }
        }

        @JvmStatic
        fun fromCanonicalPathOrNull(canonicalPath: String): FileVolume? {
            val normalizedPath = canonicalPath.trimStart('/')
            val storagePrefix = "storage/"
            if (!normalizedPath.startsWith(storagePrefix)) {
                return null
            }
            val afterStorage = normalizedPath.substring(storagePrefix.length)
            val segments = afterStorage.split('/')
            if (segments.isEmpty() || segments[0].isEmpty()) {
                return null
            }
            val firstSegment = segments[0]

            if (firstSegment == "emulated") {
                if (segments.size < 2 || segments[1].isEmpty()) {
                    return null
                }
                val userId = segments[1]
                val volumePath = "storage/emulated/$userId"
                return FileVolume(storageKey = volumePath, path = volumePath, isPrimary = true)
            } else {
                val volumePath = "storage/$firstSegment"
                return FileVolume(storageKey = volumePath, path = volumePath, isPrimary = false)
            }
        }
    }
    
}

object MediaStoreUtils {

    @JvmStatic
    fun checkIfMediaStoreAvailable(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val volumes = MediaStore.getExternalVolumeNames(context)
            if (!volumes.contains(MediaStore.VOLUME_EXTERNAL_PRIMARY)) {
                //can crash in rare weird cases on android 10 so we check for existence
                //reproduce: restart device and we'll get this error
                // (likely we're trying to read it before media store initialization)
                //https://stackoverflow.com/questions/63111091/java-lang-illegalargumentexception-volume-external-primary-not-found-in-android
                throw UnavailableMediaStoreException()
            }
        }
    }

    @JvmStatic
    fun query(
        contentResolver: ContentResolver,
        uri: Uri,
        projection: Array<String>?,
        selection: String?,
        selectionArgs: Array<String>?,
        sortOrder: String?
    ): Cursor? {
        return try {
            contentResolver.query(uri, projection, selection, selectionArgs, sortOrder, null)
        } catch (_: IllegalArgumentException) {
            null // catch-ignore 'volume not found' errors
        } catch (e: Exception) {
            throw ContentResolverQueryException(e)
        }
    }

    fun moveToNext(cursor: Cursor): Boolean {
        try {
            return cursor.moveToNext()
        } catch (e: Exception) {
            throw ContentResolverQueryException(e)
        }
    }

    fun getVolumes(context: Context): Map<String, FileVolume> {
        val storageManager = context.getSystemService(Context.STORAGE_SERVICE) as? StorageManager
            ?: return emptyMap()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val volumes = mutableMapOf<String, FileVolume>()
            for (volume in storageManager.storageVolumes) {
                if (volume.state != Environment.MEDIA_MOUNTED) {
                    continue
                }
                val volumeDir = volume.directory
                if (volumeDir != null) {
                    val key = volume.mediaStoreVolumeName
                        ?: if (volume.isPrimary) MediaStore.VOLUME_EXTERNAL_PRIMARY else volumeDir.name
                    val path = volumeDir.absolutePath.trimStart('/')
                    val storageKey = volume.uuid ?: path
                    volumes[key] = FileVolume(storageKey, path, volume.isPrimary)
                }
            }
            return volumes
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            // StorageManager is the authoritative volume source — getExternalFilesDirs
            // misses volumes on which the system has not provisioned an app-specific
            // directory (common on Huawei/EMUI for secondary/SD/OTG mounts), while
            // MediaStore still indexes their files. Falling back to getExternalFilesDirs
            // there leaves us unable to resolve the volume root and crashes the scanner.
            val volumes = mutableMapOf<String, FileVolume>()
            for (storageVolume in storageManager.storageVolumes) {
                if (storageVolume.state != Environment.MEDIA_MOUNTED) {
                    continue
                }
                val rawPath = try {
                    val getPathMethod = storageVolume.javaClass.getMethod("getPath")
                    getPathMethod.invoke(storageVolume) as? String
                } catch (_: Exception) {
                    null
                } ?: continue
                val rootFile = File(rawPath)
                val path = try {
                    rootFile.canonicalPath.trimStart('/')
                } catch (_: IOException) {
                    rootFile.absolutePath.trimStart('/')
                }
                val isPrimary = storageVolume.isPrimary
                val key = if (isPrimary) "external" else (storageVolume.uuid ?: path)
                volumes[key] = FileVolume(path, path, isPrimary)
            }
            if (volumes.isNotEmpty()) {
                return volumes
            }
            // If nothing is returned from StorageManager - fallback to getExternalFilesDirs
        }
        val volumes = mutableMapOf<String, FileVolume>()
        val externalDirs = ContextCompat.getExternalFilesDirs(context, null)
        for (dir in externalDirs) {
            if (dir == null) {
                continue
            }
            val root = heuristicLookupForVolumeRoot(dir)
            if (root != null) {
                val isPrimary = Environment.isExternalStorageEmulated(root)
                val key = if (isPrimary) {
                    "external"
                } else {
                    getVolumeName(context, root, storageManager)
                }
                val path = try {
                    root.canonicalPath.trimStart('/')
                } catch (_: IOException) {
                    root.absolutePath.trimStart('/')
                }
                volumes[key] = FileVolume(path, path, isPrimary)
            }
        }
        return volumes
    }

    fun getAllowedAudioFolders(): String {
        return getStandardAudioDirectoryNames().joinToString(", ")
    }

    fun getStandardAudioDirectoryNames(): List<String> {
        val standardAudioDirs = mutableListOf(
            Environment.DIRECTORY_MUSIC,
            Environment.DIRECTORY_PODCASTS,
            Environment.DIRECTORY_RINGTONES,
            Environment.DIRECTORY_ALARMS,
            Environment.DIRECTORY_NOTIFICATIONS
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            standardAudioDirs.add(Environment.DIRECTORY_AUDIOBOOKS)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            standardAudioDirs.add(Environment.DIRECTORY_RECORDINGS)
        }
        return standardAudioDirs
    }

    fun getVolumeDisplayName(context: Context, storageKey: String): String {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            return storageKey
        }
        val storageManager = context.getSystemService(Context.STORAGE_SERVICE) as StorageManager

        val foundVolume = storageManager.storageVolumes.find { storageVolume ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (storageVolume.uuid == storageKey) {
                    return@find true
                }
                val dirPath = storageVolume.directory?.absolutePath?.trimStart('/')
                if (dirPath == storageKey) {
                    return@find true
                }
            } else {
                val reflectedPath = try {
                    val getPathMethod = storageVolume.javaClass.getMethod("getPath")
                    (getPathMethod.invoke(storageVolume) as? String)?.trimStart('/')
                } catch (_: Exception) {
                    null
                }
                if (reflectedPath == storageKey) {
                    return@find true
                }
            }
            false
        }
        return foundVolume?.getDescription(context) ?: storageKey
    }

    private fun getVolumeName(context: Context, volumeRoot: File, storageManager: StorageManager): String {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val storageVolume = storageManager.getStorageVolume(volumeRoot)
            if (storageVolume != null) {
                return storageVolume.getDescription(context)
            }
        }
        return if (Environment.isExternalStorageEmulated(volumeRoot)) {
            "Internal Storage"
        } else {
            "Removable Storage"
        }
    }

    private fun heuristicLookupForVolumeRoot(appSpecificDir: File): File? {
        var current: File? = appSpecificDir
        while (current != null) {
            if (current.name == "Android") {
                return current.parentFile
            }
            current = current.parentFile
        }
        return null
    }

}