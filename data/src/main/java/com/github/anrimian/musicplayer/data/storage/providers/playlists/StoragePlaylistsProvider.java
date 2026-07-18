package com.github.anrimian.musicplayer.data.storage.providers.playlists;

import static android.provider.BaseColumns._ID;
import static android.provider.MediaStore.Audio.Playlists.Members.AUDIO_ID;
import static android.provider.MediaStore.Audio.Playlists.Members.getContentUri;
import static android.text.TextUtils.isEmpty;
import static java.util.Collections.emptyList;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore.Audio.Playlists;

import com.github.anrimian.musicplayer.data.models.exceptions.PlayListNotCreatedException;
import com.github.anrimian.musicplayer.data.storage.exceptions.UnavailableMediaStoreException;
import com.github.anrimian.musicplayer.data.storage.providers.MediaStoreUtils;
import com.github.anrimian.musicplayer.data.utils.db.CursorWrapper;
import com.github.anrimian.musicplayer.data.utils.rx.content_observer.RxContentObserver;
import com.github.anrimian.musicplayer.domain.interactors.playlists.validators.PlaylistFileNameValidator;
import com.github.anrimian.musicplayer.domain.models.composition.CompositionModel;
import com.github.anrimian.musicplayer.domain.utils.rx.FastDebounceFilter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;

public class StoragePlaylistsProvider {

    private final Context context;
    private final ContentResolver contentResolver;

    public StoragePlaylistsProvider(Context context) {
        this.context = context;
        contentResolver = context.getContentResolver();
    }

    public Observable<Map<String, StoragePlaylist>> getPlayListsObservable() {
        return RxContentObserver.getObservable(contentResolver, Playlists.EXTERNAL_CONTENT_URI)
                .flatMapSingle(o -> Single.create(emitter -> {
                    Map<String, StoragePlaylist> playLists = getPlayLists();
                    if (playLists != null) {
                        emitter.onSuccess(playLists);
                    }
                }));
    }

    @Nullable
    public Map<String, StoragePlaylist> getPlayLists() {
        try {
            MediaStoreUtils.checkIfMediaStoreAvailable(context);
        } catch (UnavailableMediaStoreException e) {
            return null;
        }

        try(Cursor cursor = MediaStoreUtils.query(contentResolver,
                Playlists.EXTERNAL_CONTENT_URI,
                null,
                null,
                null,
                null)) {
            if (cursor == null) {
                return new HashMap<>();
            }
            CursorWrapper cursorWrapper = new CursorWrapper(cursor);

            Map<String, StoragePlaylist> map = new HashMap<>();
            while (cursor.moveToNext()) {
                StoragePlaylist playList = getPlayListFromCursor(cursorWrapper);
                if (playList != null) {
                    map.put(playList.getName(), playList);
                }
            }
            return map;
        }
    }

    public Long createPlayList(String name, long timeAdded, long timeModified) {
        MediaStoreUtils.checkIfMediaStoreAvailable(context);

        ContentValues contentValues = new ContentValues();
        contentValues.put(Playlists.NAME, name);
        contentValues.put(Playlists.DATE_ADDED, timeAdded / 1000L);
        contentValues.put(Playlists.DATE_MODIFIED, timeModified / 1000L);
        Uri uri = contentResolver.insert(Playlists.EXTERNAL_CONTENT_URI, contentValues);
        if (uri == null || isEmpty(uri.getLastPathSegment())) {
            return null;
        }
        long id = Long.parseLong(uri.getLastPathSegment());
        StoragePlaylist playList = findPlayList(id);
        if (playList == null) {
            return null;
        }
        return playList.getStorageId();
    }

    public StoragePlaylist createPlayList(String name) {
        MediaStoreUtils.checkIfMediaStoreAvailable(context);

        ContentValues contentValues = new ContentValues();
        contentValues.put(Playlists.NAME, name);
        contentValues.put(Playlists.DATE_MODIFIED, System.currentTimeMillis() / 1000L);
        Uri uri = contentResolver.insert(Playlists.EXTERNAL_CONTENT_URI, contentValues);
        if (uri == null || isEmpty(uri.getLastPathSegment())) {
            throw new PlayListNotCreatedException();
        }
        long id = Long.parseLong(uri.getLastPathSegment());
        StoragePlaylist playList = findPlayList(id);
        if (playList == null) {
            throw new PlayListNotCreatedException();
        }
        return playList;
    }

    public void deletePlayList(long id) {
        MediaStoreUtils.checkIfMediaStoreAvailable(context);

        contentResolver.delete(Playlists.EXTERNAL_CONTENT_URI,
                Playlists._ID + " = ?",
                new String[] { String.valueOf(id) });
    }

    public Observable<List<StoragePlaylistItem>> getPlayListEntriesObservable(long playListId) {
        return RxContentObserver.getObservable(contentResolver, getContentUri("external", playListId))
                .debounce(new FastDebounceFilter<>())
                .map(o -> getPlayListItems(playListId));
    }

