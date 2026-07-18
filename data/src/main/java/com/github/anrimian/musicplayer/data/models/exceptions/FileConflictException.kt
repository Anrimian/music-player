package com.github.anrimian.musicplayer.data.models.exceptions

import android.net.Uri

class FileConflictException(
    val resolvedName: String,
    val uri: Uri
) : Exception()
