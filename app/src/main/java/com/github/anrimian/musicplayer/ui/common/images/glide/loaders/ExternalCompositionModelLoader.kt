package com.github.anrimian.musicplayer.ui.common.images.glide.loaders

import android.content.Context
import android.media.MediaMetadataRetriever
import com.bumptech.glide.Priority
import com.bumptech.glide.load.data.DataFetcher
import com.github.anrimian.musicplayer.ui.common.images.glide.util.AppModelLoader
import com.github.anrimian.musicplayer.ui.common.images.models.UriCompositionImage
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStream

class ExternalCompositionModelLoader(
    private val context: Context,
) : AppModelLoader<UriCompositionImage, InputStream>() {

    override fun getModelKey(model: UriCompositionImage): Any {
        return model
    }

    override fun loadData(
        model: UriCompositionImage,
        priority: Priority,
        callback: DataFetcher.DataCallback<in InputStream>,
    ) {
        var mmr: MediaMetadataRetriever? = null
        try {
            mmr = MediaMetadataRetriever()
            mmr.setDataSource(context, model.uri)
            val imageBytes = mmr.embeddedPicture

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
