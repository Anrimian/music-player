package com.github.anrimian.musicplayer.ui.common.images.glide.loaders;

import androidx.annotation.NonNull;

import com.bumptech.glide.Priority;
import com.bumptech.glide.load.data.DataFetcher;
import com.github.anrimian.musicplayer.data.storage.providers.albums.StorageAlbumsProvider;
import com.github.anrimian.musicplayer.domain.models.albums.Album;
import com.github.anrimian.musicplayer.ui.common.images.glide.util.AppModelLoader;

import java.io.IOException;
import java.io.InputStream;

public class AlbumModelLoader extends AppModelLoader<Album, InputStream> {

    private final StorageAlbumsProvider storageAlbumsProvider;

    public AlbumModelLoader(StorageAlbumsProvider storageAlbumsProvider) {
        this.storageAlbumsProvider = storageAlbumsProvider;
    }

    @Override
    protected Object getModelKey(Album album) {
        return album.getId();
    }

    @Override
    protected void loadData(Album album,
                            @NonNull Priority priority,
                            @NonNull DataFetcher.DataCallback<? super InputStream> callback) {
        try {
            callback.onDataReady(storageAlbumsProvider.getAlbumCoverStream(album.getName()));
        } catch (IOException e) {
            callback.onLoadFailed(e);
        }
    }

}
