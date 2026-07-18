package com.github.anrimian.musicplayer.data.storage.source

import android.net.Uri
import com.github.anrimian.musicplayer.data.models.composition.file.StorageCompositionSource
import com.github.anrimian.musicplayer.data.models.composition.source.UriContentSource
import com.github.anrimian.musicplayer.data.storage.providers.music.SystemAudioCatalogProvider
import com.github.anrimian.musicplayer.domain.models.composition.content.CompositionContentSource
import java.io.File

open class ContentSourceHelper(
    private val systemAudioCatalogProvider: SystemAudioCatalogProvider
) {

    open fun createUri(source: CompositionContentSource): Uri {
        return when(source) {
            is StorageCompositionSource -> source.uri
            is UriContentSource -> source.uri
            else -> throw IllegalStateException()
        }
    }

    open fun getAsFile(source: CompositionContentSource): File {
        return when(source) {
            is StorageCompositionSource -> {
                val filePath = systemAudioCatalogProvider.getCompositionFilePath(source.uri)
                    ?: throw IllegalStateException("file path not found")
                File(filePath)
            }
            else -> throw IllegalStateException()
        }
    }

}