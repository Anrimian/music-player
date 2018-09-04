package com.github.anrimian.simplemusicplayer.data.storage.providers.playlists;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore.Audio.Playlists;

import com.github.anrimian.simplemusicplayer.data.models.StoragePlayList;
import com.github.anrimian.simplemusicplayer.data.models.exceptions.CompositionNotDeletedException;
import com.github.anrimian.simplemusicplayer.data.models.exceptions.CompositionNotMovedException;
import com.github.anrimian.simplemusicplayer.data.models.exceptions.PlayListNotCreatedException;
import com.github.anrimian.simplemusicplayer.data.models.exceptions.PlayListNotDeletedException;
import com.github.anrimian.simplemusicplayer.data.models.exceptions.PlayListNotModifiedException;
import com.github.anrimian.simplemusicplayer.data.storage.providers.music.StorageMusicProvider;
import com.github.anrimian.simplemusicplayer.data.utils.IOUtils;
import com.github.anrimian.simplemusicplayer.data.utils.db.CursorWrapper;
import com.github.anrimian.simplemusicplayer.data.utils.rx.content_observer.RxContentObserver;
import com.github.anrimian.simplemusicplayer.domain.models.composition.Composition;
import com.github.anrimian.simplemusicplayer.domain.models.playlist.PlayList;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.Nullable;

import io.reactivex.Observable;

import static android.provider.MediaStore.Audio.Playlists.Members.getContentUri;
import static com.github.anrimian.simplemusicplayer.domain.utils.Objects.requireNonNull;
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
                    Playlists.DATE_ADDED + " DESC");
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

    public void createPlayList(String name) {//TODO also return new playlist
        ContentValues contentValues = new ContentValues();
        contentValues.put(Playlists.NAME, name);
        Uri uri = contentResolver.insert(Playlists.EXTERNAL_CONTENT_URI, contentValues);
        if (uri == null) {
            throw new PlayListNotCreatedException();
        }
    }

    public void deletePlayList(long id) {
        int deletedRows = contentResolver.delete(Playlists.EXTERNAL_CONTENT_URI,
                Playlists._ID + " = ?",
                new String[] { String.valueOf(id) });

        if (deletedRows == 0) {
            throw new PlayListNotDeletedException();
        }
    }

    public Observable<List<Composition>> getPlayListChangeObservable(long playListId) {
        return RxContentObserver.getObservable(contentResolver, getContentUri("external", playListId))
                .map(o -> getCompositions(playListId));
    }

    public List<Composition> getCompositions(long playListId) {
        Cursor cursor = null;
        try {
            cursor = contentResolver.query(
                    getContentUri("external", playListId),
                    null,
                    null,
                    null,
                    Playlists.Members.PLAY_ORDER);
            if (cursor == null) {
                return emptyList();
            }
            CursorWrapper cursorWrapper = new CursorWrapper(cursor);
            List<Composition> compositions = new ArrayList<>(cursor.getCount());
            for (int i = 0; i < cursor.getCount(); i++) {
                cursor.moveToPosition(i);

                Composition composition = getCompositionFromCursor(cursorWrapper);
                checkCorruptedComposition(composition);
                compositions.add(composition);
            }
            return compositions;
        } finally {
            IOUtils.closeSilently(cursor);
        }
    }

    public void addCompositionInPlayList(long compositionId, long playListId, int position) {
        ContentValues values = new ContentValues();
        values.put(Playlists.Members.PLAY_ORDER, position);
        values.put(Playlists.Members.AUDIO_ID, compositionId);
        values.put(Playlists.Members.PLAYLIST_ID, playListId);

        Uri uri = contentResolver.insert(getContentUri("external", playListId), values);
        if (uri == null) {
            throw new PlayListNotModifiedException();
        }
    }

    public void deleteCompositionFromPlayList(long compositionId, long playListId) {
        int deletedRows = contentResolver.delete(
                getContentUri("external", playListId),
                Playlists.Members.AUDIO_ID + " = ?",
                new String[] { String.valueOf(compositionId) }
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

    private Composition getCompositionFromCursor(CursorWrapper cursorWrapper) {
        String artist = cursorWrapper.getString(Playlists.Members.ARTIST);
        String title = cursorWrapper.getString(Playlists.Members.TITLE);
        String album = cursorWrapper.getString(Playlists.Members.ALBUM);
        String filePath = cursorWrapper.getString(Playlists.Members.DATA);
        String albumKey = cursorWrapper.getString(Playlists.Members.ALBUM_KEY);
        String composer = cursorWrapper.getString(Playlists.Members.COMPOSER);
        String displayName = cursorWrapper.getString(Playlists.Members.DISPLAY_NAME);
        String mimeType = cursorWrapper.getString(Playlists.Members.MIME_TYPE);

        long duration = cursorWrapper.getLong(Playlists.Members.DURATION);
        long size = cursorWrapper.getLong(Playlists.Members.SIZE);
        long id = cursorWrapper.getLong(Playlists.Members.AUDIO_ID);
        long artistId = cursorWrapper.getLong(Playlists.Members.ARTIST_ID);
        long bookmark = cursorWrapper.getLong(Playlists.Members.BOOKMARK);
        long albumId = cursorWrapper.getLong(Playlists.Members.ALBUM_ID);
        long dateAdded = cursorWrapper.getLong(Playlists.Members.DATE_ADDED);
        long dateModified = cursorWrapper.getLong(Playlists.Members.DATE_MODIFIED);

        boolean isAlarm = cursorWrapper.getBoolean(Playlists.Members.IS_ALARM);
        boolean isMusic = cursorWrapper.getBoolean(Playlists.Members.IS_MUSIC);
        boolean isNotification = cursorWrapper.getBoolean(Playlists.Members.IS_NOTIFICATION);
        boolean isPodcast = cursorWrapper.getBoolean(Playlists.Members.IS_PODCAST);
        boolean isRingtone = cursorWrapper.getBoolean(Playlists.Members.IS_RINGTONE);

        @Nullable Integer year = cursorWrapper.getInt(Playlists.Members.YEAR);

        if (artist.equals("<unknown>")) {
            artist = null;
        }

        Composition composition = new Composition();
        //composition
        composition.setArtist(artist);
        composition.setTitle(title);
        composition.setAlbum(album);
        composition.setFilePath(filePath);
        composition.setComposer(composer);
        composition.setDisplayName(displayName);

        composition.setDuration(duration);
        composition.setSize(size);
        composition.setId(id);
        composition.setDateAdded(new Date(dateAdded * 1000L));
        composition.setDateModified(new Date(dateModified * 1000L));

        composition.setAlarm(isAlarm);
        composition.setMusic(isMusic);
        composition.setNotification(isNotification);
        composition.setPodcast(isPodcast);
        composition.setRingtone(isRingtone);

        composition.setYear(year);
        return composition;
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
