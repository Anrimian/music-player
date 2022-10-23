package com.github.anrimian.musicplayer.ui.main.external_player;

import static android.view.View.VISIBLE;
import static com.github.anrimian.musicplayer.Constants.Arguments.LAUNCH_PREPARE_ARG;
import static com.github.anrimian.musicplayer.domain.models.utils.CompositionHelper.formatCompositionName;
import static com.github.anrimian.musicplayer.ui.common.format.FormatUtils.formatMilliseconds;
import static com.github.anrimian.musicplayer.ui.common.format.FormatUtils.getRepeatModeIcon;
import static com.github.anrimian.musicplayer.ui.common.format.FormatUtils.getRepeatModeText;
import static com.github.anrimian.musicplayer.ui.common.view.ViewUtils.setOnHoldListener;
import static com.github.anrimian.musicplayer.ui.utils.ViewUtils.onCheckChanged;
import static com.github.anrimian.musicplayer.ui.utils.ViewUtils.setChecked;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;

import com.github.anrimian.musicplayer.R;
import com.github.anrimian.musicplayer.data.models.composition.source.ExternalCompositionSource;
import com.github.anrimian.musicplayer.data.utils.db.CursorWrapper;
import com.github.anrimian.musicplayer.databinding.ActivityExternalPlayerBinding;
import com.github.anrimian.musicplayer.di.Components;
import com.github.anrimian.musicplayer.ui.common.compat.CompatUtils;
import com.github.anrimian.musicplayer.ui.common.dialogs.DialogUtils;
import com.github.anrimian.musicplayer.ui.common.error.ErrorCommand;
import com.github.anrimian.musicplayer.ui.common.format.FormatUtils;
import com.github.anrimian.musicplayer.ui.utils.AndroidUtils;
import com.github.anrimian.musicplayer.ui.utils.ImageUtils;
import com.github.anrimian.musicplayer.ui.utils.views.seek_bar.SeekBarViewWrapper;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import moxy.MvpAppCompatActivity;
import moxy.presenter.InjectPresenter;
import moxy.presenter.ProvidePresenter;

public class ExternalPlayerActivity extends MvpAppCompatActivity implements ExternalPlayerView {

    @InjectPresenter
    ExternalPlayerPresenter presenter;

    private ActivityExternalPlayerBinding viewBinding;

    private SeekBarViewWrapper seekBarViewWrapper;

    @Nullable
    private Disposable sourceCreationDisposable;

    @ProvidePresenter
    ExternalPlayerPresenter providePresenter() {
        return Components.getExternalPlayerComponent().externalPlayerPresenter();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Components.getAppComponent().themeController().applyCurrentTheme(this);
        getTheme().applyStyle(R.style.DialogActivityTheme, true);
        super.onCreate(savedInstanceState);
        viewBinding = ActivityExternalPlayerBinding.inflate(getLayoutInflater());
        setContentView(viewBinding.getRoot());

        CompatUtils.setOutlineTextButtonStyle(viewBinding.tvPlaybackSpeed);

        seekBarViewWrapper = new SeekBarViewWrapper(viewBinding.sbTrackState);
        seekBarViewWrapper.setProgressChangeListener(presenter::onTrackRewoundTo);
        seekBarViewWrapper.setOnSeekStartListener(presenter::onSeekStart);
        seekBarViewWrapper.setOnSeekStopListener(presenter::onSeekStop);

        viewBinding.ivPlayPause.setOnClickListener(v -> presenter.onPlayPauseClicked());
        viewBinding.ivRepeatMode.setOnClickListener(v -> presenter.onRepeatModeButtonClicked());
        onCheckChanged(viewBinding.cbKeepPlayingAfterClose, presenter::onKeepPlayerInBackgroundChecked);

        viewBinding.ivFastForward.setOnClickListener(v -> presenter.onFastSeekForwardCalled());
        setOnHoldListener(viewBinding.ivFastForward, presenter::onFastSeekForwardCalled);
        viewBinding.ivRewind.setOnClickListener(v -> presenter.onFastSeekBackwardCalled());
        setOnHoldListener(viewBinding.ivRewind, presenter::onFastSeekBackwardCalled);

        if (savedInstanceState == null && getIntent().getBooleanExtra(LAUNCH_PREPARE_ARG, true)) {
            Uri uriToPlay = getIntent().getData();
            createCompositionSource(uriToPlay);
        }
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(Components.getAppComponent().localeController().dispatchAttachBaseContext(base));
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        Uri uriToPlay = intent.getData();
        createCompositionSource(uriToPlay);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (sourceCreationDisposable != null) {
            sourceCreationDisposable.dispose();
        }
    }

    @Override
    public void displayComposition(ExternalCompositionSource source) {
        viewBinding.tvComposition.setText(formatCompositionName(source.getTitle(), source.getDisplayName()));
        viewBinding.tvCompositionAuthor.setText(FormatUtils.formatAuthor(source.getArtist(), this));
        seekBarViewWrapper.setMax(source.getDuration());
        viewBinding.tvTotalTime.setText(formatMilliseconds(source.getDuration()));
        Components.getAppComponent()
                .imageLoader()
                .displayImageInReusableTarget(viewBinding.ivMusicIcon, source, R.drawable.ic_music_placeholder);
    }

