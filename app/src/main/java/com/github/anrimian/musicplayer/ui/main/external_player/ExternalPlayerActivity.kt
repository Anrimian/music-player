package com.github.anrimian.musicplayer.ui.main.external_player

import android.content.Context
import android.content.Intent
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.annotation.DrawableRes
import com.github.anrimian.musicplayer.Constants
import com.github.anrimian.musicplayer.R
import com.github.anrimian.musicplayer.data.models.composition.source.ExternalCompositionSource
import com.github.anrimian.musicplayer.data.utils.db.CursorWrapper
import com.github.anrimian.musicplayer.databinding.ActivityExternalPlayerBinding
import com.github.anrimian.musicplayer.di.Components
import com.github.anrimian.musicplayer.domain.models.utils.CompositionHelper
import com.github.anrimian.musicplayer.ui.common.compat.CompatUtils
import com.github.anrimian.musicplayer.ui.common.dialogs.DialogUtils
import com.github.anrimian.musicplayer.ui.common.error.ErrorCommand
import com.github.anrimian.musicplayer.ui.common.format.FormatUtils
import com.github.anrimian.musicplayer.ui.common.view.setOnHoldListener
import com.github.anrimian.musicplayer.ui.utils.AndroidUtils
import com.github.anrimian.musicplayer.ui.utils.ImageUtils
import com.github.anrimian.musicplayer.ui.utils.ViewUtils
import com.github.anrimian.musicplayer.ui.utils.views.seek_bar.SeekBarViewWrapper
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import moxy.MvpAppCompatActivity
import moxy.ktx.moxyPresenter
import java.io.IOException
import java.util.concurrent.TimeUnit

class ExternalPlayerActivity : MvpAppCompatActivity(), ExternalPlayerView {

    private val presenter by moxyPresenter { 
        Components.getExternalPlayerComponent().externalPlayerPresenter() 
    }
    
    private lateinit var binding: ActivityExternalPlayerBinding
    
    private lateinit var seekBarViewWrapper: SeekBarViewWrapper
    private var sourceCreationDisposable: Disposable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        Components.getAppComponent().themeController().applyCurrentTheme(this)
        theme.applyStyle(R.style.DialogActivityTheme, true)
        super.onCreate(savedInstanceState)
        binding = ActivityExternalPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        CompatUtils.setOutlineTextButtonStyle(binding.tvPlaybackSpeed)

        seekBarViewWrapper = SeekBarViewWrapper(binding.sbTrackState)
        seekBarViewWrapper.setProgressChangeListener(presenter::onTrackRewoundTo)
        seekBarViewWrapper.setOnSeekStartListener(presenter::onSeekStart)
        seekBarViewWrapper.setOnSeekStopListener(presenter::onSeekStop)

        binding.ivPlayPause.setOnClickListener { presenter.onPlayPauseClicked() }
        binding.ivRepeatMode.setOnClickListener { presenter.onRepeatModeButtonClicked() }

        ViewUtils.onCheckChanged(binding.cbKeepPlayingAfterClose, presenter::onKeepPlayerInBackgroundChecked)

        binding.ivFastForward.setOnClickListener { presenter.onFastSeekForwardCalled() }
        binding.ivFastForward.setOnHoldListener(presenter::onFastSeekForwardCalled)
        binding.ivRewind.setOnClickListener { presenter.onFastSeekBackwardCalled() }
        binding.ivRewind.setOnHoldListener(presenter::onFastSeekBackwardCalled)

