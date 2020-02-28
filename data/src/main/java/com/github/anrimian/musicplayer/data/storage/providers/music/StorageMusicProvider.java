package com.github.anrimian.musicplayer.data.storage.providers.music;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.RemoteException;
import android.provider.MediaStore;

import androidx.annotation.RequiresApi;
import androidx.collection.LongSparseArray;

import com.github.anrimian.musicplayer.data.storage.exceptions.UpdateMediaStoreException;
import com.github.anrimian.musicplayer.data.storage.providers.albums.StorageAlbum;
import com.github.anrimian.musicplayer.data.storage.providers.albums.StorageAlbumsProvider;
import com.github.anrimian.musicplayer.data.utils.IOUtils;
import com.github.anrimian.musicplayer.data.utils.db.CursorWrapper;
import com.github.anrimian.musicplayer.data.utils.rx.content_observer.RxContentObserver;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.utils.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.Nullable;

import io.reactivex.Observable;

import static android.provider.MediaStore.Audio.Media;
import static android.text.TextUtils.isEmpty;

public class StorageMusicProvider {

    private final ContentResolver contentResolver;
    private final Context context;
    private final StorageAlbumsProvider albumsProvider;

    public StorageMusicProvider(Context context, StorageAlbumsProvider albumsProvider) {
        contentResolver = context.getContentResolver();
        this.context = context;
        this.albumsProvider = albumsProvider;
    }

