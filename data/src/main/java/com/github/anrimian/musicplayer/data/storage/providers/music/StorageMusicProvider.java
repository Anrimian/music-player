package com.github.anrimian.musicplayer.data.storage.providers.music;

import android.app.PendingIntent;
import android.app.RecoverableSecurityException;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.collection.LongSparseArray;

import com.github.anrimian.musicplayer.data.storage.exceptions.UnavailableMediaStoreException;
import com.github.anrimian.musicplayer.data.storage.exceptions.UpdateMediaStoreException;
import com.github.anrimian.musicplayer.data.storage.providers.albums.StorageAlbum;
import com.github.anrimian.musicplayer.data.storage.providers.albums.StorageAlbumsProvider;
import com.github.anrimian.musicplayer.data.utils.db.CursorWrapper;
import com.github.anrimian.musicplayer.data.utils.rx.content_observer.RxContentObserver;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.utils.FileUtils;
import com.github.anrimian.musicplayer.domain.utils.ListUtils;
import com.github.anrimian.musicplayer.domain.utils.TextUtils;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;

import static android.provider.MediaStore.Audio.Media;
import static android.text.TextUtils.isEmpty;
import static com.github.anrimian.musicplayer.domain.utils.ListUtils.asList;

public class StorageMusicProvider {

    private final ContentResolver contentResolver;
    private final Context context;
    private final StorageAlbumsProvider albumsProvider;