        if (savedInstanceState == null
            && intent.getBooleanExtra(Constants.Arguments.LAUNCH_PREPARE_ARG, true)
        ) {
            val uriToPlay = intent.data
            createCompositionSource(uriToPlay)
        }
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(Components.getAppComponent().localeController().dispatchAttachBaseContext(base))
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        val uriToPlay = intent.data
        createCompositionSource(uriToPlay)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (sourceCreationDisposable != null) {
            sourceCreationDisposable!!.dispose()
        }
    }

    override fun displayComposition(source: ExternalCompositionSource) {
        binding.tvComposition.text = CompositionHelper.formatCompositionName(source.title, source.displayName)
        binding.tvCompositionAuthor.text = FormatUtils.formatAuthor(source.artist, this)
        seekBarViewWrapper.setMax(source.duration)
        binding.tvTotalTime.text = FormatUtils.formatMilliseconds(source.duration)

        Components.getAppComponent()
            .imageLoader()
            .displayImageInReusableTarget(
                binding.ivMusicIcon,
                source,
                R.drawable.ic_music_placeholder
            )
    }

    override fun showPlayerState(isPlaying: Boolean) {
        if (isPlaying) {
            AndroidUtils.setAnimatedVectorDrawable(binding.ivPlayPause, R.drawable.anim_play_to_pause)
            binding.ivPlayPause.contentDescription = getString(R.string.pause)
        } else {
            AndroidUtils.setAnimatedVectorDrawable(binding.ivPlayPause, R.drawable.anim_pause_to_play)
            binding.ivPlayPause.contentDescription = getString(R.string.play)
        }
    }

    override fun showTrackState(currentPosition: Long, duration: Long) {
        seekBarViewWrapper.setProgress(currentPosition)
        val formattedTime = FormatUtils.formatMilliseconds(currentPosition)
        binding.sbTrackState.contentDescription = getString(R.string.position_template, formattedTime)
        binding.tvPlayedTime.text = formattedTime
    }

    override fun showRepeatMode(mode: Int) {
        @DrawableRes val iconRes = FormatUtils.getRepeatModeIcon(mode)
        binding.ivRepeatMode.setImageResource(iconRes)
        val description = getString(FormatUtils.getRepeatModeText(mode))
        binding.ivRepeatMode.contentDescription = description
    }

    override fun showPlayErrorState(errorCommand: ErrorCommand?) {
        if (errorCommand == null) {
            binding.tvError.visibility = View.GONE
            return
        }
        binding.tvError.visibility = View.VISIBLE
        binding.tvError.text = errorCommand.message
    }

    override fun showKeepPlayerInBackground(externalPlayerKeepInBackground: Boolean) {
        ViewUtils.setChecked(binding.cbKeepPlayingAfterClose, externalPlayerKeepInBackground)
    }

    override fun displayPlaybackSpeed(speed: Float) {
        binding.tvPlaybackSpeed.text = getString(R.string.playback_speed_template, speed)
        binding.tvPlaybackSpeed.setOnClickListener {
            DialogUtils.showSpeedSelectorDialog(this, speed, presenter::onPlaybackSpeedSelected)
        }
    }

    override fun showSpeedChangeFeatureVisible(visible: Boolean) {
        binding.tvPlaybackSpeed.visibility = if (visible) View.VISIBLE else View.GONE
    }

    private fun createCompositionSource(uri: Uri?) {
        if (uri == null) {
            Toast.makeText(this, R.string.no_enough_data_to_play, Toast.LENGTH_LONG).show()
            finish()
            return
        }
        val builder = ExternalCompositionSource.Builder(uri)
        sourceCreationDisposable = Single.fromCallable { builder }
            .map(::readDataFromContentResolver)
            .timeout(2, TimeUnit.SECONDS)
            .map(::readDataFromFile)
            .timeout(2, TimeUnit.SECONDS)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .onErrorReturnItem(builder)
            .subscribe { createdBuilder ->
                presenter.onSourceForPlayingReceived(createdBuilder.build())
            }
    }

    private fun readDataFromContentResolver(builder: ExternalCompositionSource.Builder): ExternalCompositionSource.Builder {
        var displayName: String? = null
        var size: Long = 0
        try {
            contentResolver.query(
                builder.uri,
                arrayOf(MediaStore.Audio.Media.DISPLAY_NAME, MediaStore.Audio.Media.SIZE),
                null,
                null,
                null
            ).use { cursor ->
                val cursorWrapper = CursorWrapper(cursor)
                if (cursor != null && cursor.moveToFirst()) {
                    displayName = cursorWrapper.getString(MediaStore.Audio.Media.DISPLAY_NAME)
                    size = cursorWrapper.getLong(MediaStore.Audio.Media.SIZE)
                }
            }
        } catch (ignored: Exception) {}
        return builder.setDisplayName(if (displayName == null) "unknown name" else displayName)
            .setSize(size)
    }

    private fun readDataFromFile(
        builder: ExternalCompositionSource.Builder
    ): ExternalCompositionSource.Builder {
        var title: String? = null
        var artist: String? = null
        var album: String? = null
        var duration: Long = 0
        var imageBytes: ByteArray? = null
        var mmr: MediaMetadataRetriever? = null
        try {
            mmr = MediaMetadataRetriever()
            mmr.setDataSource(this, builder.uri)
            artist = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
            title = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
            album = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM)
            val durationStr = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            try {
                duration = durationStr!!.toLong()
            } catch (ignored: NumberFormatException) { }
            val coverSize = resources.getInteger(R.integer.icon_image_size)
            imageBytes = ImageUtils.downscaleImageBytes(mmr.embeddedPicture, coverSize)
        } catch (ignored: Exception) {
        } finally {
            if (mmr != null) {
                try {
                    mmr.release()
                } catch (ignored: IOException) {}
            }
        }
        return builder.setTitle(title)
            .setArtist(artist)
            .setAlbum(album)
            .setDuration(duration)
            .setImageBytes(imageBytes)
    }
}