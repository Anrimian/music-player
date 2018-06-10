package com.github.anrimian.simplemusicplayer.data.storage;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;

import com.github.anrimian.simplemusicplayer.data.models.exceptions.DeleteFileException;
import com.github.anrimian.simplemusicplayer.data.repositories.music.exceptions.MusicNotFoundException;
import com.github.anrimian.simplemusicplayer.data.utils.IOUtils;
import com.github.anrimian.simplemusicplayer.data.utils.db.CursorWrapper;
import com.github.anrimian.simplemusicplayer.data.utils.rx.content_observer.RxContentObserver;
import com.github.anrimian.simplemusicplayer.domain.models.composition.Composition;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;

import io.reactivex.Observable;

public class StorageMusicProvider {

    private static final int CHANGE_EVENTS_WINDOW_SECONDS = 5;

    private final Context context;

    public StorageMusicProvider(Context context) {
        this.context = context;
    }

    public Observable<Map<Long, Composition>> getChangeObservable() {
        return RxContentObserver.getObservable(context, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI)
                .throttleFirst(CHANGE_EVENTS_WINDOW_SECONDS, TimeUnit.SECONDS)//TODO not this
                .map(o -> getCompositions());
    }

    public Map<Long, Composition> getCompositions() {
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    null,
                    null,
                    null,
                    null);
            if (cursor == null) {
                throw new MusicNotFoundException();
            }
            CursorWrapper cursorWrapper = new CursorWrapper(cursor);
            Map<Long, Composition> compositions = new HashMap<>(cursor.getCount());
            for (int i = 0; i < cursor.getCount(); i++) {
                cursor.moveToPosition(i);

                String artist = cursorWrapper.getString(MediaStore.Audio.Media.ARTIST);
                String title = cursorWrapper.getString(MediaStore.Audio.Media.TITLE);
                String album = cursorWrapper.getString(MediaStore.Audio.Media.ALBUM);
                String filePath = cursorWrapper.getString(MediaStore.Images.Media.DATA);
                String albumKey = cursorWrapper.getString(MediaStore.Audio.Media.ALBUM_KEY);
                String composer = cursorWrapper.getString(MediaStore.Audio.Media.COMPOSER);
                String displayName = cursorWrapper.getString(MediaStore.Audio.Media.DISPLAY_NAME);
                String mimeType = cursorWrapper.getString(MediaStore.Audio.Media.MIME_TYPE);

                long duration = cursorWrapper.getLong(MediaStore.Audio.Media.DURATION);
                long size = cursorWrapper.getLong(MediaStore.Images.Media.SIZE);
                long id = cursorWrapper.getLong(MediaStore.Audio.Media._ID);
                long artistId = cursorWrapper.getLong(MediaStore.Audio.Media.ARTIST_ID);
                long bookmark = cursorWrapper.getLong(MediaStore.Audio.Media.BOOKMARK);
                long albumId = cursorWrapper.getLong(MediaStore.Audio.Media.ALBUM_ID);
                long dateAdded = cursorWrapper.getLong(MediaStore.Audio.Media.DATE_ADDED);
                long dateModified = cursorWrapper.getLong(MediaStore.Audio.Media.DATE_MODIFIED);

                boolean isAlarm = cursorWrapper.getBoolean(MediaStore.Audio.Media.IS_ALARM);
                boolean isMusic = cursorWrapper.getBoolean(MediaStore.Audio.Media.IS_MUSIC);
                boolean isNotification = cursorWrapper.getBoolean(MediaStore.Audio.Media.IS_NOTIFICATION);
                boolean isPodcast = cursorWrapper.getBoolean(MediaStore.Audio.Media.IS_PODCAST);
                boolean isRingtone = cursorWrapper.getBoolean(MediaStore.Audio.Media.IS_RINGTONE);

                @Nullable Integer year = cursorWrapper.getInt(MediaStore.Audio.Media.YEAR);

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
                compositions.put(id, composition);
            }
            return compositions;
        } finally {
            IOUtils.closeSilently(cursor);
        }
    }

    public void deleteComposition(String path) {//TODO update media store and not become update
        File file = new File(path);
        boolean deleted = file.delete();
        if (!deleted) {
            throw new DeleteFileException("can not delete file: " + path);
        }
    }
}
