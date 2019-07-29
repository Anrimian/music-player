package com.github.anrimian.musicplayer.data.storage.providers.music;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import com.github.anrimian.musicplayer.data.utils.IOUtils;
import com.github.anrimian.musicplayer.data.utils.db.CursorWrapper;
import com.github.anrimian.musicplayer.data.utils.rx.content_observer.RxContentObserver;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nullable;

import io.reactivex.Observable;

import static android.provider.MediaStore.Audio.Media.ALBUM;
import static android.provider.MediaStore.Audio.Media.ARTIST;
import static android.provider.MediaStore.Audio.Media.DATE_ADDED;
import static android.provider.MediaStore.Audio.Media.DATE_MODIFIED;
import static android.provider.MediaStore.Audio.Media.DISPLAY_NAME;
import static android.provider.MediaStore.Audio.Media.DURATION;
import static android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
import static android.provider.MediaStore.Audio.Media.IS_MUSIC;
import static android.provider.MediaStore.Audio.Media.TITLE;
import static android.provider.MediaStore.Audio.Media._ID;
import static java.util.Collections.emptyMap;

public class StorageMusicProvider {

    private static final int CHANGE_EVENTS_WINDOW_SECONDS = 5;

    private final ContentResolver contentResolver;

    public StorageMusicProvider(Context context) {
        contentResolver = context.getContentResolver();
    }

    public Observable<Map<Long, Composition>> getChangeObservable() {
        return RxContentObserver.getObservable(contentResolver, EXTERNAL_CONTENT_URI)
//                .doOnNext(o -> Log.d("KEK", "received update"))
//                .throttleFirst(CHANGE_EVENTS_WINDOW_SECONDS, TimeUnit.SECONDS)//TODO not this, ask on so
                .map(o -> getCompositions());
    }

    public Map<Long, Composition> getCompositions() {
        return getCompositions(EXTERNAL_CONTENT_URI);
    }

    public void deleteComposition(String path) {//TODO not become update
        contentResolver.delete(EXTERNAL_CONTENT_URI,
                MediaStore.Images.Media.DATA + " = ?",
                new String[] { path });
    }

    @Nullable
    public Composition getComposition(long id) {
        Cursor cursor = null;
        try {
            cursor = contentResolver.query(
                    EXTERNAL_CONTENT_URI,
                    null,
                    MediaStore.Images.Media._ID + " = ?",
                    new String[] { String.valueOf(id) },
                    null);
            if (cursor == null || cursor.getCount() == 0) {
                return null;
            }

            cursor.moveToPosition(0);
            CursorWrapper cursorWrapper = new CursorWrapper(cursor);
            return getCompositionFromCursor(cursorWrapper);
        } finally {
            IOUtils.closeSilently(cursor);
        }
    }

    public void updateCompositionAuthor(Composition composition, String author) {
        updateComposition(composition.getId(), ARTIST, author);
    }

    public void updateCompositionTitle(Composition composition, String title) {
        updateComposition(composition.getId(), TITLE, title);
    }

    public void updateCompositionFilePath(Composition composition, String filePath) {
        updateComposition(composition.getId(), MediaStore.Images.Media.DATA, filePath);
    }

    private void updateComposition(long id, String key, String value) {
        ContentValues cv = new ContentValues();
        cv.put(key, value);
        contentResolver.update(EXTERNAL_CONTENT_URI,
                cv,
                MediaStore.Audio.Playlists._ID + " = ?",
                new String[] { String.valueOf(id) });
    }

    private Map<Long, Composition> getCompositions(Uri uri) {
        Cursor cursor = null;
        try {
            cursor = contentResolver.query(
                    uri,
                    new String[] {
                            ARTIST,
                            TITLE,
                            ALBUM,
                            MediaStore.Images.Media.DATA,
                            DISPLAY_NAME,
                            DURATION,
                            MediaStore.Images.Media.SIZE,
                            _ID,
                            DATE_ADDED,
                            DATE_MODIFIED},
                    IS_MUSIC + " = ?",
                    new String[] { String.valueOf(1) },
                    null);
            if (cursor == null) {
                return emptyMap();
            }
            CursorWrapper cursorWrapper = new CursorWrapper(cursor);
            Map<Long, Composition> compositions = new ConcurrentHashMap<>(cursor.getCount());
            for (int i = 0; i < cursor.getCount(); i++) {
                cursor.moveToPosition(i);

                Composition composition = getCompositionFromCursor(cursorWrapper);
                checkCorruptedComposition(composition);
                compositions.put(composition.getId(), composition);
            }
            return compositions;
        } finally {
            IOUtils.closeSilently(cursor);
        }
    }

    private Composition getCompositionFromCursor(CursorWrapper cursorWrapper) {
        String artist = cursorWrapper.getString(ARTIST);
        String title = cursorWrapper.getString(TITLE);
        String album = cursorWrapper.getString(ALBUM);
        String filePath = cursorWrapper.getString(MediaStore.Images.Media.DATA);
//        String albumKey = cursorWrapper.getString(MediaStore.Audio.Media.ALBUM_KEY);
//        String composer = cursorWrapper.getString(MediaStore.Audio.Media.COMPOSER);
        String displayName = cursorWrapper.getString(DISPLAY_NAME);
//        String mimeType = cursorWrapper.getString(MediaStore.Audio.Media.MIME_TYPE);

        long duration = cursorWrapper.getLong(DURATION);
        long size = cursorWrapper.getLong(MediaStore.Images.Media.SIZE);
        long id = cursorWrapper.getLong(_ID);
//        long artistId = cursorWrapper.getLong(MediaStore.Audio.Media.ARTIST_ID);
//        long bookmark = cursorWrapper.getLong(MediaStore.Audio.Media.BOOKMARK);
//        long albumId = cursorWrapper.getLong(MediaStore.Audio.Media.ALBUM_ID);
        long dateAdded = cursorWrapper.getLong(DATE_ADDED);
        long dateModified = cursorWrapper.getLong(DATE_MODIFIED);

//        boolean isAlarm = cursorWrapper.getBoolean(MediaStore.Audio.Media.IS_ALARM);
//        boolean isMusic = cursorWrapper.getBoolean(MediaStore.Audio.Media.IS_MUSIC);
//        boolean isNotification = cursorWrapper.getBoolean(MediaStore.Audio.Media.IS_NOTIFICATION);
//        boolean isPodcast = cursorWrapper.getBoolean(MediaStore.Audio.Media.IS_PODCAST);
//        boolean isRingtone = cursorWrapper.getBoolean(MediaStore.Audio.Media.IS_RINGTONE);

//        @Nullable Integer year = cursorWrapper.getInt(YEAR);

        if (artist.equals("<unknown>")) {
            artist = null;
        }

        Composition composition = new Composition();
        //composition
        composition.setArtist(artist);
        composition.setTitle(title);
        composition.setAlbum(album);
        composition.setFilePath(filePath);
//        composition.setComposer(composer);
        composition.setDisplayName(displayName);

        composition.setDuration(duration);
        composition.setSize(size);
        composition.setId(id);
        composition.setDateAdded(new Date(dateAdded * 1000L));
        composition.setDateModified(new Date(dateModified * 1000L));

//        composition.setAlarm(isAlarm);
//        composition.setMusic(isMusic);
//        composition.setNotification(isNotification);
//        composition.setPodcast(isPodcast);
//        composition.setRingtone(isRingtone);

//        composition.setYear(year);
        return composition;
    }

    private void checkCorruptedComposition(Composition composition) {
        if (composition.getDuration() == 0) {
            composition.setCorrupted(true);
        }
    }
}
