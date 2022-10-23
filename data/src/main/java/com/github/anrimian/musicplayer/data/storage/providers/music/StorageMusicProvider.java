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
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.provider.MediaStore;

import androidx.annotation.RequiresApi;
import androidx.annotation.RequiresPermission;
import androidx.collection.LongSparseArray;

import com.github.anrimian.musicplayer.data.models.composition.AudioFileType;
import com.github.anrimian.musicplayer.data.storage.exceptions.NotAllowedPathException;
import com.github.anrimian.musicplayer.data.storage.exceptions.UpdateMediaStoreException;
import com.github.anrimian.musicplayer.data.storage.providers.MediaStoreUtils;
import com.github.anrimian.musicplayer.data.storage.providers.albums.StorageAlbum;
import com.github.anrimian.musicplayer.data.storage.providers.albums.StorageAlbumsProvider;
import com.github.anrimian.musicplayer.data.utils.collections.StringArrayBuilder;
import com.github.anrimian.musicplayer.data.utils.db.CursorWrapper;
import com.github.anrimian.musicplayer.data.utils.rx.content_observer.RxContentObserver;
import com.github.anrimian.musicplayer.domain.models.composition.FullComposition;
import com.github.anrimian.musicplayer.domain.utils.FileUtils;
import com.github.anrimian.musicplayer.domain.utils.ListUtils;
import com.github.anrimian.musicplayer.domain.utils.TextUtils;
import com.github.anrimian.musicplayer.domain.utils.functions.Callback;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
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

    private boolean isContentObserverEnabled = true;

    public StorageMusicProvider(Context context, StorageAlbumsProvider albumsProvider) {
        contentResolver = context.getContentResolver();
        this.context = context;
        this.albumsProvider = albumsProvider;
    }

    public void scanMedia(Uri uri) {
        Intent scanFileIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri);
        context.sendBroadcast(scanFileIntent);
    }

    public void setContentObserverEnabled(boolean enabled) {
        isContentObserverEnabled = enabled;
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
                .filter(o -> isContentObserverEnabled)
                .flatMapSingle(o -> getCompositionsSingle(minAudioDurationMillis, showAllAudioFiles));
    }

    @Nullable
    public LongSparseArray<StorageFullComposition> getCompositions(
            long minAudioDurationMillis,
            boolean showAllAudioFiles
    ) {
        StringArrayBuilder queryBuilder = new StringArrayBuilder(new String[] {
                Media.ARTIST,
                Media.TITLE,
                Media.DISPLAY_NAME,
                Media.DURATION,
                Media.SIZE,
                Media._ID,
                Media.ALBUM_ID,
                Media.DATE_ADDED,
                Media.DATE_MODIFIED,
                Media.IS_MUSIC,
                Media.IS_PODCAST,
                Media.IS_ALARM,
                Media.IS_NOTIFICATION,
                Media.IS_RINGTONE
        });
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            queryBuilder.append(Media.RELATIVE_PATH);
        } else {
            queryBuilder.append(Media.DATA);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            queryBuilder.append(Media.IS_AUDIOBOOK);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            queryBuilder.append(Media.IS_RECORDING);
        }
        String[] query = queryBuilder.build();

        //check how it works
        List<Uri> uris = getStorageUris();
        if (uris.isEmpty()) {
            return null;
        }

