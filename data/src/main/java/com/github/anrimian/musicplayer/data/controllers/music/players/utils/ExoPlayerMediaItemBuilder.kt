package com.github.anrimian.musicplayer.data.controllers.music.players.utils

import android.net.Uri
import com.github.anrimian.musicplayer.data.models.composition.file.StorageCompositionSource
import com.github.anrimian.musicplayer.data.models.composition.source.UriContentSource
import com.github.anrimian.musicplayer.domain.models.composition.content.CompositionContentSource

open class ExoPlayerMediaItemBuilder {

    open fun createUri(source: CompositionContentSource): Uri {
        return when(source) {
            is StorageCompositionSource -> source.uri
            is UriContentSource -> source.uri
            else -> throw IllegalStateException()
        }
    }

}