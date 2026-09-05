package com.github.anrimian.musicplayer.ui.common.images.glide.loaders

import android.content.Context
import android.media.MediaMetadataRetriever
import androidx.core.net.toUri
import com.bumptech.glide.Priority
import com.bumptech.glide.load.data.DataFetcher
import com.github.anrimian.musicplayer.domain.repositories.StorageSourceRepository
import com.github.anrimian.musicplayer.ui.common.images.glide.util.AppModelLoader
import com.github.anrimian.musicplayer.ui.common.images.models.CompositionImage
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStream

class CompositionModelLoader(
    private val context: Context,
    private val storageSourceRepository: StorageSourceRepository,
) : AppModelLoader<CompositionImage, InputStream>() {

    override fun getModelKey(model: CompositionImage): Any {
        return model
    }

    override fun loadData(
        model: CompositionImage,
        priority: Priority,
        callback: DataFetcher.DataCallback<in InputStream>,
    ) {
        var mmr: MediaMetadataRetriever? = null
        try {
            val id = model.id
            var imageBytes = storageSourceRepository.getCompositionArtworkBinaryData(id)
                .blockingGet()

            if (imageBytes == null) {
                mmr = MediaMetadataRetriever()
                val uriString = storageSourceRepository.getCompositionUri(id)
                mmr.setDataSource(context, uriString.toUri())
                imageBytes = mmr.embeddedPicture
            }

            if (imageBytes != null) {
                callback.onDataReady(ByteArrayInputStream(imageBytes))
            } else {
                callback.onDataReady(null)
            }
        } catch (e: Exception) {
            callback.onLoadFailed(e)
        } finally {
            if (mmr != null) {
                try {
                    mmr.release()
                } catch (_: IOException) {}
            }
        }
    }
}
