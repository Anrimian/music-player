package com.github.anrimian.musicplayer.data.storage.providers.music;

import static android.provider.MediaStore.Audio.Media;
import static android.text.TextUtils.isEmpty;
import static com.github.anrimian.musicplayer.data.utils.db.CursorWrapper.getColumnIndex;
import static com.github.anrimian.musicplayer.domain.utils.ListUtils.asList;

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

import androidx.annotation.RequiresApi;
import androidx.annotation.RequiresPermission;
import androidx.collection.LongSparseArray;

import com.github.anrimian.musicplayer.data.models.composition.CompositionId;
import com.github.anrimian.musicplayer.data.storage.exceptions.ContentResolverQueryException;
import com.github.anrimian.musicplayer.data.storage.exceptions.UnavailableMediaStoreException;
import com.github.anrimian.musicplayer.data.storage.exceptions.UpdateMediaStoreException;
import com.github.anrimian.musicplayer.data.storage.providers.albums.StorageAlbum;
import com.github.anrimian.musicplayer.data.storage.providers.albums.StorageAlbumsProvider;
import com.github.anrimian.musicplayer.data.utils.db.CursorWrapper;
import com.github.anrimian.musicplayer.data.utils.rx.content_observer.RxContentObserver;
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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;

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

    public Observable<LongSparseArray<StorageFullComposition>> getCompositionsObservable(
            long minAudioDurationMillis,
            boolean showAllAudioFiles
    ) {
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
        return storageChangeObservable
                .flatMapSingle(o -> getCompositionsSingle(minAudioDurationMillis, showAllAudioFiles));
    }

    @Nullable
    public LongSparseArray<StorageFullComposition> getCompositions(
            long minAudioDurationMillis,
            boolean showAllAudioFiles
    ) {
        String[] query;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            query = new String[] {
                    Media.ARTIST,
                    Media.TITLE,
                    Media.DISPLAY_NAME,
                    Media.RELATIVE_PATH,
                    Media.DURATION,
                    Media.SIZE,
                    Media._ID,
                    Media.ALBUM_ID,
                    Media.DATE_ADDED,
                    Media.DATE_MODIFIED
            };
        } else {
            query = new String[] {
                    Media.ARTIST,
                    Media.TITLE,
                    Media.DISPLAY_NAME,
                    Media.DATA,
                    Media.DURATION,
                    Media.SIZE,
                    Media._ID,
                    Media.ALBUM_ID,
                    Media.DATE_ADDED,
                    Media.DATE_MODIFIED
            };
        }

        Uri uri;
        try {
            uri = getStorageUri();//TODO select all uri's from all available volumes
        } catch (UnavailableMediaStoreException e) {
            return null;
        }

        StringBuilder selectionBuilder = new StringBuilder();
        //also display unsupported or corrupted compositions
        selectionBuilder.append("(" + Media.DURATION + " >= ? OR " + Media.DURATION + " IS NULL)");
        if (!showAllAudioFiles) {
            selectionBuilder.append(" AND ");
            selectionBuilder.append(Media.IS_MUSIC);
            selectionBuilder.append(" = ?");
        }

        String selection = selectionBuilder.toString();
        String[] projection;
        if (showAllAudioFiles) {
            projection = new String[] { String.valueOf(minAudioDurationMillis) };
        } else {
            projection = new String[] { String.valueOf(minAudioDurationMillis), String.valueOf(1) };
        }

        try(Cursor cursor = query(uri, query, selection, projection, null)) {
            if (cursor == null) {
                return new LongSparseArray<>();
            }

            LongSparseArray<StorageAlbum> albums = albumsProvider.getAlbums();

            CursorWrapper cursorWrapper = new CursorWrapper(cursor);
            LongSparseArray<StorageFullComposition> compositions = new LongSparseArray<>(cursor.getCount());

            int artistIndex = getColumnIndex(cursor, Media.ARTIST);
            int titleIndex = getColumnIndex(cursor, Media.TITLE);
            int relativePathIndex = -1;
            int filePathIndex = -1;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                relativePathIndex = getColumnIndex(cursor, Media.RELATIVE_PATH);
            } else {
                filePathIndex = getColumnIndex(cursor, Media.DATA);
            }
            int displayNameIndex = getColumnIndex(cursor, Media.DISPLAY_NAME);
            int durationIndex = getColumnIndex(cursor, Media.DURATION);
            int sizeIndex = getColumnIndex(cursor, Media.SIZE);
            int idIndex = getColumnIndex(cursor, Media._ID);
            int albumIdIndex = getColumnIndex(cursor, Media.ALBUM_ID);
            int dateAddedIndex = getColumnIndex(cursor, Media.DATE_ADDED);
            int dateModifiedIndex = getColumnIndex(cursor, Media.DATE_MODIFIED);

            while (cursor.moveToNext()) {
                StorageFullComposition composition = buildStorageComposition(
                        artistIndex,
                        titleIndex,
                        relativePathIndex,
                        filePathIndex,
                        displayNameIndex,
                        durationIndex,
                        sizeIndex,
                        idIndex,
                        albumIdIndex,
                        dateAddedIndex,
                        dateModifiedIndex,
                        cursorWrapper,
                        albums
                );
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

        try(Cursor cursor = query(
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

        try(Cursor cursor = query(
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

        try(Cursor cursor = query(
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

    public Completable processStorageError(Throwable throwable, List<CompositionId> compositions) {
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

    private Single<LongSparseArray<StorageFullComposition>> getCompositionsSingle(
            long minAudioDurationMillis,
            boolean showAllAudioFiles
    ) {
        return Single.create(emitter -> {
            LongSparseArray<StorageFullComposition> compositions = getCompositions(
                    minAudioDurationMillis,
                    showAllAudioFiles
            );
            if (compositions != null) {
                emitter.onSuccess(compositions);
            }
        });
    }

    private void updateComposition(long id, String key, String value) {
        ContentValues cv = new ContentValues();
        cv.put(key, value);
        contentResolver.update(getCompositionUri(id), cv, null, null);
    }

    private StorageFullComposition buildStorageComposition(
            int artistIndex,
            int titleIndex,
            int relativePathIndex,
            int filePathIndex,
            int displayNameIndex,
            int durationIndex,
            int sizeIndex,
            int idIndex,
            int albumIdIndex,
            int dateAddedIndex,
            int dateModifiedIndex,
            CursorWrapper cursorWrapper,
            LongSparseArray<StorageAlbum> albums
    ) {

        String artist = cursorWrapper.getString(artistIndex);
        String title = cursorWrapper.getString(titleIndex);
//        String album = cursorWrapper.getString(Media.ALBUM);

        String filePath;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            filePath = cursorWrapper.getString(relativePathIndex);
        } else {
            filePath = cursorWrapper.getString(filePathIndex);
            if (isEmpty(filePath)) {
                return null;
            }
            filePath = FileUtils.getParentDirPath(filePath);
        }
        if (filePath == null) {
            filePath = "";
        }

//        String albumKey = cursorWrapper.getString(MediaStore.Audio.Media.ALBUM_KEY);
//        String composer = cursorWrapper.getString(MediaStore.Audio.Media.COMPOSER);
        String displayName = cursorWrapper.getString(displayNameIndex);
        if (TextUtils.isEmpty(displayName)) {
            displayName = "<unknown>";
        }
//        String mimeType = cursorWrapper.getString(Media.MIME_TYPE);

        long duration = cursorWrapper.getLong(durationIndex);
        long size = cursorWrapper.getLong(sizeIndex);
        long id = cursorWrapper.getLong(idIndex);
//        long artistId = cursorWrapper.getLong(Media.ARTIST_ID);
//        long bookmark = cursorWrapper.getLong(Media.BOOKMARK);
        long albumId = cursorWrapper.getLong(albumIdIndex);
        long dateAddedMillis = cursorWrapper.getLong(dateAddedIndex);
        long dateModifiedMillis = cursorWrapper.getLong(dateModifiedIndex);

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

        StorageAlbum storageAlbum = albums.get(albumId);

        return new StorageFullComposition(
                artist,
                title,
                displayName,
                filePath,
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

    @Nullable
    private Cursor query(@RequiresPermission.Read @Nonnull Uri uri,
                         @Nullable String[] projection,
                         @Nullable String selection,
                         @Nullable String[] selectionArgs,
                         @Nullable String sortOrder) {
        try {
            return contentResolver.query(uri, projection, selection, selectionArgs, sortOrder, null);
        } catch (Exception e) {
            throw new ContentResolverQueryException(e);
        }
    }
}
