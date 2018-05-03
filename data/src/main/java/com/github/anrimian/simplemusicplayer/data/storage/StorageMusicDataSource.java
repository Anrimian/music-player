package com.github.anrimian.simplemusicplayer.data.storage;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import com.github.anrimian.simplemusicplayer.data.repositories.music.exceptions.MusicNotFoundException;
import com.github.anrimian.simplemusicplayer.data.utils.IOUtils;
import com.github.anrimian.simplemusicplayer.data.utils.db.CursorWrapper;
import com.github.anrimian.simplemusicplayer.data.utils.rx.content_observer.ContentObserverDisposable;
import com.github.anrimian.simplemusicplayer.domain.models.composition.Composition;
import com.github.anrimian.simplemusicplayer.domain.utils.changes.Change;
import com.github.anrimian.simplemusicplayer.domain.utils.changes.ChangeableList;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.Nullable;

import io.reactivex.Emitter;
import io.reactivex.Observable;
import io.reactivex.Single;


public class StorageMusicDataSource {

    private Context context;

    public StorageMusicDataSource(Context context) {
        this.context = context;
    }

    public Single<List<Composition>> getAllCompositions() {
        return Single.just(getCompositionsFromProvider());
    }

    public Single<ChangeableList<Composition>> getCompositions() {//TODO cache list
        return Single.fromCallable(() -> {
            List<Composition> compositions = getCompositionsFromProvider();
            return new ChangeableList<>(compositions, getCompositionChangeObservable().share());
        });
    }

    private Observable<Change<Composition>> getCompositionChangeObservable() {
        return Observable.create(emitter -> {
            ContentObserver contentObserver = new CompositionsContentObserver(emitter);
            ContentResolver contentResolver = context.getContentResolver();
            contentResolver.registerContentObserver(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                            false,
                            contentObserver);
            emitter.setDisposable(new ContentObserverDisposable(contentObserver, contentResolver));
        });
    }

    private List<Composition> getCompositionsFromProvider() {
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
            List<Composition> compositions = new ArrayList<>(cursor.getCount());
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
                compositions.add(composition);
            }
            return compositions;
        } finally {
            IOUtils.closeSilently(cursor);
        }
    }

    private class CompositionsContentObserver extends ContentObserver {

        private Emitter<Change<Composition>> changeEmitter;

        CompositionsContentObserver(Emitter<Change<Composition>> changeEmitter) {
            super(null);
            this.changeEmitter = changeEmitter;
        }

        @Override
        public boolean deliverSelfNotifications() {
            return true;
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            Log.d("KEK", "onChange: " + selfChange);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            super.onChange(selfChange, uri);
            Log.d("KEK", "onChange: " + selfChange + ", uri: " + uri);
        }

    }
}
