package com.github.anrimian.musicplayer.data.repositories.player

import android.content.Context
import android.net.Uri
import android.util.Base64
import com.github.anrimian.musicplayer.domain.interactors.analytics.Analytics
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.subjects.AsyncSubject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap

class ExternalAudioFileCache(
    private val context: Context,
    private val analytics: Analytics,
    private val ioScheduler: Scheduler,
) {

    private val cacheDir = File(context.cacheDir, "external_audio_cache")

    private val writeOperations: ConcurrentHashMap<String, AsyncSubject<CachedAudioFile>> = ConcurrentHashMap()

    private val savingDisposables = CompositeDisposable()

    fun runAudioFileSaving(uri: Uri, displayName: String) {
        val key = uri.toString()

        if (writeOperations.contains(key)) {
            return
        }

        // we store only one file
        deleteAllCachedFiles()

        val disposable = Completable.fromAction {
            val cacheFile = getCacheFile(uri, displayName)
            val newWriteSubject = AsyncSubject.create<CachedAudioFile>()
            writeOperations[key] = newWriteSubject
            try {
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    FileOutputStream(cacheFile).use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                } ?: throw IOException()

                val localFileUri = Uri.fromFile(cacheFile)
                val cachedFileInfo = CachedAudioFile(localFileUri, displayName, cacheFile.length())
                newWriteSubject.onNext(cachedFileInfo)
                newWriteSubject.onComplete()
            } catch (e: Exception) {
                cacheFile.delete()
                newWriteSubject.onError(e)
                throw e
            } finally {
                writeOperations.remove(key)
            }
        }.subscribeOn(ioScheduler)
            .doOnError(analytics::processNonFatalError)
            .onErrorComplete()
            .subscribe()
        savingDisposables.add(disposable)
    }

    fun getAudioFile(uri: Uri): Maybe<CachedAudioFile> {
        return Maybe.defer {
            val key = uri.toString()
            writeOperations[key]?.let { writeSubject ->
                return@defer writeSubject.firstOrError().toMaybe()
            }
            return@defer Maybe.fromCallable { findCacheFile(uri) }
        }
    }

    fun clearCache() {
        savingDisposables.clear()
        deleteAllCachedFiles()
        writeOperations.clear()
    }

    private fun deleteAllCachedFiles() {
        if (cacheDir.exists()) {
            cacheDir.listFiles()?.forEach(File::delete)
        }
    }

    private fun getCacheFile(originalUri: Uri, displayName: String): File {
        if (!cacheDir.exists()) {
            cacheDir.mkdirs()
        }
        val uriPart = sanitizeUriPartForFileName(originalUri)
        val encodedDisplayNamePart = encodeDisplayName(displayName)

        val fileName = "$uriPart$FILENAME_SEPARATOR$encodedDisplayNamePart"
        return File(cacheDir, fileName)
    }

    private fun findCacheFile(originalUri: Uri): CachedAudioFile? {
        if (!cacheDir.exists()) {
            return null
        }

        val uriPartToMatch = sanitizeUriPartForFileName(originalUri)
        val foundFile = cacheDir.listFiles { _, name ->
            name.startsWith(uriPartToMatch + FILENAME_SEPARATOR)
        }?.firstOrNull() ?: return null

        val fileName = foundFile.name
        val prefixToRemove = uriPartToMatch + FILENAME_SEPARATOR
        val encodedDisplayName = fileName.substringAfter(prefixToRemove, "")
        return CachedAudioFile(
            uri = Uri.fromFile(foundFile),
            displayName = decodeDisplayName(encodedDisplayName),
            size = foundFile.length()
        )
    }

    private fun sanitizeUriPartForFileName(originalUri: Uri): String {
        val lastPathSegment = originalUri.lastPathSegment ?: "unknown_file"
        return lastPathSegment.replace(Regex("[^a-zA-Z0-9._-]"), "_").take(50)
    }

    private fun encodeDisplayName(displayName: String): String {
        val bytes = displayName.toByteArray(Charsets.UTF_8)
        val truncatedBytes = if (bytes.size > 150) bytes.copyOf(150) else bytes
        return Base64.encodeToString(
            truncatedBytes,
            Base64.URL_SAFE or Base64.NO_WRAP
        )
    }

    private fun decodeDisplayName(encodedPart: String): String {
        return try {
            String(Base64.decode(encodedPart, Base64.URL_SAFE or Base64.NO_WRAP))
        } catch (e: IllegalArgumentException) {
            analytics.processNonFatalError(e)
            "undecoded_display_name"
        }
    }

    private companion object {
        const val FILENAME_SEPARATOR = "___"
    }

}