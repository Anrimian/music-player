package com.github.anrimian.musicplayer.data.storage.providers.playlists;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio.Playlists;

import com.github.anrimian.musicplayer.data.models.StoragePlayList;
import com.github.anrimian.musicplayer.data.models.exceptions.CompositionNotDeletedException;
import com.github.anrimian.musicplayer.data.models.exceptions.CompositionNotMovedException;
import com.github.anrimian.musicplayer.data.models.exceptions.PlayListAlreadyDeletedException;
import com.github.anrimian.musicplayer.data.models.exceptions.PlayListNotCreatedException;
import com.github.anrimian.musicplayer.data.models.exceptions.PlayListNotDeletedException;
import com.github.anrimian.musicplayer.data.models.exceptions.PlayListNotModifiedException;
import com.github.anrimian.musicplayer.data.utils.IOUtils;
import com.github.anrimian.musicplayer.data.utils.db.CursorWrapper;
import com.github.anrimian.musicplayer.data.utils.rx.content_observer.RxContentObserver;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.playlist.PlayListItem;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.Nullable;

import io.reactivex.Observable;

import static android.provider.BaseColumns._ID;
import static android.provider.MediaStore.Audio.AudioColumns.ALBUM;
import static android.provider.MediaStore.Audio.AudioColumns.ARTIST;
import static android.provider.MediaStore.Audio.AudioColumns.DURATION;
import static android.provider.MediaStore.Audio.Playlists.Members.AUDIO_ID;
import static android.provider.MediaStore.Audio.Playlists.Members.getContentUri;
import static android.provider.MediaStore.MediaColumns.DATE_ADDED;
import static android.provider.MediaStore.MediaColumns.DATE_MODIFIED;
import static android.provider.MediaStore.MediaColumns.DISPLAY_NAME;
import static android.provider.MediaStore.MediaColumns.TITLE;
import static android.text.TextUtils.isEmpty;
import static com.github.anrimian.musicplayer.domain.utils.Objects.requireNonNull;
import static java.util.Collections.emptyList;

public class StoragePlayListsProvider {

    private final ContentResolver contentResolver;

    public StoragePlayListsProvider(Context context) {
        contentResolver = context.getContentResolver();
    }

    public Observable<List<StoragePlayList>> getChangeObservable() {
        return RxContentObserver.getObservable(contentResolver, Playlists.EXTERNAL_CONTENT_URI)
                .map(o -> getPlayLists());
    }

    public List<StoragePlayList> getPlayLists() {
        Cursor cursor = null;
        try {
            cursor = contentResolver.query(
                    Playlists.EXTERNAL_CONTENT_URI,
                    null,
                    null,
                    null,
                    null);
            if (cursor == null) {
                return emptyList();
            }
            CursorWrapper cursorWrapper = new CursorWrapper(cursor);

            List<StoragePlayList> playLists = new ArrayList<>();
            for (int i = 0; i < cursor.getCount(); i++) {
                cursor.moveToPosition(i);
                playLists.add(getPlayListFromCursor(cursorWrapper));
            }
            return playLists;
        } finally {
            IOUtils.closeSilently(cursor);
        }
    }