//        Uri uri;
//        try {
//            uri = getStorageUri();//try to select all uri's from all available volumes
//        } catch (UnavailableMediaStoreException e) {
//            return null;
//        }

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

        LongSparseArray<StorageFullComposition> compositions = new LongSparseArray<>();
        for (Uri uri: uris) {
            try (Cursor cursor = query(uri, query, selection, projection, null)) {
                if (cursor == null) {
                    return new LongSparseArray<>();
                }

                LongSparseArray<StorageAlbum> albums = albumsProvider.getAlbums();

                CursorWrapper cursorWrapper = new CursorWrapper(cursor);
                LongSparseArray<StorageFullComposition> volumeCompositions = new LongSparseArray<>(cursor.getCount());

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

                int isMusicIndex = getColumnIndex(cursor, Media.IS_MUSIC);
                int isPodcastIndex = getColumnIndex(cursor, Media.IS_PODCAST);
                int isAudioBookIndex = 0;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    isAudioBookIndex = getColumnIndex(cursor, Media.IS_AUDIOBOOK);
                }
                int isRecordingIndex = 0;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    getColumnIndex(cursor, Media.IS_RECORDING);
                }
                int isAlarmIndex = getColumnIndex(cursor, Media.IS_ALARM);
                int isNotificationIndex = getColumnIndex(cursor, Media.IS_NOTIFICATION);
                int isRingtoneIndex = getColumnIndex(cursor, Media.IS_RINGTONE);

                while (MediaStoreUtils.moveToNext(cursor)) {
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
                            isMusicIndex,
                            isPodcastIndex,
                            isAudioBookIndex,
                            isRecordingIndex,
                            isAlarmIndex,
                            isNotificationIndex,
                            isRingtoneIndex,
                            cursorWrapper,
                            albums
                    );
                    if (composition != null) {
                        volumeCompositions.put(composition.getStorageId(), composition);
                    }
                }
                compositions.putAll(volumeCompositions);
            }
        }
        return compositions;
    }

    @Nullable
    public String getCompositionFilePath(long storageId) {
        return getCompositionFilePath(getCompositionUri(storageId));
    }

    @Nullable
    public String getCompositionFilePath(Uri uri) {
        String[] query = { Media.DATA };

        try(Cursor cursor = query(
                uri,
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
    public Long findCompositionByPath(String filePath) {
        String[] query = { Media._ID };

        try(Cursor cursor = query(
                getStorageUri(),
                query,
                Media.DATA + " = ? ",
                new String[] { filePath },
                null)) {
            if (cursor == null || cursor.getCount() == 0) {
                return null;
            }

            CursorWrapper cursorWrapper = new CursorWrapper(cursor);
            if (cursor.moveToFirst()) {
                return cursorWrapper.getLong(Media._ID);
            }
            return null;
        }
    }

    @Nullable
    @RequiresApi(api = Build.VERSION_CODES.Q)
    public Long findCompositionByNameAndRelativePath(String name, String relativePath) {
        String[] query = { Media._ID, Media.RELATIVE_PATH };

        try(Cursor cursor = query(
                getStorageUri(),
                query,
                Media.DISPLAY_NAME + " = ? AND " + Media.RELATIVE_PATH + " = ? ",
                new String[] { name, relativePath },
                null)) {
            if (cursor == null || cursor.getCount() == 0) {
                return null;
            }

            CursorWrapper cursorWrapper = new CursorWrapper(cursor);
            if (cursor.moveToFirst()) {
                return cursorWrapper.getLong(Media._ID);
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
                PendingIntent pIntent = createDeleteRequest(uris);
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
        } catch (Exception e) {
            processEditException(e);
            throw e;
        }
    }

    public Uri getCompositionUri(long id) {
        return ContentUris.withAppendedId(getStorageUri(), id);
    }

    public InputStream getCompositionStream(long id) throws FileNotFoundException {
        return contentResolver.openInputStream(getCompositionUri(id));
    }

    public FileDescriptor getFileDescriptor(Uri uri) throws FileNotFoundException {
        ParcelFileDescriptor fd = contentResolver.openFileDescriptor(uri, "r");
        if (fd == null) {
            throw new RuntimeException("file descriptor not found");
        }
        return fd.getFileDescriptor();
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
        return openCompositionOutputStream(getCompositionUri(id));
    }

    public OutputStream openCompositionOutputStream(Uri uri) throws FileNotFoundException {
        return contentResolver.openOutputStream(uri);
    }

    @Nonnull
    public Uri insertComposition(String name,
                                 String parentPath,
                                 FullComposition composition,
                                 Callback<OutputStream> streamCallback) throws IOException {
        ContentValues cv = new ContentValues();
        cv.put(Media.DISPLAY_NAME, name);
        cv.put(Media.TITLE, composition.getTitle());
        cv.put(Media.MIME_TYPE, "audio/*");
        cv.put(Media.ARTIST, composition.getArtist());
        cv.put(Media.ALBUM, composition.getAlbum());
        cv.put(Media.DURATION, composition.getDuration());
        cv.put(Media.SIZE, composition.getSize());
        cv.put(Media.DATE_ADDED, composition.getDateAdded().getTime() / 1000);
        cv.put(Media.DATE_MODIFIED, composition.getDateModified().getTime() / 1000);
        putAudioFileType(cv, composition.getAudioFileType());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return insertCompositionApi30(name, parentPath, cv, streamCallback);
        }

        //noinspection deprecation
        File parentFolder = new File(Environment.getExternalStorageDirectory() + "/" + parentPath);
        //noinspection ResultOfMethodCallIgnored
        parentFolder.mkdirs();
        File file = new File(parentFolder, name);
        String absolutePath = file.getAbsolutePath();
        Long id = findCompositionByPath(absolutePath);
        if (id != null) {
            return updateComposition(id, cv, streamCallback);
        }

        try (OutputStream outputStream = new FileOutputStream(file, false)) {
            streamCallback.call(outputStream);
        } catch (Exception e) {
            //noinspection ResultOfMethodCallIgnored
            file.delete();
            throw new RuntimeException(e);
        }

        //noinspection deprecation
        cv.put(Media.DATA, absolutePath);

        Uri uri = contentResolver.insert(getStorageUriForInsertion(), cv);
        scanMedia(uri);
        return uri;
    }

    public Completable processStorageException(Throwable throwable, List<Uri> uris) {
        return Completable.fromAction(() -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && throwable instanceof RecoverableSecurityException) {
                PendingIntent pIntent = createWriteRequest(uris);
                throw new RecoverableSecurityExceptionExt(pIntent, throwable.getMessage());
            }
            throw throwable;
        });
    }

    @RequiresApi(Build.VERSION_CODES.R)
    public PendingIntent createWriteRequest(List<Uri> uris) {
        return MediaStore.createWriteRequest(contentResolver, uris);
    }

    @RequiresApi(Build.VERSION_CODES.R)
    public PendingIntent createDeleteRequest(List<Uri> uris) {
        return MediaStore.createDeleteRequest(contentResolver, uris);
    }

    @Nullable
    @RequiresApi(api = Build.VERSION_CODES.Q)
    public Uri getUriByNameAndPath(String name, String parentPath) {
        String relativePath = parentPath + File.separator;
        Long id = findCompositionByNameAndRelativePath(name, relativePath);
        if (id == null) {
            return null;
        }
        return getCompositionUri(id);
    }

    @Nonnull
    @RequiresApi(api = Build.VERSION_CODES.Q)
    private Uri insertCompositionApi30(String name,
                                       String parentPath,
                                       ContentValues contentValues,
                                       Callback<OutputStream> streamCallback) throws IOException {
        Uri uri = null;

        String relativePath = parentPath + File.separator;
        Long id = findCompositionByNameAndRelativePath(name, relativePath);
        if (id != null) {
            return updateComposition(id, contentValues, streamCallback);
        }

        boolean completed = false;
        try {
            contentValues.put(Media.RELATIVE_PATH, parentPath);
            contentValues.put(Media.IS_PENDING, 1);
            uri = contentResolver.insert(getStorageUriForInsertion(), contentValues);

            try (OutputStream outputStream = contentResolver.openOutputStream(uri)) {
                streamCallback.call(outputStream);
            }

            contentValues.clear();
            contentValues.put(Media.IS_PENDING, 0);
            contentResolver.update(uri, contentValues, null, null);
            completed = true;
        } catch (Exception e) {
            processEditException(e);
            throw e;
        } finally {
            if (!completed && uri != null) {
                contentResolver.delete(uri, null, null);
            }
        }
        return uri;
    }

    private Uri updateComposition(long id,
                                  ContentValues contentValues,
                                  Callback<OutputStream> streamCallback) throws IOException {
        Uri uri = getCompositionUri(id);
        try (OutputStream outputStream = contentResolver.openOutputStream(uri)) {
            streamCallback.call(outputStream);
        } catch (Exception e) {
            processEditException(e);
            throw e;
        }
        contentResolver.update(uri, contentValues, null, null);
        return uri;
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
            int isMusicIndex,
            int isPodcastIndex,
            int isAudioBookIndex,
            int isRecordingIndex,
            int isAlarmIndex,
            int isNotificationIndex,
            int isRingtoneIndex,
            CursorWrapper cursorWrapper,
            LongSparseArray<StorageAlbum> albums
    ) {

        String artist = cursorWrapper.getString(artistIndex);
        String title = cursorWrapper.getString(titleIndex);
//        String album = cursorWrapper.getString(Media.ALBUM);

        String filePath;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            filePath = cursorWrapper.getString(relativePathIndex);
            if (filePath == null) {
                filePath = "";
            }
            int lastCharIndex = filePath.length() - 1;
            char lastChar = filePath.charAt(lastCharIndex);
            if (lastChar == '/') {
                filePath = filePath.substring(0, lastCharIndex);
            }
        } else {
            filePath = cursorWrapper.getString(filePathIndex);
            if (isEmpty(filePath)) {
                return null;
            }
            //noinspection ConstantConditions
            filePath = FileUtils.getParentDirPath(filePath);
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

        int audioFileType = getAudioFileType(cursorWrapper,
                isMusicIndex,
                isPodcastIndex,
                isAudioBookIndex,
                isRecordingIndex,
                isAlarmIndex,
                isNotificationIndex,
                isRingtoneIndex);

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
                storageAlbum,
                audioFileType);
    }

    private int getAudioFileType(CursorWrapper cursor,
                                 int isMusicIndex,
                                 int isPodcastIndex,
                                 int isAudioBookIndex,
                                 int isRecordingIndex,
                                 int isAlarmIndex,
                                 int isNotificationIndex,
                                 int isRingtoneIndex) {
        if (cursor.getBoolean(isMusicIndex)) {
            return AudioFileType.MUSIC;
        }
        if (cursor.getBoolean(isPodcastIndex)) {
            return AudioFileType.PODCAST;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && cursor.getBoolean(isAudioBookIndex)) {
            return AudioFileType.AUDIOBOOK;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && cursor.getBoolean(isRecordingIndex)) {
            return AudioFileType.RECORDING;
        }
        if (cursor.getBoolean(isAlarmIndex)) {
            return AudioFileType.ALARM;
        }
        if (cursor.getBoolean(isNotificationIndex)) {
            return AudioFileType.NOTIFICATION;
        }
        if (cursor.getBoolean(isRingtoneIndex)) {
            return AudioFileType.RINGTONE;
        }
        return AudioFileType.MUSIC;
    }

    private void putAudioFileType(ContentValues cv, int audioFileType) {
        switch (audioFileType) {
            case AudioFileType.MUSIC: {
                cv.put(Media.IS_MUSIC, true);
                return;
            }
            case AudioFileType.PODCAST: {
                cv.put(Media.IS_PODCAST, true);
                return;
            }
            case AudioFileType.AUDIOBOOK: {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    cv.put(Media.IS_AUDIOBOOK, true);
                }
                return;
            }
            case AudioFileType.RECORDING: {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    cv.put(Media.IS_RECORDING, true);
                }
                return;
            }
            case AudioFileType.NOTIFICATION: {
                cv.put(Media.IS_NOTIFICATION, true);
                return;
            }
            case AudioFileType.ALARM: {
                cv.put(Media.IS_ALARM, true);
                return;
            }
            case AudioFileType.RINGTONE: {
                cv.put(Media.IS_RINGTONE, true);
                return;
            }
            default: cv.put(Media.IS_MUSIC, true);
        }
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
            MediaStoreUtils.checkIfMediaStoreAvailable(context);
            return MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL);
        } else {
            return Media.EXTERNAL_CONTENT_URI;
        }
    }

    private Uri getStorageUriForInsertion() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
        } else {
            return Media.EXTERNAL_CONTENT_URI;
        }
    }

    private List<Uri> getStorageUris() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Set<String> volumes = MediaStore.getExternalVolumeNames(context);
            List<Uri> uris = new ArrayList<>();
            volumes.forEach(volume -> uris.add(MediaStore.Audio.Media.getContentUri(volume)));
            return uris;
        } else {
            return asList(Media.EXTERNAL_CONTENT_URI);
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
        return MediaStoreUtils.query(contentResolver, uri, projection, selection, selectionArgs, sortOrder);
    }

    private void processEditException(Exception e) {
        String message = e.getMessage();
        if (e instanceof IllegalArgumentException
                && message != null
                && message.contains("not allowed for content")) {
            int indexOfFirstQuotes = message.indexOf('[') + 1;
            String allowedFolders = message.substring(
                    indexOfFirstQuotes,
                    message.indexOf(']', indexOfFirstQuotes)
            );
            throw new NotAllowedPathException(allowedFolders);
        }
    }
}
