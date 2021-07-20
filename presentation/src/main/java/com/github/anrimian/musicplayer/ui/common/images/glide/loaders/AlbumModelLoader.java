package com.github.anrimian.musicplayer.ui.common.images.glide.loaders;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import androidx.annotation.NonNull;

import com.bumptech.glide.Priority;
import com.bumptech.glide.load.data.DataFetcher;
import com.github.anrimian.musicplayer.R;
import com.github.anrimian.musicplayer.data.storage.providers.albums.StorageAlbumsProvider;
import com.github.anrimian.musicplayer.domain.models.albums.Album;
import com.github.anrimian.musicplayer.ui.common.images.glide.util.AppModelLoader;

import java.io.IOException;
import java.io.InputStream;

public class AlbumModelLoader extends AppModelLoader<Album, Bitmap> {

    private final Context context;
    private final StorageAlbumsProvider storageAlbumsProvider;

    public AlbumModelLoader(Context context, StorageAlbumsProvider storageAlbumsProvider) {
        this.context = context;
        this.storageAlbumsProvider = storageAlbumsProvider;
    }

    @Override
    protected Object getModelKey(Album album) {
        return album.getId();
    }

    @Override
    protected void loadData(Album album,
                            @NonNull Priority priority,
                            @NonNull DataFetcher.DataCallback<? super Bitmap> callback) {
        callback.onDataReady(extractAlbumCover(album));
    }

    private Bitmap extractAlbumCover(Album album) {
        try (InputStream in = storageAlbumsProvider.getAlbumCoverStream(album.getName())) {
            BitmapFactory.Options opt = new BitmapFactory.Options();
            opt.outWidth = getCoverSize();
            opt.outHeight = getCoverSize();
            return BitmapFactory.decodeStream(in, null, opt);
        } catch (IOException ignores) {
            return null;
        }
    }

    private int getCoverSize() {
        return context.getResources().getInteger(R.integer.icon_image_size);
    }
}
