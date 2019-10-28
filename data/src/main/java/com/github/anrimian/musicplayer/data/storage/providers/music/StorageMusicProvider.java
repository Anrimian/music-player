package com.github.anrimian.musicplayer.data.storage.providers.music;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.os.RemoteException;
import android.provider.MediaStore;

import androidx.collection.LongSparseArray;

import com.github.anrimian.musicplayer.data.storage.exceptions.UpdateMediaStoreException;
import com.github.anrimian.musicplayer.data.utils.IOUtils;
import com.github.anrimian.musicplayer.data.utils.db.CursorWrapper;
import com.github.anrimian.musicplayer.data.utils.rx.content_observer.RxContentObserver;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.Nullable;

import io.reactivex.Observable;

import static android.provider.MediaStore.Audio.Media.ALBUM;
import static android.provider.MediaStore.Audio.Media.ARTIST;
import static android.provider.MediaStore.Audio.Media.DATE_ADDED;
import static android.provider.MediaStore.Audio.Media.DATE_MODIFIED;
import static android.provider.MediaStore.Audio.Media.DURATION;
import static android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
import static android.provider.MediaStore.Audio.Media.IS_MUSIC;
import static android.provider.MediaStore.Audio.Media.TITLE;
import static android.provider.MediaStore.Audio.Media._ID;
import static android.text.TextUtils.isEmpty;

public class StorageMusicProvider {

    private final ContentResolver contentResolver;

    public StorageMusicProvider(Context context) {
        contentResolver = context.getContentResolver();
    }

    public Observable<LongSparseArray<StorageComposition>> getCompositionsObservable() {
        return RxContentObserver.getObservable(contentResolver, EXTERNAL_CONTENT_URI)
                .map(o -> getCompositions());
    }

    public LongSparseArray<StorageComposition> getCompositions() {
        Cursor cursor = null;
        try {
            cursor = contentResolver.query(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    new String[] {
                            ARTIST,
                            TITLE,
                            ALBUM,
                            MediaStore.Images.Media.DATA,
                            DURATION,
                            MediaStore.Images.Media.SIZE,
                            _ID,
                            DATE_ADDED,
                            DATE_MODIFIED},
                    IS_MUSIC + " = ?",
                    new String[] { String.valueOf(1) },
                    null);
            if (cursor == null) {
                return new LongSparseArray<>();
            }
            CursorWrapper cursorWrapper = new CursorWrapper(cursor);
            LongSparseArray<StorageComposition> compositions = new LongSparseArray<>(cursor.getCount());
            for (int i = 0; i < cursor.getCount(); i++) {
                cursor.moveToPosition(i);

                StorageComposition composition = getCompositionFromCursor(cursorWrapper);
                if (composition != null) {
                    compositions.put(composition.getId(), composition);
                }
            }
            return compositions;
        } finally {
            IOUtils.closeSilently(cursor);
        }
    }

    public void deleteCompositions(List<Long> ids) {
        ArrayList<ContentProviderOperation> operations = new ArrayList<>();

        for (Long storageId: ids) {
            ContentProviderOperation operation = ContentProviderOperation.newDelete(EXTERNAL_CONTENT_URI)
                    .withSelection(MediaStore.Audio.Playlists._ID + " = ?", new String[] { String.valueOf(storageId) })
                    .build();

            operations.add(operation);
        }

        try {
            contentResolver.applyBatch(MediaStore.AUTHORITY, operations);
        } catch (OperationApplicationException | RemoteException e) {
            throw new UpdateMediaStoreException(e);
        }
    }

    public void deleteComposition(long id) {
        contentResolver.delete(EXTERNAL_CONTENT_URI,
                MediaStore.Images.Media._ID + " = ?",
                new String[] { String.valueOf(id) });
    }