    public static void checkIfMediaStoreAvailable(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Set<String> volumes = MediaStore.getExternalVolumeNames(context);
            if (!volumes.contains(MediaStore.VOLUME_EXTERNAL_PRIMARY)) {
                //can crash in rare weird cases on android 10 so we check for existence
                //https://stackoverflow.com/questions/63111091/java-lang-illegalargumentexception-volume-external-primary-not-found-in-android
                throw new UnavailableMediaStoreException();
            }
        }
    }

    public StorageMusicProvider(Context context, StorageAlbumsProvider albumsProvider) {
        contentResolver = context.getContentResolver();
        this.context = context;
        this.albumsProvider = albumsProvider;
    }

    public void scanMedia(long id) {
        String filePath = getCompositionFilePath(id);
        if (filePath != null) {
            scanMedia(filePath);
        }
    }

    public void scanMedia(String path) {
        File file = new File(path);
        Uri uri = Uri.fromFile(file);
        Intent scanFileIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri);
        context.sendBroadcast(scanFileIntent);
    }

    public Observable<LongSparseArray<StorageFullComposition>> getCompositionsObservable() {
        Observable<Object> storageChangeObservable = RxContentObserver.getObservable(contentResolver, unsafeGetStorageUri());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            //on new composition content observer not called on android 10
            //but for some reason content observer is called for playlist items when new file added
            //so we create observer for non-existing playlist(!) and it works

            Observable<Object> playListChangeObservable = RxContentObserver.getObservable(
                    contentResolver,
                    MediaStore.Audio.Playlists.Members.getContentUri("external", 0)
            );
            //maybe filter often events?
            storageChangeObservable = Observable.merge(storageChangeObservable, playListChangeObservable);
        }
        return storageChangeObservable.flatMapSingle(o -> Single.create(emitter -> {
            LongSparseArray<StorageFullComposition> compositions = getCompositions();
            if (compositions != null) {
                emitter.onSuccess(compositions);
            }
        }));
    }

    @Nullable
    public LongSparseArray<StorageFullComposition> getCompositions() {
        String[] query;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            query = new String[] {
                    Media.ARTIST,
                    Media.TITLE,
                    Media.DISPLAY_NAME,
//                            Media.ALBUM,
                    Media.DATA,
                    Media.RELATIVE_PATH,
                    Media.DURATION,
                    Media.SIZE,
                    Media._ID,
//                            Media.ARTIST_ID,
                    Media.ALBUM_ID,
                    Media.DATE_ADDED,
                    Media.DATE_MODIFIED
            };
        } else {
            query = new String[] {
                    Media.ARTIST,
                    Media.TITLE,
                    Media.DISPLAY_NAME,
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
        }

        Uri uri;
        try {
            uri = getStorageUri();
        } catch (UnavailableMediaStoreException e) {
            return null;
        }

        try(Cursor cursor = contentResolver.query(
                uri,
                query,
                Media.IS_MUSIC + " = ?",
                new String[] { String.valueOf(1) },
                null)) {
            if (cursor == null) {
                return new LongSparseArray<>();
            }

            LongSparseArray<StorageAlbum> albums = albumsProvider.getAlbums();

            CursorWrapper cursorWrapper = new CursorWrapper(cursor);
            LongSparseArray<StorageFullComposition> compositions = new LongSparseArray<>(cursor.getCount());
            while (cursor.moveToNext()) {
                StorageFullComposition composition = buildStorageComposition(cursorWrapper, albums);
                if (composition != null) {
                    compositions.put(composition.getId(), composition);
                }
            }
            return compositions;
        }
    }

    @Nullable
    public String getCompositionFilePath(long storageId) {
        String[] query;
        query = new String[] {
                Media.DATA,
        };

        try(Cursor cursor = contentResolver.query(
                getCompositionUri(storageId),
                query,
                null,
                null,
                null)) {
            if (cursor == null || cursor.getCount() == 0) {
                return null;
            }

            CursorWrapper cursorWrapper = new CursorWrapper(cursor);
            if (cursor.moveToFirst()) {
                return cursorWrapper.getString(Media.DATA);
            }
            return null;
        }
    }

    @Nullable
    public String getCompositionFileName(long storageId) {
        String[] query;
        query = new String[] {
                Media.DISPLAY_NAME,
        };

        try(Cursor cursor = contentResolver.query(
                getCompositionUri(storageId),
                query,
                null,
                null,
                null)) {
            if (cursor == null || cursor.getCount() == 0) {
                return null;
            }

            CursorWrapper cursorWrapper = new CursorWrapper(cursor);
            if (cursor.moveToFirst()) {
                return cursorWrapper.getString(Media.DISPLAY_NAME);
            }
            return null;
        }
    }

    @Nullable
    public String getCompositionRelativePath(long storageId) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            throw new IllegalStateException();
        }
        String[] query;
        query = new String[] {
                Media.RELATIVE_PATH,
        };

        try(Cursor cursor = contentResolver.query(
                getCompositionUri(storageId),
                query,
                null,
                null,
                null)) {
            if (cursor == null || cursor.getCount() == 0) {
                return null;
            }

            CursorWrapper cursorWrapper = new CursorWrapper(cursor);
            if (cursor.moveToFirst()) {
                return cursorWrapper.getString(Media.RELATIVE_PATH);
            }
            return null;
        }
    }

    public void deleteComposition(long id) {
        deleteCompositions(asList(id));
    }

    public void deleteCompositions(List<Long> ids) {
        ArrayList<ContentProviderOperation> operations = new ArrayList<>();

        for (Long storageId: ids) {
            ContentProviderOperation operation = ContentProviderOperation
                    .newDelete(getCompositionUri(storageId))
                    .build();

            operations.add(operation);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                applyBatch(operations);
            } catch (RecoverableSecurityException e) {
                List<Uri> uris = ListUtils.mapList(ids, this::getCompositionUri);
                PendingIntent pIntent = MediaStore.createDeleteRequest(contentResolver, uris);
                throw new RecoverableSecurityExceptionExt(pIntent, e.getMessage());
            }
        } else {
            applyBatch(operations);
        }
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

    public void updateCompositionFileName(long id, String name) {
        updateComposition(id, MediaStore.Audio.AudioColumns.DISPLAY_NAME, name);
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
            ContentProviderOperation operation = ContentProviderOperation.newUpdate(getCompositionUri(storageId))
                    .withValue(Media.DATA, composition.getFilePath())
                    .build();

            operations.add(operation);
        }

        applyBatch(operations);
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    public void updateCompositionsRelativePath(List<FilePathComposition> compositions) {
        ArrayList<ContentProviderOperation> operations = new ArrayList<>();

        for (FilePathComposition composition: compositions) {
            Long storageId = composition.getStorageId();
            if (storageId == null) {
                continue;
            }
            Log.d("KEK", "updateCompositionsRelativePath: " + composition.getFilePath());
            ContentProviderOperation operation = ContentProviderOperation.newUpdate(getCompositionUri(storageId))
                    .withValue(Media.RELATIVE_PATH, composition.getFilePath())
                    .build();

            operations.add(operation);
        }

        try {
            applyBatch(operations);
        } catch (RecoverableSecurityException e) {
            List<Uri> uris = ListUtils.mapListNotNull(compositions, composition -> {
                Long storageId = composition.getStorageId();
                if (storageId == null) {
                    return null;
                }
                return getCompositionUri(storageId);
            });
            PendingIntent pIntent = MediaStore.createWriteRequest(contentResolver, uris);
            throw new RecoverableSecurityExceptionExt(pIntent, e.getMessage());
        }
    }

    public Uri getCompositionUri(long id) {
        return ContentUris.withAppendedId(getStorageUri(), id);
    }

    public InputStream getCompositionStream(long id) throws FileNotFoundException {
        return contentResolver.openInputStream(getCompositionUri(id));
    }

    public FileDescriptor getFileDescriptor(long id) throws FileNotFoundException {
        Uri uri = getCompositionUri(id);
        ParcelFileDescriptor fd = contentResolver.openFileDescriptor(uri, "r");
        if (fd == null) {
            throw new RuntimeException("file descriptor not found");
        }
        return fd.getFileDescriptor();
    }

    public OutputStream openCompositionOutputStream(Long id) throws FileNotFoundException {
        if (id == null) {
            throw new FileNotFoundException("can not open stream for file without media store id");
        }
        return contentResolver.openOutputStream(getCompositionUri(id));
    }

    public Completable processStorageError(Throwable throwable, List<Composition> compositions) {
        return Completable.fromAction(() -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && throwable instanceof RecoverableSecurityException) {
                List<Uri> uris = ListUtils.mapListNotNull(compositions, composition -> {
                    Long storageId = composition.getStorageId();
                    if (storageId == null) {
                        return null;
                    }
                    return getCompositionUri(storageId);
                });
                PendingIntent pIntent = MediaStore.createWriteRequest(contentResolver, uris);
                throw new RecoverableSecurityExceptionExt(pIntent, throwable.getMessage());
            }
            throw throwable;
        });
    }

