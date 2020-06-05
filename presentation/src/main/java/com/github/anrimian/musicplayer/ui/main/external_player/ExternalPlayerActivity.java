package com.github.anrimian.musicplayer.ui.main.external_player;

import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;

import androidx.annotation.Nullable;

import com.github.anrimian.musicplayer.R;
import com.github.anrimian.musicplayer.data.models.composition.source.UriCompositionSource;
import com.github.anrimian.musicplayer.data.utils.db.CursorWrapper;
import com.github.anrimian.musicplayer.di.Components;

import moxy.MvpAppCompatActivity;
import moxy.presenter.InjectPresenter;
import moxy.presenter.ProvidePresenter;

public class ExternalPlayerActivity extends MvpAppCompatActivity implements ExternalPlayerView {

    @InjectPresenter
    ExternalPlayerPresenter presenter;

    @ProvidePresenter
    ExternalPlayerPresenter providePresenter() {
        Uri uriToPlay = getIntent().getData();
        String[] query = new String[] {
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.DISPLAY_NAME,
//                            Media.ALBUM,
//                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.SIZE,
//                MediaStore.Audio.Media._ID,
//                            Media.ARTIST_ID,
//                MediaStore.Audio.Media.ALBUM_ID,
//                MediaStore.Audio.Media.DATE_ADDED,
//                MediaStore.Audio.Media.DATE_MODIFIED
        };
//            }

        try(Cursor cursor = getContentResolver().query(
                uriToPlay,
                query,
                null,
                null,
                null)) {
            if (cursor == null || cursor.getCount() == 0) {
                return null;
            }

            CursorWrapper cursorWrapper = new CursorWrapper(cursor);
            if (cursor.moveToFirst()) {
                String artist = cursorWrapper.getString(MediaStore.Audio.Media.ARTIST);
                Log.d("KEK", "artist: " + artist);

                String title = cursorWrapper.getString(MediaStore.Audio.Media.TITLE);
                Log.d("KEK", "title: " + title);

                String displayName = cursorWrapper.getString(MediaStore.Audio.Media.DISPLAY_NAME);
                Log.d("KEK", "displayName: " + displayName);

                long duration = cursorWrapper.getLong(MediaStore.Audio.Media.DURATION);
                Log.d("KEK", "duration: " + duration);

                long size = cursorWrapper.getLong(MediaStore.Audio.Media.SIZE);
                Log.d("KEK", "size: " + size);
            }
        }

//        MediaMetadataRetriever..METADATA_KEY_DURATION

        MediaMetadataRetriever mmr = null;
        try {
            mmr = new MediaMetadataRetriever();
            mmr.setDataSource(this, uriToPlay);

            String mmrArtist = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
            Log.d("KEK", "mmrArtist: " + mmrArtist);
            String mmrTitle = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
            Log.d("KEK", "mmrTitle: " + mmrTitle);
            String mmrDuration = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            Log.d("KEK", "mmrDuration: " + mmrDuration);


        } catch (Exception ignored) {
            return null;
        } finally {
            if (mmr != null) {
                mmr.release();
            }
        }


        UriCompositionSource source = new UriCompositionSource(uriToPlay);
        return Components.getExternalPlayerComponent(source).externalPlayerPresenter();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Components.getAppComponent().themeController().applyCurrentTheme(this);
        getTheme().applyStyle(R.style.DialogActivityTheme, true);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_external_player);


//        finish();
    }
}
