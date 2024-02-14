package com.github.anrimian.musicplayer.data.models.exceptions

class PlaylistEntryInsertException(override val cause: Throwable): RuntimeException(cause)