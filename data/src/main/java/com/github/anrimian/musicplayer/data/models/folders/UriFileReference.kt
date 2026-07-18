package com.github.anrimian.musicplayer.data.models.folders

import android.net.Uri
import androidx.core.net.toUri
import com.github.anrimian.musicplayer.domain.models.folders.FileReference

class UriFileReference(val uri: Uri): FileReference {
    override val path: String = uri.toString()
}

fun String.toFileReference(): FileReference {
    return UriFileReference(this.toUri())
}