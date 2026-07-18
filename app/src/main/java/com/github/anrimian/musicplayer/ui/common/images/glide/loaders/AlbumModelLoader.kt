package com.github.anrimian.musicplayer.ui.common.images.glide.loaders

import com.bumptech.glide.Priority
import com.bumptech.glide.load.data.DataFetcher
import com.github.anrimian.musicplayer.data.storage.providers.albums.StorageAlbumsProvider
import com.github.anrimian.musicplayer.domain.models.albums.Album
import com.github.anrimian.musicplayer.ui.common.images.glide.util.AppModelLoader
import java.io.IOException
import java.io.InputStream

class AlbumModelLoader(
    private val storageAlbumsProvider: StorageAlbumsProvider
) : AppModelLoader<Album, InputStream>() {

    override fun getModelKey(model: Album): Any {
        return model.id
    }

    override fun loadData(
        model: Album,
        priority: Priority,
        callback: DataFetcher.DataCallback<in InputStream>
    ) {
        try {
            callback.onDataReady(storageAlbumsProvider.getAlbumCoverStream(model.name))
        } catch (e: IOException) {
            callback.onLoadFailed(e)
        }
    }
}
