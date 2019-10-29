package com.github.anrimian.musicplayer.data.storage.providers.albums;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore.Audio.Albums;

import androidx.collection.LongSparseArray;

import com.github.anrimian.musicplayer.data.utils.db.CursorWrapper;
import com.github.anrimian.musicplayer.data.utils.rx.content_observer.RxContentObserver;

import io.reactivex.Observable;

public class StorageAlbumsProvider {

    private final ContentResolver contentResolver;

    public StorageAlbumsProvider(Context context) {
        contentResolver = context.getContentResolver();
    }

    public Observable<LongSparseArray<StorageAlbum>> getAlbumsObservable() {
        return RxContentObserver.getObservable(contentResolver, Albums.EXTERNAL_CONTENT_URI)
                .map(o -> getAlbums());
    }

    public LongSparseArray<StorageAlbum> getAlbums() {
        try(Cursor cursor = contentResolver.query(Albums.EXTERNAL_CONTENT_URI,
                new String[] {
                        Albums._ID,
                        Albums.ALBUM,
//                        Albums.ALBUM_ID,
                        Albums.ALBUM_KEY,
                        Albums.FIRST_YEAR,
                        Albums.LAST_YEAR,
                        Albums.ARTIST,
                        Albums.ARTIST_ID
                },
                null,
                null,
                null)) {
            if (cursor == null) {
                return new LongSparseArray<>();
            }
            CursorWrapper cursorWrapper = new CursorWrapper(cursor);
            LongSparseArray<StorageAlbum> artists = new LongSparseArray<>(cursor.getCount());
            for (int i = 0; i < cursor.getCount(); i++) {
                cursor.moveToPosition(i);

                StorageAlbum item = getAlbumFromCursor(cursorWrapper);
                artists.put(item.getId(), item);
            }
            return artists;
        }
    }

    private StorageAlbum getAlbumFromCursor(CursorWrapper cursorWrapper) {
        return new StorageAlbum(
                cursorWrapper.getLong(Albums._ID),
                cursorWrapper.getString(Albums.ALBUM),
                cursorWrapper.getString(Albums.ALBUM_KEY),
                cursorWrapper.getString(Albums.ARTIST),
                cursorWrapper.getLong(Albums.ARTIST_ID),
                cursorWrapper.getLong(Albums.FIRST_YEAR),
                cursorWrapper.getLong(Albums.LAST_YEAR)
        );
    }
}
