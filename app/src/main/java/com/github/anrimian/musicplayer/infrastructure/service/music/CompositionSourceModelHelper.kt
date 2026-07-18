package com.github.anrimian.musicplayer.infrastructure.service.music

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import com.github.anrimian.musicplayer.data.models.composition.source.ExternalCompositionSource
import com.github.anrimian.musicplayer.di.Components
import com.github.anrimian.musicplayer.domain.models.composition.source.CompositionSource
import com.github.anrimian.musicplayer.domain.models.composition.source.LibraryCompositionSource
import com.github.anrimian.musicplayer.domain.models.player.service.MusicNotificationSetting
import com.github.anrimian.musicplayer.domain.models.utils.CompositionHelper
import com.github.anrimian.musicplayer.ui.common.format.FormatUtils

object CompositionSourceModelHelper {

    fun areSourcesTheSame(first: CompositionSource?, second: CompositionSource?): Boolean {
        if (first == null || second == null) {
            return false
        }

        if (first.javaClass == second.javaClass) {
            if (first is LibraryCompositionSource) {
                return CompositionHelper.areSourcesTheSame(
                    first.composition,
                    (second as LibraryCompositionSource).composition
                )
            }
            if (first is ExternalCompositionSource) {
                return true
            }
        }
        return false
    }

    fun updateMediaSessionAlbumArt(
        source: CompositionSource?,
        metadataBuilder: MediaMetadataCompat.Builder,
        mediaSession: MediaSessionCompat,
        setting: MusicNotificationSetting
    ): Runnable? {
        val useAlbumArt = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && setting.isShowCovers) || setting.isCoversOnLockScreen
        if (!useAlbumArt || source == null) {
            return null
        }

        val imageLoader = Components.getAppComponent().imageLoader()
        val cancellations = mutableListOf<Runnable>()

        if (source is LibraryCompositionSource) {
            val composition = source.composition
            cancellations.add(imageLoader.loadImageUri(composition) { uri: Uri? ->
                var uriStr: String? = null
                if (uri != null) {
                    uriStr = uri.toString()
                }
                metadataBuilder.putString(MediaMetadataCompat.METADATA_KEY_ART_URI, uriStr)
                mediaSession.setMetadata(metadataBuilder.build())
            })

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                cancellations.add(imageLoader.loadMediaSessionImage(composition) { bitmap: Bitmap? ->
                    putBitmapToMetadata(metadataBuilder, bitmap)
                    mediaSession.setMetadata(metadataBuilder.build())
                })
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                metadataBuilder.putBitmap(MediaMetadataCompat.METADATA_KEY_ART, null)
                mediaSession.setMetadata(metadataBuilder.build())
            } else {
                //uri doesn't work for lock screen background, so put it here
                cancellations.add(imageLoader.loadImage(composition) { bitmap: Bitmap? ->
                    putBitmapToMetadata(metadataBuilder, bitmap)
                    mediaSession.setMetadata(metadataBuilder.build())
                })
            }
        }
        if (source is ExternalCompositionSource) {
            cancellations.add(imageLoader.loadImageUri(source) { uri: Uri? ->
                var uriStr: String? = null
                if (uri != null) {
                    uriStr = uri.toString()
                }
                metadataBuilder.putString(MediaMetadataCompat.METADATA_KEY_ART_URI, uriStr)
                mediaSession.setMetadata(metadataBuilder.build())
            })

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                cancellations.add(imageLoader.loadMediaSessionImage(source) { bitmap: Bitmap? ->
                    putBitmapToMetadata(metadataBuilder, bitmap)
                    mediaSession.setMetadata(metadataBuilder.build())
                })
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                metadataBuilder.putBitmap(MediaMetadataCompat.METADATA_KEY_ART, null)
                mediaSession.setMetadata(metadataBuilder.build())
            } else {
                //uri doesn't work for lock screen background, so put it here
                cancellations.add(imageLoader.loadImage(source) { bitmap: Bitmap? ->
                    putBitmapToMetadata(metadataBuilder, bitmap)
                    mediaSession.setMetadata(metadataBuilder.build())
                })
            }
        }

        if (cancellations.isEmpty()) {
            return null
        }
        return Runnable { cancellations.forEach { runnable -> runnable.run() } }
    }

    fun updateMediaSessionMetadata(
        source: CompositionSource?,
        metadataBuilder: MediaMetadataCompat.Builder,
        mediaSession: MediaSessionCompat,
        context: Context,
        trackNumber: Long,
        totalTracks: Long
    ) {
        if (source is LibraryCompositionSource) {
            val composition = source.composition
            val builder = metadataBuilder
                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, composition.id.toString())
                .putString(
                    MediaMetadataCompat.METADATA_KEY_TITLE,
                    CompositionHelper.formatCompositionName(composition)
                )
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, composition.album)
                .putString(
                    MediaMetadataCompat.METADATA_KEY_ARTIST,
                    FormatUtils.formatCompositionAuthor(composition, context).toString()
                )
                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, composition.duration)
                .putLong(MediaMetadataCompat.METADATA_KEY_TRACK_NUMBER, trackNumber)
                .putLong(MediaMetadataCompat.METADATA_KEY_NUM_TRACKS, totalTracks)
            mediaSession.setMetadata(builder.build())
            return
        }
        if (source is ExternalCompositionSource) {
            val builder = metadataBuilder
                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, source.uri.toString())
                .putString(
                    MediaMetadataCompat.METADATA_KEY_TITLE,
                    CompositionHelper.formatCompositionName(source.title, source.displayName)
                )
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, source.album)
                .putString(
                    MediaMetadataCompat.METADATA_KEY_ARTIST,
                    FormatUtils.formatAuthor(source.artist, context).toString()
                )
                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, source.duration)
            mediaSession.setMetadata(builder.build())
            return
        }
        mediaSession.setMetadata(null)
    }

    private fun putBitmapToMetadata(
        metadataBuilder: MediaMetadataCompat.Builder,
        bitmap: Bitmap?
    ) {
        if (bitmap != null && bitmap.getWidth() > 0 && bitmap.getHeight() > 0) {
            metadataBuilder.putBitmap(MediaMetadataCompat.METADATA_KEY_ART, bitmap)
        } else {
            metadataBuilder.putBitmap(MediaMetadataCompat.METADATA_KEY_ART, null)
        }
    }
}
