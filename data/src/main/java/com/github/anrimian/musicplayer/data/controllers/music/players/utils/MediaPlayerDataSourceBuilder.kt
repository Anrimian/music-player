package com.github.anrimian.musicplayer.data.controllers.music.players.utils

import android.content.Context
import android.media.MediaPlayer
import com.github.anrimian.musicplayer.data.models.composition.file.StorageCompositionSource
import com.github.anrimian.musicplayer.data.models.composition.source.UriContentSource
import com.github.anrimian.musicplayer.data.storage.providers.music.StorageMusicProvider
import com.github.anrimian.musicplayer.domain.models.composition.content.CompositionContentSource

open class MediaPlayerDataSourceBuilder(
    protected val context: Context,
    private val storageMusicProvider: StorageMusicProvider
) {

    open fun setMediaSource(
        mediaPlayer: MediaPlayer,
        source: CompositionContentSource
    ) {
        when(source) {
            is StorageCompositionSource -> {
                val fileDescriptor = storageMusicProvider.getFileDescriptor(source.uri)
                mediaPlayer.setDataSource(fileDescriptor)
            }
            is UriContentSource -> mediaPlayer.setDataSource(context, source.uri)
        }
    }

}