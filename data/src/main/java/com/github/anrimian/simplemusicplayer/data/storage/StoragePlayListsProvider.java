package com.github.anrimian.simplemusicplayer.data.storage;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import android.util.Log;

import com.github.anrimian.simplemusicplayer.data.utils.IOUtils;
import com.github.anrimian.simplemusicplayer.data.utils.db.CursorWrapper;
import com.github.anrimian.simplemusicplayer.domain.models.composition.Composition;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class StoragePlayListsProvider {

    private final ContentResolver contentResolver;
    private final StorageMusicProvider storageMusicProvider;

    public StoragePlayListsProvider(Context context, StorageMusicProvider storageMusicProvider) {
        contentResolver = context.getContentResolver();
        this.storageMusicProvider = storageMusicProvider;
    }

    public void getPlayLists() {
        Cursor cursor = null;
        try {
            cursor = contentResolver.query(
                    MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI,
                    null,
                    null,
                    null,
                    null);
            if (cursor == null) {
                return;
            }
            CursorWrapper cursorWrapper = new CursorWrapper(cursor);
//            Map<Long, Composition> compositions = new HashMap<>(cursor.getCount());

            List<Long> ids = new ArrayList<>();
            for (int i = 0; i < cursor.getCount(); i++) {
                cursor.moveToPosition(i);
                long id = getPlayListFromCursor(cursorWrapper);
                ids.add(id);
            }
            for (long id : ids) {

                //        MediaStore.Audio.Playlists.Members.AUDIO_ID //look at this
                Map<Long, Composition> compositions = storageMusicProvider.getCompositionsInPlayList(id);
                Log.d("KEK", "playlist: " + id);
                for (Composition composition: compositions.values()) {
                    Log.d("KEK", "composition: " + composition);
                }
            }

        } finally {
            IOUtils.closeSilently(cursor);
        }
    }

    private long getPlayListFromCursor(CursorWrapper cursorWrapper) {
        long id = cursorWrapper.getLong(MediaStore.Audio.Playlists._ID);
        String name = cursorWrapper.getString(MediaStore.Audio.Playlists.NAME);
        String data = cursorWrapper.getString(MediaStore.Audio.Playlists.DATA);
        String dateAdded = cursorWrapper.getString(MediaStore.Audio.Playlists.DATE_ADDED);
        String dateModified = cursorWrapper.getString(MediaStore.Audio.Playlists.DATE_MODIFIED);

        Log.d("KEK", "id: " + id);
        Log.d("KEK", "name: " + name);
        Log.d("KEK", "data: " + data);
        Log.d("KEK", "dateAdded: " + dateAdded);
        Log.d("KEK", "dateModified: " + dateModified);
        return id;
    }
}
