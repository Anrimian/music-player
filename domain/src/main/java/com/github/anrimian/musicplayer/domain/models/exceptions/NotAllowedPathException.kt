package com.github.anrimian.musicplayer.domain.models.exceptions

class NotAllowedPathException(val allowedFolders: String) : RuntimeException(allowedFolders)