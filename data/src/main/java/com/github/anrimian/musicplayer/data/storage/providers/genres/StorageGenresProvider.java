package com.github.anrimian.musicplayer.data.storage.providers.genres;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore.Audio.Genres;

import androidx.collection.LongSparseArray;

import com.github.anrimian.musicplayer.data.utils.db.CursorWrapper;
import com.github.anrimian.musicplayer.data.utils.rx.content_observer.RxContentObserver;

import io.reactivex.Observable;

public class StorageGenresProvider {

    private final ContentResolver contentResolver;

    public StorageGenresProvider(Context context) {
        contentResolver = context.getContentResolver();
    }

    public Observable<LongSparseArray<StorageGenre>> getGenresObservable() {
        return RxContentObserver.getObservable(contentResolver, Genres.EXTERNAL_CONTENT_URI)
                .map(o -> getGenres());
    }

    public LongSparseArray<StorageGenre> getGenres() {
        try(Cursor cursor = contentResolver.query(Genres.EXTERNAL_CONTENT_URI,
                new String[] {
                        Genres._ID,
                        Genres.NAME
                },
                null,
                null,
                null)) {
            if (cursor == null) {
                return new LongSparseArray<>();
            }
            CursorWrapper cursorWrapper = new CursorWrapper(cursor);
            LongSparseArray<StorageGenre> artists = new LongSparseArray<>(cursor.getCount());
            for (int i = 0; i < cursor.getCount(); i++) {
                cursor.moveToPosition(i);

                StorageGenre item = getGenreFromCursor(cursorWrapper);
                artists.put(item.getId(), item);
            }
            return artists;
        }
    }

    public Observable<LongSparseArray<StorageGenreItem>> getGenreItemsObservable(long genreId) {
        return RxContentObserver.getObservable(contentResolver, Genres.Members.getContentUri("external", genreId))
                .map(o -> getGenreItems(genreId));
    }

    public LongSparseArray<StorageGenreItem> getGenreItems(long genreId) {
        try(Cursor cursor = contentResolver.query(
                Genres.Members.getContentUri("external", genreId),
                new String[] {
                        Genres.Members._ID,
                        Genres.Members.AUDIO_ID
                },
                null,
                null,
                null)) {
            if (cursor == null) {
                return new LongSparseArray<>();
            }
            CursorWrapper cursorWrapper = new CursorWrapper(cursor);
            LongSparseArray<StorageGenreItem> artists = new LongSparseArray<>(cursor.getCount());
            for (int i = 0; i < cursor.getCount(); i++) {
                cursor.moveToPosition(i);

                StorageGenreItem item = getGenreItemFromCursor(cursorWrapper);
                artists.put(item.getId(), item);
            }
            return artists;
        }
    }

    private StorageGenreItem getGenreItemFromCursor(CursorWrapper cursorWrapper) {
        return new StorageGenreItem(
                cursorWrapper.getLong(Genres.Members._ID),
                cursorWrapper.getLong(Genres.Members.AUDIO_ID)
        );
    }

    private StorageGenre getGenreFromCursor(CursorWrapper cursorWrapper) {
        return new StorageGenre(
                cursorWrapper.getLong(Genres._ID),
                cursorWrapper.getString(Genres.NAME)
        );
    }
}