    public StoragePlayList createPlayList(String name) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(Playlists.NAME, name);
        contentValues.put(Playlists.DATE_MODIFIED, System.currentTimeMillis() / 1000L);
        Uri uri = contentResolver.insert(Playlists.EXTERNAL_CONTENT_URI, contentValues);
        if (uri == null || isEmpty(uri.getLastPathSegment())) {
            throw new PlayListNotCreatedException();
        }
        long id = Long.valueOf(uri.getLastPathSegment());
        StoragePlayList playList = findPlayList(id);
        if (playList == null) {
            throw new PlayListNotCreatedException();
        }
        return playList;
    }

    public void deletePlayList(long id) {
        int deletedRows = contentResolver.delete(Playlists.EXTERNAL_CONTENT_URI,
                Playlists._ID + " = ?",
                new String[] { String.valueOf(id) });

        if (deletedRows == 0) {
            StoragePlayList storagePlayList = findPlayList(id);
            if (storagePlayList == null) {
                throw new PlayListAlreadyDeletedException();
            }
            throw new PlayListNotDeletedException();
        }
    }

    public Observable<List<PlayListItem>> getPlayListChangeObservable(long playListId) {
        return RxContentObserver.getObservable(contentResolver, getContentUri("external", playListId))
                .map(o -> getPlayListItems(playListId));
    }

    public List<PlayListItem> getPlayListItems(long playListId) {
        Cursor cursor = null;
        try {
            cursor = contentResolver.query(
                    getContentUri("external", playListId),
                    new String[] {
                            ARTIST,
                            TITLE,
                            ALBUM,
                            MediaStore.Images.Media.DATA,
                            DISPLAY_NAME,
                            DURATION,
                            MediaStore.Images.Media.SIZE,
                            AUDIO_ID,
                            _ID,
                            DATE_ADDED,
                            DATE_MODIFIED},
                    null,
                    null,
                    Playlists.Members.PLAY_ORDER);
            if (cursor == null) {
                return emptyList();
            }
            CursorWrapper cursorWrapper = new CursorWrapper(cursor);
            List<PlayListItem> compositions = new ArrayList<>(cursor.getCount());
            for (int i = 0; i < cursor.getCount(); i++) {
                cursor.moveToPosition(i);

                PlayListItem item = getPlayListItemFromCursor(cursorWrapper);
                compositions.add(item);
            }
            return compositions;
        } finally {
            IOUtils.closeSilently(cursor);
        }
    }

    public void addCompositionToPlayList(long compositionId, long playListId, int position) {
        ContentValues values = new ContentValues();
        values.put(Playlists.Members.PLAY_ORDER, position);
        values.put(Playlists.Members.AUDIO_ID, compositionId);
        values.put(Playlists.Members.PLAYLIST_ID, playListId);

        Uri uri = contentResolver.insert(getContentUri("external", playListId), values);
        if (uri == null) {
            throw new PlayListNotModifiedException();
        }
        updateModifyTime(playListId);
    }

    public void addCompositionsToPlayList(List<Composition> compositions, long playListId, int startPosition) {
        int position = startPosition;
        ContentValues[] valuesList = new ContentValues[compositions.size()];
        for (int i = 0; i < compositions.size(); i++) {
            long compositionId = compositions.get(i).getId();
            ContentValues values = new ContentValues();
            values.put(Playlists.Members.PLAY_ORDER, position);
            values.put(Playlists.Members.AUDIO_ID, compositionId);
            values.put(Playlists.Members.PLAYLIST_ID, playListId);
            valuesList[i] = values;
            position++;
        }

        int inserted = contentResolver.bulkInsert(getContentUri("external", playListId), valuesList);
        if (inserted == 0) {
            throw new PlayListNotModifiedException();
        }
        updateModifyTime(playListId);
    }

    public void deleteItemFromPlayList(long itemId, long playListId) {
        int deletedRows = contentResolver.delete(
                getContentUri("external", playListId),
                Playlists.Members._ID + " = ?",
                new String[] { String.valueOf(itemId) }
        );

        if (deletedRows == 0) {
            throw new CompositionNotDeletedException();
        }
    }

    public void moveItemInPlayList(long playListId, int from, int to) {
        boolean moved = Playlists.Members.moveItem(contentResolver, playListId, from, to);
        if (!moved) {
            throw new CompositionNotMovedException();
        }
    }

    private void updateModifyTime(long playListId) {
        ContentValues playListValues = new ContentValues();
        playListValues.put(Playlists.DATE_MODIFIED, System.currentTimeMillis() / 1000L);
        contentResolver.update(Playlists.EXTERNAL_CONTENT_URI,
                playListValues,
                Playlists._ID + " = ?",
                new String[] { String.valueOf(playListId) });
    }

    @Nullable
    private StoragePlayList findPlayList(long id) {
        Cursor cursor = null;
        try {
            cursor = contentResolver.query(
                    Playlists.EXTERNAL_CONTENT_URI,
                    null,
                    Playlists._ID + " = ?",
                    new String[] { String.valueOf(id) },
                    Playlists.DATE_ADDED + " DESC");
            if (cursor != null && cursor.moveToFirst()) {
                CursorWrapper cursorWrapper = new CursorWrapper(cursor);
                return getPlayListFromCursor(cursorWrapper);
            }
            return null;
        } finally {
            IOUtils.closeSilently(cursor);
        }
    }

    private PlayListItem getPlayListItemFromCursor(CursorWrapper cursorWrapper) {
        String artist = cursorWrapper.getString(Playlists.Members.ARTIST);
        String title = cursorWrapper.getString(Playlists.Members.TITLE);
        String album = cursorWrapper.getString(Playlists.Members.ALBUM);
        String filePath = cursorWrapper.getString(Playlists.Members.DATA);
//        String albumKey = cursorWrapper.getString(Playlists.Members.ALBUM_KEY);
//        String composer = cursorWrapper.getString(Playlists.Members.COMPOSER);
        String displayName = cursorWrapper.getString(Playlists.Members.DISPLAY_NAME);
//        String mimeType = cursorWrapper.getString(Playlists.Members.MIME_TYPE);

        long itemId = cursorWrapper.getLong(Playlists.Members._ID);
        long duration = cursorWrapper.getLong(Playlists.Members.DURATION);
        long size = cursorWrapper.getLong(Playlists.Members.SIZE);
        long audioId = cursorWrapper.getLong(Playlists.Members.AUDIO_ID);
//        long artistId = cursorWrapper.getLong(Playlists.Members.ARTIST_ID);
//        long bookmark = cursorWrapper.getLong(Playlists.Members.BOOKMARK);
//        long albumId = cursorWrapper.getLong(Playlists.Members.ALBUM_ID);
        long dateAdded = cursorWrapper.getLong(Playlists.Members.DATE_ADDED);
        long dateModified = cursorWrapper.getLong(Playlists.Members.DATE_MODIFIED);

//        boolean isAlarm = cursorWrapper.getBoolean(Playlists.Members.IS_ALARM);
//        boolean isMusic = cursorWrapper.getBoolean(Playlists.Members.IS_MUSIC);
//        boolean isNotification = cursorWrapper.getBoolean(Playlists.Members.IS_NOTIFICATION);
//        boolean isPodcast = cursorWrapper.getBoolean(Playlists.Members.IS_PODCAST);
//        boolean isRingtone = cursorWrapper.getBoolean(Playlists.Members.IS_RINGTONE);

//        @Nullable Integer year = cursorWrapper.getInt(Playlists.Members.YEAR);

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
        composition.setId(audioId);
        composition.setDateAdded(new Date(dateAdded * 1000L));
        composition.setDateModified(new Date(dateModified * 1000L));

//        composition.setAlarm(isAlarm);
//        composition.setMusic(isMusic);
//        composition.setNotification(isNotification);
//        composition.setPodcast(isPodcast);
//        composition.setRingtone(isRingtone);

//        composition.setYear(year);

        checkCorruptedComposition(composition);

        return new PlayListItem(itemId, composition);
    }

    private void checkCorruptedComposition(Composition composition) {
        if (composition.getDuration() == 0) {
            composition.setCorrupted(true);
        }
    }

    private StoragePlayList getPlayListFromCursor(CursorWrapper cursorWrapper) {
        long id = cursorWrapper.getLong(Playlists._ID);
        String name = cursorWrapper.getString(Playlists.NAME);
        long dateAdded = cursorWrapper.getLong(Playlists.DATE_ADDED);
        long dateModified = cursorWrapper.getLong(Playlists.DATE_MODIFIED);

        return new StoragePlayList(id,
                requireNonNull(name),
                new Date(dateAdded * 1000L),
                new Date(dateModified * 1000L));
    }
}
