package com.github.anrimian.musicplayer.data.storage.providers.albums;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore.Audio.Albums;

import androidx.annotation.Nullable;
import androidx.collection.LongSparseArray;

import com.github.anrimian.musicplayer.data.storage.providers.MediaStoreUtils;
import com.github.anrimian.musicplayer.data.utils.db.CursorWrapper;

import java.io.FileNotFoundException;
import java.io.InputStream;

public class StorageAlbumsProvider {

    private final ContentResolver contentResolver;

    public StorageAlbumsProvider(Context context) {
        contentResolver = context.getContentResolver();
    }

    public LongSparseArray<StorageAlbum> getAlbums() {
        try(Cursor cursor = MediaStoreUtils.query(contentResolver,
                Albums.EXTERNAL_CONTENT_URI,
                new String[] {
                        Albums._ID,
                        Albums.ALBUM,
                        Albums.ARTIST,
                },
                null,
                null,
                null)) {
            if (cursor == null) {
                return new LongSparseArray<>();
            }
            CursorWrapper cursorWrapper = new CursorWrapper(cursor);
            LongSparseArray<StorageAlbum> artists = new LongSparseArray<>(cursor.getCount());
            while (cursor.moveToNext()) {
                StorageAlbum item = getAlbumFromCursor(cursorWrapper);
                if (item != null) {
                    artists.put(item.getId(), item);
                }
            }
            return artists;
        }
    }

    public InputStream getAlbumCoverStream(String name) throws FileNotFoundException {
        long id = getAlbumIdByName(name);
        Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");
        Uri uri = ContentUris.withAppendedId(sArtworkUri, id);
        return contentResolver.openInputStream(uri);
    }

    private long getAlbumIdByName(String name) {
        try (Cursor cursor = contentResolver.query(Albums.EXTERNAL_CONTENT_URI,
                new String[] {
                        Albums._ID,
                },
                Albums.ALBUM + " = ? ",
                new String[] { name },
                null)) {
            if (cursor != null && cursor.moveToFirst()) {
                CursorWrapper cursorWrapper = new CursorWrapper(cursor);
                return cursorWrapper.getLong(Albums._ID);
            }
        }
        return 0;
    }

    @Nullable
    private StorageAlbum getAlbumFromCursor(CursorWrapper cursorWrapper) {
        String name = cursorWrapper.getString(Albums.ALBUM);
        if (name == null || name.equals("<unknown>")) {
            return null;
        }
        String artist = cursorWrapper.getString(Albums.ARTIST);
        if (artist != null && artist.equals("<unknown>")) {
            artist = null;
        }

        return new StorageAlbum(cursorWrapper.getLong(Albums._ID), name, artist);
    }

}
