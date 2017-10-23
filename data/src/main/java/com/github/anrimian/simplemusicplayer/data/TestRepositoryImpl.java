package com.github.anrimian.simplemusicplayer.data;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import android.util.Log;

import com.github.anrimian.simplemusicplayer.domain.TextRepository;

import io.reactivex.Single;

/**
 * Created on 18.10.2017.
 */

public class TestRepositoryImpl implements TextRepository {

    @Override
    public Single<String> getSomeData() {
        return Single.just("hey").map(o -> o + 1);
    }

    public static void parseAllAudio(Context context) {
        try {
            String TAG = "Audio";
            Cursor cur = context.getContentResolver().query(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null,
                    null);

            if (cur == null) {
                // Query failed...
                Log.e(TAG, "Failed to retrieve music: cursor is null :-(");

            }
            else if (!cur.moveToFirst()) {
                // Nothing to query. There is no music on the device. How boring.
                Log.e(TAG, "Failed to move cursor to first row (no query results).");

            }else {
                Log.i(TAG, "Listing...");
                // retrieve the indices of the columns where the ID, title, etc. of the song are

                // add each song to mItems
                do {
                    int artistColumn = cur.getColumnIndex(MediaStore.Audio.Media.ARTIST);
                    int titleColumn = cur.getColumnIndex(MediaStore.Audio.Media.TITLE);
                    int albumColumn = cur.getColumnIndex(MediaStore.Audio.Media.ALBUM);
                    int durationColumn = cur.getColumnIndex(MediaStore.Audio.Media.DURATION);
                    int idColumn = cur.getColumnIndex(MediaStore.Audio.Media._ID);
                    int filePathIndex = cur.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                    Log.i(TAG, "Title column index: " + String.valueOf(titleColumn));
                    Log.i(TAG, "ID column index: " + String.valueOf(titleColumn));

                    Log.i("Final ", "ID: " + cur.getString(idColumn) + " Title: " + cur.getString(titleColumn) + "Path: " + cur.getString(filePathIndex));
                    /*MediaFileInfo audio = new MediaFileInfo();
                    audio.setFileName(cur.getString(titleColumn));
                    audio.setFilePath(cur.getString(filePathIndex));
                    audio.setFileType(type);
                    mediaList.add(audio);*/

                } while (cur.moveToNext());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
