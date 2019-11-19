package com.github.anrimian.musicplayer.data.storage.providers.artist;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore.Audio.Artists;

import androidx.collection.LongSparseArray;

import com.github.anrimian.musicplayer.data.utils.db.CursorWrapper;
import com.github.anrimian.musicplayer.data.utils.rx.content_observer.RxContentObserver;

import javax.annotation.Nullable;

import io.reactivex.Observable;

public class StorageArtistsProvider {

    private final ContentResolver contentResolver;

    public StorageArtistsProvider(Context context) {
        contentResolver = context.getContentResolver();
    }

    public Observable<LongSparseArray<StorageArtist>> getArtistsObservable() {
        return RxContentObserver.getObservable(contentResolver, Artists.EXTERNAL_CONTENT_URI)
                .map(o -> getArtists());
    }

    public LongSparseArray<StorageArtist> getArtists() {
        try(Cursor cursor = contentResolver.query(Artists.EXTERNAL_CONTENT_URI,
                new String[] {
                        Artists._ID,
                        Artists.ARTIST,
//                        Artists.ARTIST_KEY,
//                        MediaStore.Audio.ArtistColumns.NUMBER_OF_ALBUMS,
//                        MediaStore.Audio.ArtistColumns.NUMBER_OF_TRACKS
                },
                null,
                null,
                null)) {
            if (cursor == null) {
                return new LongSparseArray<>();
            }
            CursorWrapper cursorWrapper = new CursorWrapper(cursor);
            LongSparseArray<StorageArtist> artists = new LongSparseArray<>(cursor.getCount());
            for (int i = 0; i < cursor.getCount(); i++) {
                cursor.moveToPosition(i);

                StorageArtist artist = getArtistFromCursor(cursorWrapper);
                if (artist != null) {
                    artists.put(artist.getId(), artist);
                }
            }
            return artists;
        }
    }

    @Nullable
    private StorageArtist getArtistFromCursor(CursorWrapper cursorWrapper) {
        String artistName = cursorWrapper.getString(Artists.ARTIST);
        if (artistName.equals("<unknown>")) {
            return null;
        }
        return new StorageArtist(
                cursorWrapper.getLong(Artists._ID),
                artistName
        );
    }
}
