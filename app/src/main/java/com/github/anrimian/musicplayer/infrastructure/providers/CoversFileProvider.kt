package com.github.anrimian.musicplayer.infrastructure.providers

import android.content.ContentProvider
import android.content.ContentValues
import android.content.Context
import android.content.pm.ProviderInfo
import android.database.Cursor
import android.net.Uri
import android.os.ParcelFileDescriptor
import com.github.anrimian.musicplayer.domain.utils.TextUtils.getLastPathSegment
import com.github.anrimian.musicplayer.ui.common.images.glide.MyAppGlideModule
import java.io.File
import java.io.FileNotFoundException

class CoversFileProvider : ContentProvider() {

    private lateinit var cacheDir: File

    override fun attachInfo(context: Context, info: ProviderInfo) {
        super.attachInfo(context, info)
        cacheDir = File(context.cacheDir, MyAppGlideModule.IMAGE_CACHE_DIRECTORY)
    }

    override fun onCreate() = true

    override fun openFile(uri: Uri, mode: String): ParcelFileDescriptor? {
        val file = getFileForUri(uri)
        return ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
    }

    override fun getType(uri: Uri) = "image/*"

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor? {
        throw UnsupportedOperationException("No external query")
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        throw UnsupportedOperationException("No external inserts")
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int {
        throw UnsupportedOperationException("No external delete")
    }

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?
    ): Int {
        throw UnsupportedOperationException("No external updates")
    }

    private fun getFileForUri(uri: Uri): File {
        val uriFileName = getLastPathSegment(uri.path)
        val file = File(cacheDir, uriFileName)
        if (!file.exists()) {
            throw FileNotFoundException("not found: $uriFileName")
        }
        return file
    }
}