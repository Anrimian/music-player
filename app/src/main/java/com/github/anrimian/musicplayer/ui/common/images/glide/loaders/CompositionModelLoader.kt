package com.github.anrimian.musicplayer.ui.common.images.glide.loaders

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import com.bumptech.glide.Priority
import com.bumptech.glide.load.data.DataFetcher
import com.github.anrimian.musicplayer.R
import com.github.anrimian.musicplayer.domain.repositories.StorageSourceRepository
import com.github.anrimian.musicplayer.ui.common.images.glide.util.AppModelLoader
import com.github.anrimian.musicplayer.ui.common.images.models.CompositionImage
import com.github.anrimian.musicplayer.ui.utils.ImageUtils
import java.io.IOException

class CompositionModelLoader(
    private val context: Context,
    private val storageSourceRepository: StorageSourceRepository
) : AppModelLoader<CompositionImage, Bitmap>() {

    override fun getModelKey(model: CompositionImage): Any {
        return model
    }

    override fun loadData(
        model: CompositionImage,
        priority: Priority,
        callback: DataFetcher.DataCallback<in Bitmap>
    ) {
        var mmr: MediaMetadataRetriever? = null
        try {
            val id = model.id
            var imageBytes = storageSourceRepository.getCompositionArtworkBinaryData(id)
                .blockingGet()

            if (imageBytes == null) {
                mmr = MediaMetadataRetriever()
                mmr.setDataSource(storageSourceRepository.getCompositionFileDescriptor(id))
                imageBytes = mmr.embeddedPicture
            }

            var bitmap: Bitmap? = null
            if (imageBytes != null) {
                val coverSize = context.resources.getInteger(R.integer.icon_image_full_size)
                bitmap = ImageUtils.decodeBitmap(imageBytes, coverSize)
            }
            callback.onDataReady(bitmap)
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