    @Override
    public void showPlayerState(boolean isPlaying) {
        if (isPlaying) {
            AndroidUtils.setAnimatedVectorDrawable(viewBinding.ivPlayPause, R.drawable.anim_play_to_pause);
            viewBinding.ivPlayPause.setContentDescription(getString(R.string.pause));
        } else {
            AndroidUtils.setAnimatedVectorDrawable(viewBinding.ivPlayPause, R.drawable.anim_pause_to_play);
            viewBinding.ivPlayPause.setContentDescription(getString(R.string.play));
        }
    }

    @Override
    public void showTrackState(long currentPosition, long duration) {
        seekBarViewWrapper.setProgress(currentPosition);
        String formattedTime = formatMilliseconds(currentPosition);
        viewBinding.sbTrackState.setContentDescription(getString(R.string.position_template, formattedTime));
        viewBinding.tvPlayedTime.setText(formattedTime);
    }

    @Override
    public void showRepeatMode(int mode) {
        @DrawableRes int iconRes = getRepeatModeIcon(mode);
        viewBinding.ivRepeatMode.setImageResource(iconRes);
        String description = getString(getRepeatModeText(mode));
        viewBinding.ivRepeatMode.setContentDescription(description);
    }

    @Override
    public void showPlayErrorState(@Nullable ErrorCommand errorCommand) {
        if (errorCommand == null) {
            viewBinding.tvError.setVisibility(View.GONE);
            return;
        }
        viewBinding.tvError.setVisibility(VISIBLE);
        viewBinding.tvError.setText(errorCommand.getMessage());
    }

    @Override
    public void showKeepPlayerInBackground(boolean externalPlayerKeepInBackground) {
        setChecked(viewBinding.cbKeepPlayingAfterClose, externalPlayerKeepInBackground);
    }

    @Override
    public void displayPlaybackSpeed(float speed) {
        viewBinding.tvPlaybackSpeed.setText(getString(R.string.playback_speed_template, speed));
        viewBinding.tvPlaybackSpeed.setOnClickListener(v ->
                DialogUtils.showSpeedSelectorDialog(this,
                        speed,
                        presenter::onPlaybackSpeedSelected)
        );
    }

    @Override
    public void showSpeedChangeFeatureVisible(boolean visible) {
        viewBinding.tvPlaybackSpeed.setVisibility(visible? VISIBLE: View.GONE);
    }

    private void createCompositionSource(@Nullable Uri uri) {
        if (uri == null) {
            Toast.makeText(this, "Not enough data to play composition", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        ExternalCompositionSource.Builder builder = new ExternalCompositionSource.Builder(uri);
        sourceCreationDisposable = Single.fromCallable(() -> builder)
                .map(this::readDataFromContentResolver)
                .timeout(2, TimeUnit.SECONDS)
                .map(this::readDataFromFile)
                .timeout(2, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .onErrorReturnItem(builder)
                .subscribe(
                        createdBuilder -> presenter.onSourceForPlayingReceived(createdBuilder.build())
                );
    }

    private ExternalCompositionSource.Builder readDataFromContentResolver(ExternalCompositionSource.Builder builder) {
        String displayName = null;
        long size = 0;

        try (Cursor cursor = getContentResolver().query(
                builder.getUri(),
                new String[] {
                        MediaStore.Audio.Media.DISPLAY_NAME,
                        MediaStore.Audio.Media.SIZE
                },
                null,
                null,
                null)) {
            CursorWrapper cursorWrapper = new CursorWrapper(cursor);
            if (cursor != null && cursor.moveToFirst()) {
                displayName = cursorWrapper.getString(MediaStore.Audio.Media.DISPLAY_NAME);
                size = cursorWrapper.getLong(MediaStore.Audio.Media.SIZE);
            }
        } catch (Exception ignored) {}

        return builder.setDisplayName(displayName == null? "unknown name" : displayName)
                .setSize(size);
    }

    private ExternalCompositionSource.Builder readDataFromFile(ExternalCompositionSource.Builder builder) {
        String title = null;
        String artist = null;
        String album = null;
        long duration = 0;
        byte[] imageBytes = null;
        MediaMetadataRetriever mmr = null;
        try {
            mmr = new MediaMetadataRetriever();
            mmr.setDataSource(this, builder.getUri());

            artist = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
            title = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
            album = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
            String durationStr = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            try {
                duration = Long.parseLong(durationStr);
            } catch (NumberFormatException ignored) {}

            int coverSize = getResources().getInteger(R.integer.icon_image_size);
            imageBytes = ImageUtils.downscaleImageBytes(mmr.getEmbeddedPicture(), coverSize);
        } catch (Exception ignored) {

        } finally {
            if (mmr != null) {
                try {
                    mmr.release();
                } catch (IOException ignored) {}
            }
        }

        return builder.setTitle(title)
                .setArtist(artist)
                .setAlbum(album)
                .setDuration(duration)
                .setImageBytes(imageBytes);
    }
}