    public List<StoragePlaylistItem> getPlayListItems(long playListId) {
        try(Cursor cursor = contentResolver.query(
                getContentUri("external", playListId),
                new String[] { AUDIO_ID, _ID },
                null,
                null,
                Playlists.Members.PLAY_ORDER)) {
            if (cursor == null) {
                return emptyList();
            }
            CursorWrapper cursorWrapper = new CursorWrapper(cursor);
            List<StoragePlaylistItem> items = new ArrayList<>(cursor.getCount());
            while (cursor.moveToNext()) {
                StoragePlaylistItem item = getPlayListItemFromCursor(cursorWrapper);
                items.add(item);
            }
            return items;
        }
    }

    public void addCompositionToPlayList(long compositionId, long playListId, int position) {
        ContentValues values = new ContentValues();
        values.put(Playlists.Members.PLAY_ORDER, position);
        values.put(Playlists.Members.AUDIO_ID, compositionId);
        values.put(Playlists.Members.PLAYLIST_ID, playListId);

        contentResolver.insert(getContentUri("external", playListId), values);
        updateModifyTime(playListId);
    }

    public void addCompositionsToPlayList(List<CompositionModel> compositions, long playListId, int startPosition) {
        int position = startPosition;
        ContentValues[] valuesList = new ContentValues[compositions.size()];
        for (int i = 0; i < compositions.size(); i++) {
            Long compositionId = compositions.get(i).getStorageId();
            if (compositionId == null) {
                continue;
            }
            ContentValues values = new ContentValues();
            values.put(Playlists.Members.PLAY_ORDER, position);
            values.put(Playlists.Members.AUDIO_ID, compositionId);
            values.put(Playlists.Members.PLAYLIST_ID, playListId);
            valuesList[i] = values;
            position++;
        }

        try {
            contentResolver.bulkInsert(
                    getContentUri("external", playListId),
                    valuesList
            );
            updateModifyTime(playListId);
        } catch (SecurityException ignored) {}
    }

    public void deleteItemFromPlayList(long itemId, long playListId) {
        contentResolver.delete(
                getContentUri("external", playListId),
                Playlists.Members._ID + " = ?",
                new String[] { String.valueOf(itemId) }
        );
    }

    public void moveItemInPlayList(long playListId, int from, int to) {
        Playlists.Members.moveItem(contentResolver, playListId, from, to);
    }

    public void updatePlayListName(long playListId, String name) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            //unsupported
            return;
        }
        MediaStoreUtils.checkIfMediaStoreAvailable(context);

        ContentValues contentValues = new ContentValues();
        contentValues.put(Playlists.NAME, name);
        contentResolver.update(Playlists.EXTERNAL_CONTENT_URI,
                contentValues,
                Playlists._ID + " = ?",
                new String[] { String.valueOf(playListId) });
    }

    private void updateModifyTime(long playListId) {
        MediaStoreUtils.checkIfMediaStoreAvailable(context);

        ContentValues playListValues = new ContentValues();
        playListValues.put(Playlists.DATE_MODIFIED, System.currentTimeMillis() / 1000L);
        contentResolver.update(Playlists.EXTERNAL_CONTENT_URI,
                playListValues,
                Playlists._ID + " = ?",
                new String[] { String.valueOf(playListId) });
    }

    @Nullable
    private StoragePlaylist findPlayList(long id) {
        try(Cursor cursor = contentResolver.query(
                Playlists.EXTERNAL_CONTENT_URI,
                null,
                Playlists._ID + " = ?",
                new String[] { String.valueOf(id) },
                Playlists.DATE_ADDED + " DESC")) {
            if (cursor != null && cursor.moveToFirst()) {
                CursorWrapper cursorWrapper = new CursorWrapper(cursor);
                return getPlayListFromCursor(cursorWrapper);
            }
            return null;
        }
    }

    private StoragePlaylistItem getPlayListItemFromCursor(CursorWrapper cursorWrapper) {
        long itemId = cursorWrapper.getLong(Playlists.Members._ID);
        long audioId = cursorWrapper.getLong(Playlists.Members.AUDIO_ID);
        return new StoragePlaylistItem(itemId, audioId);
    }

    @Nullable
    private StoragePlaylist getPlayListFromCursor(CursorWrapper cursorWrapper) {
        long id = cursorWrapper.getLong(Playlists._ID);
        String name = cursorWrapper.getString(Playlists.NAME);
        if (name == null) {
            return null;
        }
        name = PlaylistFileNameValidator.INSTANCE.getFormattedPlaylistName(name);

        long dateAdded = cursorWrapper.getLong(Playlists.DATE_ADDED);
        long dateModified = cursorWrapper.getLong(Playlists.DATE_MODIFIED);

        return new StoragePlaylist(id,
                name,
                dateAdded * 1000L,
                dateModified * 1000L);
    }
}
