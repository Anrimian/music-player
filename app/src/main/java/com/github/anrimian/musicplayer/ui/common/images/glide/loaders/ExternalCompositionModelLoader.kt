package com.github.anrimian.musicplayer.ui.common.images.glide.loaders

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import com.bumptech.glide.Priority
import com.bumptech.glide.load.data.DataFetcher
import com.github.anrimian.musicplayer.R
import com.github.anrimian.musicplayer.ui.common.images.glide.util.AppModelLoader
import com.github.anrimian.musicplayer.ui.common.images.models.UriCompositionImage
import com.github.anrimian.musicplayer.ui.utils.ImageUtils
import java.io.IOException

class ExternalCompositionModelLoader(
    private val context: Context,
) : AppModelLoader<UriCompositionImage, Bitmap>() {

    override fun getModelKey(model: UriCompositionImage): Any {
        return model
    }

    override fun loadData(
        model: UriCompositionImage,
        priority: Priority,
        callback: DataFetcher.DataCallback<in Bitmap>,
    ) {
        var mmr: MediaMetadataRetriever? = null
        try {
            mmr = MediaMetadataRetriever()
            mmr.setDataSource(context, model.uri)
            val imageBytes = mmr.embeddedPicture

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
