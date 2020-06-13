package com.github.anrimian.musicplayer.ui.main.external_player;

import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;

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
        UriCompositionSource source = createCompositionSource(uriToPlay);
        return Components.getExternalPlayerComponent(source).externalPlayerPresenter();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Components.getAppComponent().themeController().applyCurrentTheme(this);
        getTheme().applyStyle(R.style.DialogActivityTheme, true);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_external_player);
    }

    //async creation?
    private UriCompositionSource createCompositionSource(Uri uri) {
        String displayName = null;
        String title = null;
        String artist = null;
        String album = null;
        long duration = 0;
        long size = 0;
        byte[] imageBytes = null;

        try (Cursor cursor = getContentResolver().query(
                uri,
                new String[] {
                        MediaStore.Audio.Media.DISPLAY_NAME,
                        MediaStore.Audio.Media.SIZE
                },
                null,
                null,
                null)) {
            if (cursor == null || cursor.getCount() == 0) {
                return null;
            }

            CursorWrapper cursorWrapper = new CursorWrapper(cursor);
            if (cursor.moveToFirst()) {
                displayName = cursorWrapper.getString(MediaStore.Audio.Media.DISPLAY_NAME);
                size = cursorWrapper.getLong(MediaStore.Audio.Media.SIZE);
            }
        }

        MediaMetadataRetriever mmr = null;
        try {
            mmr = new MediaMetadataRetriever();
            mmr.setDataSource(this, uri);

            artist = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
            title = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
            album = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
            String durationStr = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            //we can't read image from uri later, read as first as possible
            imageBytes = mmr.getEmbeddedPicture();
            try {
                duration = Long.parseLong(durationStr);
            } catch (NumberFormatException ignored) {}


        } catch (Exception ignored) {

        } finally {
            if (mmr != null) {
                mmr.release();
            }
        }

        return new UriCompositionSource(uri,
                displayName == null? "unknown name" : displayName,
                title,
                artist,
                album,
                duration,
                size,
                imageBytes);
    }
}
