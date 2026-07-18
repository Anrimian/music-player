package com.github.anrimian.musicplayer.ui.main.external_player

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Toast
import androidx.annotation.DrawableRes
import com.github.anrimian.musicplayer.AppConstants
import com.github.anrimian.musicplayer.R
import com.github.anrimian.musicplayer.data.models.composition.source.ExternalCompositionSource
import com.github.anrimian.musicplayer.data.models.folders.UriFileReference
import com.github.anrimian.musicplayer.databinding.ActivityExternalPlayerBinding
import com.github.anrimian.musicplayer.di.Components
import com.github.anrimian.musicplayer.domain.models.utils.CompositionHelper
import com.github.anrimian.musicplayer.domain.models.volume.VolumeState
import com.github.anrimian.musicplayer.ui.common.activity.BaseMvpAppCompatActivity
import com.github.anrimian.musicplayer.ui.common.compat.CompatUtils
import com.github.anrimian.musicplayer.ui.common.dialogs.speed.SpeedSelectorDialogFragment
import com.github.anrimian.musicplayer.ui.common.error.ErrorCommand
import com.github.anrimian.musicplayer.ui.common.format.FormatUtils
import com.github.anrimian.musicplayer.ui.common.format.TimeFormatUtils
import com.github.anrimian.musicplayer.ui.common.format.getVolumeIcon
import com.github.anrimian.musicplayer.ui.common.menu.showVolumePopup
import com.github.anrimian.musicplayer.ui.common.view.onRewindHold
import com.github.anrimian.musicplayer.ui.common.view.setSmallDrawableStart
import com.github.anrimian.musicplayer.ui.utils.AndroidUtils
import com.github.anrimian.musicplayer.ui.utils.ImageUtils
import com.github.anrimian.musicplayer.ui.utils.ViewUtils
import com.github.anrimian.musicplayer.ui.utils.fragments.DialogFragmentRunner
import com.github.anrimian.musicplayer.ui.utils.views.seek_bar.SeekBarViewWrapper
import com.github.anrimian.utils.setAnimatedVectorDrawable
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.Disposable
import moxy.ktx.moxyPresenter

class ExternalPlayerActivity : BaseMvpAppCompatActivity(), ExternalPlayerView {

    private val presenter by moxyPresenter { 
        Components.getExternalPlayerComponent().externalPlayerPresenter() 
    }
    
    private lateinit var binding: ActivityExternalPlayerBinding
    
    private lateinit var seekBarViewWrapper: SeekBarViewWrapper
    private var sourceCreationDisposable: Disposable? = null

    private lateinit var speedDialogFragmentRunner: DialogFragmentRunner<SpeedSelectorDialogFragment>

    override fun onCreate(savedInstanceState: Bundle?) {
        Components.getAppComponent().themeController().applyCurrentTheme(this)
        theme.applyStyle(R.style.DialogActivityTheme, true)
        super.onCreate(savedInstanceState)
        binding = ActivityExternalPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        CompatUtils.setOutlineTextButtonStyle(binding.tvPlaybackSpeed)
        CompatUtils.setOutlineTextButtonStyle(binding.tvVolume)

        binding.tvVolume.setOnClickListener { v -> showVolumePopup(v, Gravity.CENTER_VERTICAL) }

        seekBarViewWrapper = SeekBarViewWrapper(binding.sbTrackState)
        seekBarViewWrapper.setProgressChangeListener(presenter::onTrackRewoundTo)
        seekBarViewWrapper.setOnSeekStartListener(presenter::onSeekStart)
        seekBarViewWrapper.setOnSeekStopListener(presenter::onSeekStop)

        binding.ivPlayPause.setOnClickListener { presenter.onPlayPauseClicked() }
        binding.ivRepeatMode.setOnClickListener { presenter.onRepeatModeButtonClicked() }

        ViewUtils.onCheckChanged(binding.cbKeepPlayingAfterClose, presenter::onKeepPlayerInBackgroundChecked)

        binding.ivFastForward.setOnClickListener { presenter.onFastSeekForwardCalled() }
        binding.ivFastForward.onRewindHold(presenter::onFastSeekForwardCalled)
        binding.ivRewind.setOnClickListener { presenter.onFastSeekBackwardCalled() }
        binding.ivRewind.onRewindHold(presenter::onFastSeekBackwardCalled)

        speedDialogFragmentRunner = DialogFragmentRunner(
            supportFragmentManager,
            AppConstants.Tags.SPEED_SELECTOR_TAG
        ) { fragment ->
            fragment.setSpeedChangeListener(presenter::onPlaybackSpeedSelected)
        }

        if (savedInstanceState == null
            && intent.getBooleanExtra(AppConstants.Arguments.LAUNCH_PREPARE_ARG, true)
        ) {
            val uriToPlay = intent.data
            onUriReceived(uriToPlay)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        val uriToPlay = intent.data
        onUriReceived(uriToPlay)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (sourceCreationDisposable != null) {
            sourceCreationDisposable!!.dispose()
        }
    }

    override fun displayComposition(source: ExternalCompositionSource?) {
        if (source == null) {
            binding.tvComposition.text = null
            binding.tvCompositionAuthor.text = null
            binding.tvTotalTime.text = null
            binding.ivCover.setImageResource(R.drawable.ic_music_placeholder)
            return
        }
        binding.tvComposition.text = CompositionHelper.formatCompositionName(source.title, source.displayName)
        binding.tvCompositionAuthor.text = FormatUtils.formatAuthor(source.artist, this)
        binding.tvTotalTime.text = TimeFormatUtils.formatMilliseconds(source.duration)

        Components.getAppComponent()
            .imageLoader()
            .displayImageInReusableTarget(
                binding.ivCover,
                source,
                R.drawable.ic_music_placeholder
            )
    }

    override fun showPlayerState(isPlaying: Boolean) {
        if (isPlaying) {
            binding.ivPlayPause.setAnimatedVectorDrawable(R.drawable.anim_play_to_pause)
            binding.ivPlayPause.contentDescription = getString(R.string.pause)
        } else {
            binding.ivPlayPause.setAnimatedVectorDrawable(R.drawable.anim_pause_to_play)
            binding.ivPlayPause.contentDescription = getString(R.string.play)
        }
    }

    override fun showTrackState(currentPosition: Long, duration: Long) {
        seekBarViewWrapper.setProgress(currentPosition, duration)
        val formattedTime = TimeFormatUtils.formatMilliseconds(currentPosition)
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
            speedDialogFragmentRunner.show(SpeedSelectorDialogFragment.newInstance(speed))
        }
    }

    override fun showSpeedVisible(visible: Boolean) {
        binding.tvPlaybackSpeed.visibility = if (visible) View.VISIBLE else View.GONE
    }

    override fun showVolumeState(volume: Long) {
        val volumeState = VolumeState.from(volume)
        val volumePercent = 100 * volumeState.getVolume() / volumeState.getMaxVolume()
        binding.tvVolume.text = getString(R.string.percentage_template, volumePercent)
        binding.tvVolume.setSmallDrawableStart(getVolumeIcon(volumePercent))
    }

    override fun closeScreen() {
        finish()
    }

    private fun onUriReceived(uri: Uri?) {
        if (uri == null) {
            Toast.makeText(this, R.string.no_enough_data_to_play, Toast.LENGTH_LONG).show()
            finish()
            return
        }
        presenter.onFileReferenceReceived(UriFileReference(uri))
    }

}