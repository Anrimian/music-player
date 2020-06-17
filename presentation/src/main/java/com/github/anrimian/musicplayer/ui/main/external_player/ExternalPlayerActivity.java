package com.github.anrimian.musicplayer.ui.main.external_player;

import android.content.Intent;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;

import androidx.annotation.Nullable;

import com.github.anrimian.musicplayer.R;
import com.github.anrimian.musicplayer.data.models.composition.source.UriCompositionSource;
import com.github.anrimian.musicplayer.data.utils.db.CursorWrapper;
import com.github.anrimian.musicplayer.databinding.ActivityExternalPlayerBinding;
import com.github.anrimian.musicplayer.di.Components;
import com.github.anrimian.musicplayer.ui.common.format.FormatUtils;
import com.github.anrimian.musicplayer.ui.utils.AndroidUtils;
import com.github.anrimian.musicplayer.ui.utils.views.seek_bar.SeekBarViewWrapper;

import moxy.MvpAppCompatActivity;
import moxy.presenter.InjectPresenter;
import moxy.presenter.ProvidePresenter;

import static com.github.anrimian.musicplayer.domain.models.utils.CompositionHelper.formatCompositionName;
import static com.github.anrimian.musicplayer.ui.common.format.FormatUtils.formatMilliseconds;

public class ExternalPlayerActivity extends MvpAppCompatActivity implements ExternalPlayerView {

    @InjectPresenter
    ExternalPlayerPresenter presenter;

    private ActivityExternalPlayerBinding viewBinding;

    private SeekBarViewWrapper seekBarViewWrapper;

    @ProvidePresenter
    ExternalPlayerPresenter providePresenter() {
        Uri uriToPlay = getIntent().getData();
        UriCompositionSource source = createCompositionSource(uriToPlay);
        ExternalPlayerPresenter presenter = Components.getExternalPlayerComponent().externalPlayerPresenter();
        presenter.onSourceForPlayingReceived(source);
        return presenter;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Components.getAppComponent().themeController().applyCurrentTheme(this);
        getTheme().applyStyle(R.style.DialogActivityTheme, true);
        super.onCreate(savedInstanceState);
        viewBinding = ActivityExternalPlayerBinding.inflate(getLayoutInflater());
        setContentView(viewBinding.getRoot());

        seekBarViewWrapper = new SeekBarViewWrapper(viewBinding.sbTrackState);
        seekBarViewWrapper.setProgressChangeListener(presenter::onTrackRewoundTo);
        seekBarViewWrapper.setOnSeekStartListener(presenter::onSeekStart);
        seekBarViewWrapper.setOnSeekStopListener(presenter::onSeekStop);

        viewBinding.ivPlayPause.setOnClickListener(v -> presenter.onPlayPauseClicked());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        Uri uriToPlay = intent.getData();
        UriCompositionSource source = createCompositionSource(uriToPlay);
        presenter.onSourceForPlayingReceived(source);
    }

    @Override
    public void displayComposition(UriCompositionSource source) {
        viewBinding.tvComposition.setText(formatCompositionName(source.getTitle(), source.getDisplayName()));
        viewBinding.tvCompositionAuthor.setText(FormatUtils.formatAuthor(source.getArtist(), this));
        seekBarViewWrapper.setMax(source.getDuration());
        viewBinding.tvTotalTime.setText(formatMilliseconds(source.getDuration()));
        Components.getAppComponent()
                .imageLoader()
                .displayImageInReusableTarget(viewBinding.ivMusicIcon, source, R.drawable.ic_music_placeholder);
    }

    @Override
    public void showStopState() {
        AndroidUtils.setAnimatedVectorDrawable(viewBinding.ivPlayPause, R.drawable.anim_pause_to_play);
        viewBinding.ivPlayPause.setContentDescription(getString(R.string.play));
    }

    @Override
    public void showPlayState() {
        AndroidUtils.setAnimatedVectorDrawable(viewBinding.ivPlayPause, R.drawable.anim_play_to_pause);
        viewBinding.ivPlayPause.setContentDescription(getString(R.string.pause));
    }

    @Override
    public void showTrackState(long currentPosition, long duration) {
        seekBarViewWrapper.setProgress(currentPosition);
        String formattedTime = formatMilliseconds(currentPosition);
        viewBinding.sbTrackState.setContentDescription(getString(R.string.position_template, formattedTime));

        viewBinding.tvPlayedTime.setText(formattedTime);
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
