package com.github.anrimian.simplemusicplayer.data.storage;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio.Playlists;
import android.util.Log;

import com.github.anrimian.simplemusicplayer.data.utils.IOUtils;
import com.github.anrimian.simplemusicplayer.data.utils.db.CursorWrapper;
import com.github.anrimian.simplemusicplayer.data.utils.rx.content_observer.RxContentObserver;
import com.github.anrimian.simplemusicplayer.domain.models.composition.Composition;
import com.github.anrimian.simplemusicplayer.domain.models.playlist.PlayList;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import io.reactivex.Observable;

import static com.github.anrimian.simplemusicplayer.data.utils.Objects.requireNonNull;
import static java.util.Collections.emptyList;

public class StoragePlayListsProvider {

    private final ContentResolver contentResolver;
    private final StorageMusicProvider storageMusicProvider;

    public StoragePlayListsProvider(Context context, StorageMusicProvider storageMusicProvider) {
        contentResolver = context.getContentResolver();
        this.storageMusicProvider = storageMusicProvider;
    }

    public Observable<List<PlayList>> getChangeObservable() {
        return RxContentObserver.getObservable(contentResolver, Playlists.EXTERNAL_CONTENT_URI)
                .map(o -> getPlayLists());
    }

    public List<PlayList> getPlayLists() {
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
//            Map<Long, Composition> compositions = new HashMap<>(cursor.getCount());

            List<PlayList> playLists = new ArrayList<>();
            for (int i = 0; i < cursor.getCount(); i++) {
                cursor.moveToPosition(i);
                playLists.add(getPlayListFromCursor(cursorWrapper));
            }
            return playLists;
//            for (long id : ids) {
//
//                //        MediaStore.Audio.Playlists.Members.AUDIO_ID //look at this
//                Map<Long, Composition> compositions = storageMusicProvider.getCompositionsInPlayList(id);
//                Log.d("KEK", "playlist: " + id);
//                for (Composition composition: compositions.values()) {
//                    Log.d("KEK", "composition: " + composition);
//                }
//            }

        } finally {
            IOUtils.closeSilently(cursor);
        }
    }

    private PlayList getPlayListFromCursor(CursorWrapper cursorWrapper) {
        long id = cursorWrapper.getLong(Playlists._ID);
        String name = cursorWrapper.getString(Playlists.NAME);
        long dateAdded = cursorWrapper.getLong(Playlists.DATE_ADDED);
        long dateModified = cursorWrapper.getLong(Playlists.DATE_MODIFIED);

        return new PlayList(id,
                requireNonNull(name),
                new Date(dateAdded * 1000L),
                new Date(dateModified * 1000L));
    }
}
