package com.github.anrimian.musicplayer.data.storage.providers.artist;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore.Audio.Artists;

import com.github.anrimian.musicplayer.data.utils.db.CursorWrapper;
import com.github.anrimian.musicplayer.data.utils.rx.content_observer.RxContentObserver;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import io.reactivex.Observable;

public class StorageArtistsProvider {

    private final ContentResolver contentResolver;

    public StorageArtistsProvider(Context context) {
        contentResolver = context.getContentResolver();
    }

    public Observable<Map<String, StorageArtist>> getArtistsObservable() {
        return RxContentObserver.getObservable(contentResolver, Artists.EXTERNAL_CONTENT_URI)
                .map(o -> getArtists());
    }

    public Map<String, StorageArtist> getArtists() {
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
                return new HashMap<>();
            }
            CursorWrapper cursorWrapper = new CursorWrapper(cursor);
            Map<String, StorageArtist> artists = new HashMap<>(cursor.getCount());
            for (int i = 0; i < cursor.getCount(); i++) {
                cursor.moveToPosition(i);

                StorageArtist artist = getArtistFromCursor(cursorWrapper);
                if (artist != null) {
                    artists.put(artist.getName(), artist);
                }
            }
            return artists;
        }
    }

    @Nullable
    private StorageArtist getArtistFromCursor(CursorWrapper cursorWrapper) {
        String artistName = cursorWrapper.getString(Artists.ARTIST);
        if (artistName == null || artistName.equals("<unknown>")) {
            return null;
        }
        return new StorageArtist(
                cursorWrapper.getLong(Artists._ID),
                artistName
        );
    }
}