    @Nullable
    public StorageComposition getComposition(long id) {
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

    public void updateCompositionAuthor(long id, String author) {
        updateComposition(id, MediaStore.Audio.AudioColumns.ARTIST, author);
    }

    public void updateCompositionTitle(long id, String title) {
        updateComposition(id, MediaStore.Audio.AudioColumns.TITLE, title);
    }

    public void updateCompositionFilePath(long id, String filePath) {
        updateComposition(id, MediaStore.Audio.AudioColumns.DATA, filePath);
    }

    public void updateCompositionsFilePath(List<Composition> compositions) {
        ArrayList<ContentProviderOperation> operations = new ArrayList<>();

        for (Composition composition: compositions) {
            Long storageId = composition.getStorageId();
            if (storageId == null) {
                continue;
            }
            ContentProviderOperation operation = ContentProviderOperation.newUpdate(EXTERNAL_CONTENT_URI)
                    .withValue(MediaStore.Images.Media.DATA, composition.getFilePath())
                    .withSelection(MediaStore.Audio.Playlists._ID + " = ?", new String[] { String.valueOf(storageId) })
                    .build();

            operations.add(operation);
        }

        try {
            contentResolver.applyBatch(MediaStore.AUTHORITY, operations);
        } catch (OperationApplicationException | RemoteException e) {
            throw new UpdateMediaStoreException(e);
        }
    }

    private void updateComposition(long id, String key, String value) {
        ContentValues cv = new ContentValues();
        cv.put(key, value);
        contentResolver.update(EXTERNAL_CONTENT_URI,
                cv,
                _ID + " = ?",
                new String[] { String.valueOf(id) });
    }

    private StorageComposition getCompositionFromCursor(CursorWrapper cursorWrapper) {

        String artist = cursorWrapper.getString(ARTIST);
        String title = cursorWrapper.getString(TITLE);
        String album = cursorWrapper.getString(ALBUM);
        String filePath = cursorWrapper.getString(MediaStore.Images.Media.DATA);
//        String albumKey = cursorWrapper.getString(MediaStore.Audio.Media.ALBUM_KEY);
//        String composer = cursorWrapper.getString(MediaStore.Audio.Media.COMPOSER);
//        String displayName = cursorWrapper.getString(DISPLAY_NAME);
//        String mimeType = cursorWrapper.getString(MediaStore.Audio.Media.MIME_TYPE);

        long duration = cursorWrapper.getLong(DURATION);
        long size = cursorWrapper.getLong(MediaStore.Images.Media.SIZE);
        long id = cursorWrapper.getLong(_ID);
//        long artistId = cursorWrapper.getLong(MediaStore.Audio.Media.ARTIST_ID);
//        long bookmark = cursorWrapper.getLong(MediaStore.Audio.Media.BOOKMARK);
//        long albumId = cursorWrapper.getLong(MediaStore.Audio.Media.ALBUM_ID);
        long dateAddedMillis = cursorWrapper.getLong(DATE_ADDED);
        long dateModifiedMillis = cursorWrapper.getLong(DATE_MODIFIED);

//        boolean isAlarm = cursorWrapper.getBoolean(MediaStore.Audio.Media.IS_ALARM);
//        boolean isMusic = cursorWrapper.getBoolean(MediaStore.Audio.Media.IS_MUSIC);
//        boolean isNotification = cursorWrapper.getBoolean(MediaStore.Audio.Media.IS_NOTIFICATION);
//        boolean isPodcast = cursorWrapper.getBoolean(MediaStore.Audio.Media.IS_PODCAST);
//        boolean isRingtone = cursorWrapper.getBoolean(MediaStore.Audio.Media.IS_RINGTONE);

//        @Nullable Integer year = cursorWrapper.getInt(YEAR);

        if (isEmpty(filePath)) {
            return null;
        }
        Date dateAdded;
        if (dateAddedMillis == 0) {
            dateAdded = new Date(System.currentTimeMillis());
        } else {
            dateAdded = new Date(dateAddedMillis * 1000L);
        }
        Date dateModified;
        if (dateModifiedMillis == 0) {
            dateModified = new Date(System.currentTimeMillis());
        } else {
            dateModified  = new Date(dateModifiedMillis * 1000L);
        }

        if (artist.equals("<unknown>")) {
            artist = null;
        }

//        CorruptionType corruptionType = null;
//        if (duration == 0) {
//            corruptionType = CorruptionType.UNKNOWN;
//        }

        return new StorageComposition(
                artist,
                title,
                album,
                filePath,
                duration,
                size,
                id,
                dateAdded,
                dateModified);
    }
}