/*    public void modifyComposition(long id, ThrowsCallback<OutputStream> modifyFunction)
            throws Exception {
        Uri uri = getCompositionUri(id);

        ContentValues cv = new ContentValues();
        cv.put(MediaStore.Audio.Media.IS_PENDING, 1);
        contentResolver.update(uri,
                cv,
                Media._ID + " = ?",
                new String[] { String.valueOf(id) });

        try (OutputStream os = contentResolver.openOutputStream(uri)) {
            modifyFunction.call(os);
        }

        cv.put(MediaStore.Audio.Media.IS_PENDING, 0);
        contentResolver.update(uri,
                cv,
                Media._ID + " = ?",
                new String[] { String.valueOf(id) });
    }*/

    private void updateComposition(long id, String key, String value) {
        ContentValues cv = new ContentValues();
        cv.put(key, value);
        contentResolver.update(getCompositionUri(id), cv, null, null);
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            relativePath = cursorWrapper.getString(Media.RELATIVE_PATH);
        } else {
            relativePath = FileUtils.getParentDirPath(filePath);
        }
        if (isEmpty(relativePath)) {
            return null;
        }
//        String albumKey = cursorWrapper.getString(MediaStore.Audio.Media.ALBUM_KEY);
//        String composer = cursorWrapper.getString(MediaStore.Audio.Media.COMPOSER);
        String displayName = cursorWrapper.getString(Media.DISPLAY_NAME);
        if (TextUtils.isEmpty(displayName)) {
            displayName = "<unknown>";
        }
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
                displayName,
                filePath,
                relativePath,
                duration,
                size,
                id,
                dateAdded,
                dateModified,
                storageAlbum);
    }

    private Uri unsafeGetStorageUri() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL);
        } else {
            return Media.EXTERNAL_CONTENT_URI;
        }
    }

    @Nullable
    private Uri getStorageUri() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            checkIfMediaStoreAvailable(context);
            return MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL);
        } else {
            return Media.EXTERNAL_CONTENT_URI;
        }
    }

    private void applyBatch(ArrayList<ContentProviderOperation> operations) {
        try {
            contentResolver.applyBatch(MediaStore.AUTHORITY, operations);
        } catch (OperationApplicationException | RemoteException e) {
            throw new UpdateMediaStoreException(e);
        }
    }
}