    public void scanMedia(String path) {
        File file = new File(path);
        Uri uri = Uri.fromFile(file);
        Intent scanFileIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri);
        context.sendBroadcast(scanFileIntent);
    }

    public Observable<LongSparseArray<StorageFullComposition>> getCompositionsObservable() {
        return RxContentObserver.getObservable(contentResolver, Media.EXTERNAL_CONTENT_URI)
                .map(o -> getCompositions());
    }

    public LongSparseArray<StorageFullComposition> getCompositions() {
        Cursor cursor = null;
        try {
            String[] query;
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//                query = new String[] {
//                        Media.ARTIST,
//                        Media.TITLE,
////                            Media.ALBUM,
//                        Media.DATA,
//                        Media.RELATIVE_PATH,
//                        Media.DURATION,
//                        Media.SIZE,
//                        Media._ID,
////                            Media.ARTIST_ID,
//                        Media.ALBUM_ID,
//                        Media.DATE_ADDED,
//                        Media.DATE_MODIFIED
//                };
//            } else {
                query = new String[] {
                        Media.ARTIST,
                        Media.TITLE,
//                            Media.ALBUM,
                        Media.DATA,
                        Media.DURATION,
                        Media.SIZE,
                        Media._ID,
//                            Media.ARTIST_ID,
                        Media.ALBUM_ID,
                        Media.DATE_ADDED,
                        Media.DATE_MODIFIED
                };
//            }

            cursor = contentResolver.query(
                    Media.EXTERNAL_CONTENT_URI,
                    query,
                    Media.IS_MUSIC + " = ?",
                    new String[] { String.valueOf(1) },
                    null);
            if (cursor == null) {
                return new LongSparseArray<>();
            }

            LongSparseArray<StorageAlbum> albums = albumsProvider.getAlbums();

            CursorWrapper cursorWrapper = new CursorWrapper(cursor);
            LongSparseArray<StorageFullComposition> compositions = new LongSparseArray<>(cursor.getCount());
            for (int i = 0; i < cursor.getCount(); i++) {
                cursor.moveToPosition(i);

                StorageFullComposition composition = buildStorageComposition(cursorWrapper, albums);
                if (composition != null) {
                    compositions.put(composition.getId(), composition);
                }
            }
            return compositions;
        } finally {
            IOUtils.closeSilently(cursor);
        }
    }

    @Nullable
    public String getCompositionFilePath(long storageId) {
        Cursor cursor = null;
        try {
            String[] query;
                query = new String[] {
                        Media.DATA,
                };

            cursor = contentResolver.query(
                    Media.EXTERNAL_CONTENT_URI,
                    query,
                    Media._ID + " = ?",
                    new String[] { String.valueOf(storageId) },
                    null);
            if (cursor == null) {
                return null;
            }

            CursorWrapper cursorWrapper = new CursorWrapper(cursor);
            cursor.moveToFirst();
            return cursorWrapper.getString(Media.DATA);
        } finally {
            IOUtils.closeSilently(cursor);
        }
    }

    public void deleteCompositions(List<Long> ids) {
        ArrayList<ContentProviderOperation> operations = new ArrayList<>();

        for (Long storageId: ids) {
            ContentProviderOperation operation = ContentProviderOperation.newDelete(Media.EXTERNAL_CONTENT_URI)
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
        contentResolver.delete(Media.EXTERNAL_CONTENT_URI,
                Media._ID + " = ?",
                new String[] { String.valueOf(id) });
    }

    public void updateCompositionArtist(long id, String author) {
        updateComposition(id, MediaStore.Audio.AudioColumns.ARTIST, author);
    }

    public void updateCompositionAlbum(long id, String album) {
        updateComposition(id, MediaStore.Audio.AudioColumns.ALBUM, album);
    }

    public void updateCompositionTitle(long id, String title) {
        updateComposition(id, MediaStore.Audio.AudioColumns.TITLE, title);
    }

    public void updateCompositionFilePath(long id, String filePath) {
        updateComposition(id, MediaStore.Audio.AudioColumns.DATA, filePath);
    }

    public void updateCompositionsFilePath(List<FilePathComposition> compositions) {
        ArrayList<ContentProviderOperation> operations = new ArrayList<>();

        for (FilePathComposition composition: compositions) {
            Long storageId = composition.getStorageId();
            if (storageId == null) {
                continue;
            }
            ContentProviderOperation operation = ContentProviderOperation.newUpdate(Media.EXTERNAL_CONTENT_URI)
                    .withValue(Media.DATA, composition.getFilePath())
                    .withSelection(Media._ID + " = ?", new String[] { String.valueOf(storageId) })
                    .build();

            operations.add(operation);
        }

        try {
            contentResolver.applyBatch(MediaStore.AUTHORITY, operations);
        } catch (OperationApplicationException | RemoteException e) {
            throw new UpdateMediaStoreException(e);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    public void updateCompositionsRelativePath(List<Composition> compositions,
                                               String oldPath,
                                               String newPath) {
        ArrayList<ContentProviderOperation> operations = new ArrayList<>();

//        Cursor cursor = null;
//        try {
//            String[] query;
//            query = new String[] {
//                    Media.RELATIVE_PATH,
//                    Media._ID,
//            };
//
//            cursor = contentResolver.query(
//                    Media.EXTERNAL_CONTENT_URI,
//                    query,
//                    Media.RELATIVE_PATH + " LIKE ?",
//                    new String[] { oldPath },//need selection
//                    null);
//            if (cursor == null) {
//                return;
//            }
//
//            CursorWrapper cursorWrapper = new CursorWrapper(cursor);
//            for (int i = 0; i < cursor.getCount(); i++) {
//                cursor.moveToPosition(i);
//                long id = cursorWrapper.getLong(Media._ID);
//                String relativePath = cursorWrapper.getString(Media.RELATIVE_PATH);
//                if (relativePath == null) {
//                    continue;
//                }
//
//                String path = relativePath.replace(oldPath, newPath);
//
//                ContentProviderOperation operation = ContentProviderOperation.newUpdate(Media.EXTERNAL_CONTENT_URI)
//                        .withValue(Media.RELATIVE_PATH, path)
//                        .withSelection(Media._ID + " = ?", new String[] { String.valueOf(id) })
//                        .build();
//
//                operations.add(operation);
//
//            }
//        } finally {
//            IOUtils.closeSilently(cursor);
//        }

        for (Composition composition: compositions) {
            Long storageId = composition.getStorageId();
            if (storageId == null) {
                continue;
            }
//            String path = composition.getFilePath().replace(oldPath, newPath);
            ContentProviderOperation operation = ContentProviderOperation.newUpdate(Media.EXTERNAL_CONTENT_URI)
                    .withValue(Media.RELATIVE_PATH, newPath)//we can't move? Path seems right
                    .withSelection(Media._ID + " = ?", new String[] { String.valueOf(storageId) })
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
        contentResolver.update(Media.EXTERNAL_CONTENT_URI,
                cv,
                Media._ID + " = ?",
                new String[] { String.valueOf(id) });
    }

    private StorageFullComposition buildStorageComposition(CursorWrapper cursorWrapper,
                                                           LongSparseArray<StorageAlbum> albums) {

        String artist = cursorWrapper.getString(Media.ARTIST);
        String title = cursorWrapper.getString(Media.TITLE);
//        String album = cursorWrapper.getString(Media.ALBUM);
        String filePath = cursorWrapper.getString(Media.DATA);
        if (isEmpty(filePath)) {
            return null;
        }

        String relativePath;
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//            relativePath = cursorWrapper.getString(Media.RELATIVE_PATH);
//        } else {
            relativePath = FileUtils.getParentDirPath(filePath);
//        }
        if (isEmpty(relativePath)) {
            return null;
        }
//        String albumKey = cursorWrapper.getString(MediaStore.Audio.Media.ALBUM_KEY);
//        String composer = cursorWrapper.getString(MediaStore.Audio.Media.COMPOSER);
//        String displayName = cursorWrapper.getString(DISPLAY_NAME);
//        String mimeType = cursorWrapper.getString(Media.MIME_TYPE);

        long duration = cursorWrapper.getLong(Media.DURATION);
        long size = cursorWrapper.getLong(Media.SIZE);
        long id = cursorWrapper.getLong(Media._ID);
//        long artistId = cursorWrapper.getLong(Media.ARTIST_ID);
//        long bookmark = cursorWrapper.getLong(Media.BOOKMARK);
        long albumId = cursorWrapper.getLong(Media.ALBUM_ID);
        long dateAddedMillis = cursorWrapper.getLong(Media.DATE_ADDED);
        long dateModifiedMillis = cursorWrapper.getLong(Media.DATE_MODIFIED);

//        boolean isAlarm = cursorWrapper.getBoolean(Media.IS_ALARM);
//        boolean isMusic = cursorWrapper.getBoolean(Media.IS_MUSIC);
//        boolean isNotification = cursorWrapper.getBoolean(Media.IS_NOTIFICATION);
//        boolean isPodcast = cursorWrapper.getBoolean(Media.IS_PODCAST);
//        boolean isRingtone = cursorWrapper.getBoolean(Media.IS_RINGTONE);

//        @Nullable Integer year = cursorWrapper.getInt(YEAR);


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

        if (artist != null && artist.equals("<unknown>")) {
            artist = null;
        }

//        CorruptionType corruptionType = null;
//        if (duration == 0) {
//            corruptionType = CorruptionType.UNKNOWN;
//        }

        StorageAlbum storageAlbum = albums.get(albumId);

        return new StorageFullComposition(
                artist,
                title,
                filePath,
                relativePath,
                duration,
                size,
                id,
                dateAdded,
                dateModified,
                storageAlbum);
    }
}
