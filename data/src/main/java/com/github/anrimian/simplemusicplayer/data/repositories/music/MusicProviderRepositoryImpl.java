package com.github.anrimian.simplemusicplayer.data.repositories.music;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import android.util.Log;

import com.github.anrimian.simplemusicplayer.data.repositories.music.exceptions.MusicNotFoundException;
import com.github.anrimian.simplemusicplayer.data.utils.IOUtils;
import com.github.anrimian.simplemusicplayer.domain.Composition;
import com.github.anrimian.simplemusicplayer.domain.repositories.MusicProviderRepository;


import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import io.reactivex.Single;

import static android.provider.MediaStore.*;

/**
 * Created on 24.10.2017.
 */

public class MusicProviderRepositoryImpl implements MusicProviderRepository {

    private Context context;

    public MusicProviderRepositoryImpl(Context context) {
        this.context = context;
    }

    @Override
    public Single<List<Composition>> getAllCompositions() {
        return Single.create(emitter -> {
            Cursor cursor = null;
            try {
                cursor = context.getContentResolver().query(
                        Audio.Media.EXTERNAL_CONTENT_URI, null, null, null, null);
                if (cursor == null) {
                    emitter.onError(new MusicNotFoundException());
                    return;
                }
                List<Composition> compositions = new ArrayList<>(cursor.getCount());
                for (int i = 0; i < cursor.getCount(); i++) {
                    cursor.moveToPosition(i);

                    int artistColumn = cursor.getColumnIndex(Audio.Media.ARTIST);
                    int titleColumn = cursor.getColumnIndex(Audio.Media.TITLE);
                    int albumColumn = cursor.getColumnIndex(Audio.Media.ALBUM);
                    int durationColumn = cursor.getColumnIndex(Audio.Media.DURATION);
                    int idColumn = cursor.getColumnIndex(Audio.Media._ID);
                    int filePathColumn = cursor.getColumnIndex(Images.Media.DATA);
                    int sizeColumn = cursor.getColumnIndex(Images.Media.SIZE);
                    int countColumn = cursor.getColumnIndex(Audio.Media._COUNT);
                    int albumIdColumn = cursor.getColumnIndex(Audio.Media.ALBUM_ID);
                    int albumKeyColumn = cursor.getColumnIndex(Audio.Media.ALBUM_KEY);
                    int artistIdColumn = cursor.getColumnIndex(Audio.Media.ARTIST_ID);
                    int artistKeyColumn = cursor.getColumnIndex(Audio.Media.ARTIST_KEY);
                    int bookmarkColumn = cursor.getColumnIndex(Audio.Media.BOOKMARK);
                    int composerColumn = cursor.getColumnIndex(Audio.Media.COMPOSER);
                    int dateAddedColumn = cursor.getColumnIndex(Audio.Media.DATE_ADDED);
                    int dateModifiedColumn = cursor.getColumnIndex(Audio.Media.DATE_MODIFIED);
                    int displayNameColumn = cursor.getColumnIndex(Audio.Media.DISPLAY_NAME);
                    int isAlarmColumn = cursor.getColumnIndex(Audio.Media.IS_ALARM);
                    int isMusicColumn = cursor.getColumnIndex(Audio.Media.IS_MUSIC);
                    int isNotificationColumn = cursor.getColumnIndex(Audio.Media.IS_NOTIFICATION);
                    int isPodcastColumn = cursor.getColumnIndex(Audio.Media.IS_PODCAST);
                    int isRingtoneColumn = cursor.getColumnIndex(Audio.Media.IS_RINGTONE);
                    int mimeTypeColumn = cursor.getColumnIndex(Audio.Media.MIME_TYPE);
                    int titleKeyColumn = cursor.getColumnIndex(Audio.Media.TITLE_KEY);
                    int trackColumn = cursor.getColumnIndex(Audio.Media.TRACK);
                    int yearColumn = cursor.getColumnIndex(Audio.Media.YEAR);

                    String artist = cursor.getString(artistColumn);
                    String title = cursor.getString(titleColumn);
                    String album = cursor.getString(albumColumn);
                    String filePath = cursor.getString(filePathColumn);

                    long albumId = cursor.getLong(albumIdColumn);
                    String albumKey = cursor.getString(albumKeyColumn);
                    long artistId = cursor.getLong(artistIdColumn);
                    long bookmark = cursor.getLong(bookmarkColumn);
                    String composer = cursor.getString(composerColumn);
                    long dateAdded = cursor.getLong(dateAddedColumn);
                    long dateModified = cursor.getLong(dateModifiedColumn);
                    String displayName = cursor.getString(displayNameColumn);
                    boolean isAlarm = cursor.getInt(isAlarmColumn) == 1;
                    boolean isMusic = cursor.getInt(isMusicColumn) == 1;
                    boolean isNotification = cursor.getInt(isNotificationColumn) == 1;
                    boolean isPodcast = cursor.getInt(isPodcastColumn) == 1;
                    boolean isRingtone = cursor.getInt(isRingtoneColumn) == 1;
                    String mimeType = cursor.getString(mimeTypeColumn);
                    @Nullable int year = cursor.getInt(yearColumn);

                    long duration = cursor.getLong(durationColumn);
                    long size = cursor.getLong(sizeColumn);
                    long id = cursor.getLong(idColumn);

                    Log.d("MusicProviderRepository", "new composition----------");
                    Log.d("MusicProviderRepository", "artist: " + artist);
                    Log.d("MusicProviderRepository", "title: " + title);
                    Log.d("MusicProviderRepository", "album: " + album);
                    Log.d("MusicProviderRepository", "duration: " + duration);
                    Log.d("MusicProviderRepository", "id: " + id);
                    Log.d("MusicProviderRepository", "size: " + size);
                    Log.d("MusicProviderRepository", "filePath: " + filePath);

//                    Log.d("MusicProviderRepository", "count: " + count);
                    Log.d("MusicProviderRepository", "albumId: " + albumId);
                    Log.d("MusicProviderRepository", "albumKey: " + albumKey);
                    Log.d("MusicProviderRepository", "artistId: " + artistId);
//                    Log.d("MusicProviderRepository", "artistKey: " + artistKey);
                    Log.d("MusicProviderRepository", "bookmark: " + bookmark);
                    Log.d("MusicProviderRepository", "composer: " + composer);
                    Log.d("MusicProviderRepository", "dateAdded: " + dateAdded);
                    Log.d("MusicProviderRepository", "dateModified: " + dateModified);
                    Log.d("MusicProviderRepository", "displayName: " + displayName);
                    Log.d("MusicProviderRepository", "isAlarm: " + isAlarm);
                    Log.d("MusicProviderRepository", "isMusic: " + isMusic);
                    Log.d("MusicProviderRepository", "isNotification: " + isNotification);
                    Log.d("MusicProviderRepository", "isPodcast: " + isPodcast);
                    Log.d("MusicProviderRepository", "isRingtone: " + isRingtone);
                    Log.d("MusicProviderRepository", "mimeType: " + mimeType);
//                    Log.d("MusicProviderRepository", "titleKey: " + titleKey);
//                    Log.d("MusicProviderRepository", "track: " + track);
                    Log.d("MusicProviderRepository", "year: " + year);

                    Composition composition = new Composition();
                    //composition
                    compositions.add(composition);
                }
                Log.d("MusicProviderRepository", "compositions:" + compositions.size());
                emitter.onSuccess(compositions);
            } finally {
                IOUtils.closeSilently(cursor);
            }
        });
    }
}
