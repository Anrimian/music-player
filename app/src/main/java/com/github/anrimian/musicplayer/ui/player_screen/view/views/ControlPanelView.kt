package com.github.anrimian.musicplayer.ui.player_screen.view.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.annotation.DrawableRes
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.viewpager2.widget.ViewPager2
import com.github.anrimian.fsync.models.state.file.FileSyncState
import com.github.anrimian.musicplayer.R
import com.github.anrimian.musicplayer.databinding.PartialControlPanelBinding
import com.github.anrimian.musicplayer.di.Components
import com.github.anrimian.musicplayer.domain.interactors.sleep_timer.NO_TIMER
import com.github.anrimian.musicplayer.domain.models.composition.Composition
import com.github.anrimian.musicplayer.domain.models.play_queue.PlayQueueItem
import com.github.anrimian.musicplayer.domain.models.utils.CompositionHelper
import com.github.anrimian.musicplayer.domain.models.volume.VolumeState
import com.github.anrimian.musicplayer.ui.common.compat.CompatUtils
import com.github.anrimian.musicplayer.ui.common.error.ErrorCommand
import com.github.anrimian.musicplayer.ui.common.format.FormatUtils
import com.github.anrimian.musicplayer.ui.common.format.TimeFormatUtils
import com.github.anrimian.musicplayer.ui.common.format.getVolumeIcon
import com.github.anrimian.musicplayer.ui.common.format.showFileSyncState
import com.github.anrimian.musicplayer.ui.common.view.onRewindHold
import com.github.anrimian.musicplayer.ui.common.view.setSmallDrawableStart
import com.github.anrimian.musicplayer.ui.utils.AndroidUtils
import com.github.anrimian.musicplayer.ui.utils.getString
import com.github.anrimian.musicplayer.ui.utils.isLandscape
import com.github.anrimian.musicplayer.ui.utils.isScreenLarge
import com.github.anrimian.musicplayer.ui.utils.onLongClick
import com.github.anrimian.musicplayer.ui.utils.onPageScrolled
import com.github.anrimian.musicplayer.ui.utils.views.delegate.BoundValuesDelegate
import com.github.anrimian.musicplayer.ui.utils.views.delegate.DelegateManager
import com.github.anrimian.musicplayer.ui.utils.views.delegate.ExpandViewDelegate
import com.github.anrimian.musicplayer.ui.utils.views.delegate.MotionLayoutDelegate
import com.github.anrimian.musicplayer.ui.utils.views.delegate.ReverseDelegate
import com.github.anrimian.musicplayer.ui.utils.views.delegate.SlideDelegate
import com.github.anrimian.musicplayer.ui.utils.views.delegate.VisibilityDelegate
import com.github.anrimian.musicplayer.ui.utils.views.seek_bar.SeekBarViewWrapper
import com.github.anrimian.utils.setAnimatedVectorDrawable

class ControlPanelView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MotionLayout(context, attrs, defStyleAttr) {

    private val binding = PartialControlPanelBinding.inflate(LayoutInflater.from(context), this)

    private val isLargeLand = context.isScreenLarge() && context.isLandscape()

    private val motionLayoutDelegate: MotionLayoutDelegate
    private val coverTransitionDelegate: SlideDelegate
    private val panelCollapseDelegate: SlideDelegate

    private val seekBarViewWrapper = SeekBarViewWrapper(binding.sbTrackState)

    private var previousCoverComposition: Composition? = null

    init {
        CompatUtils.setOutlineTextButtonStyle(binding.tvPlaybackSpeed)
        CompatUtils.setOutlineTextButtonStyle(binding.tvSleepTime)
        CompatUtils.setOutlineTextButtonStyle(binding.tvVolume)

        motionLayoutDelegate = MotionLayoutDelegate(this)

        val coverTransitionDelegateManager = DelegateManager()
        coverTransitionDelegateManager.addDelegate(motionLayoutDelegate)
        coverTransitionDelegateManager.addDelegate(
            ReverseDelegate(ExpandViewDelegate(R.dimen.panel_cover_size, binding.ivCover))
        )

        coverTransitionDelegate = BoundValuesDelegate(
            ML_BOUND_START,
            ML_BOUND_END,
            coverTransitionDelegateManager
        )

        panelCollapseDelegate = DelegateManager().apply {
            addDelegate(motionLayoutDelegate)
            addDelegate(BoundValuesDelegate(0.3f, 1.0f, ExpandViewDelegate(R.dimen.panel_cover_size, binding.ivCover)))
            addDelegate(BoundValuesDelegate(0.98f, 1.0f, VisibilityDelegate(binding.pvFileState)))
            addDelegate(BoundValuesDelegate(0.95f, 1.0f, VisibilityDelegate(binding.tvCurrentCompositionArtist)))
            addDelegate(BoundValuesDelegate(0.4f, 1.0f, VisibilityDelegate(binding.btnActionsMenu)))
            addDelegate(BoundValuesDelegate(0.93f, 1.0f, VisibilityDelegate(binding.sbTrackState)))
            addDelegate(BoundValuesDelegate(0.95f, 1.0f, VisibilityDelegate(binding.tvError)))
            addDelegate(BoundValuesDelegate(0.98f, 1.0f, VisibilityDelegate(binding.btnRepeatMode)))
            addDelegate(BoundValuesDelegate(0.98f, 1.0f, VisibilityDelegate(binding.btnRandomMode)))
            addDelegate(BoundValuesDelegate(0.97f, 1.0f, VisibilityDelegate(binding.tvPlayedTime)))
            addDelegate(BoundValuesDelegate(0.97f, 1.0f, VisibilityDelegate(binding.tvTotalTime)))
            addDelegate(BoundValuesDelegate(0.97f, 1.0f, VisibilityDelegate(binding.tvPlaybackSpeed)))
            addDelegate(BoundValuesDelegate(0.97f, 1.0f, VisibilityDelegate(binding.tvVolume)))
            addDelegate(BoundValuesDelegate(0.97f, 1.0f, VisibilityDelegate(binding.tvSleepTime)))
            addDelegate(ReverseDelegate(BoundValuesDelegate(0.8f, 1f, VisibilityDelegate(binding.ivBottomPanelIndicator))));
        }
    }

    fun initWithViewPager(viewPager: ViewPager2) {
        viewPager.onPageScrolled { position, positionOffset, _ ->
            if (viewPager.adapter!!.itemCount == 2) {
                // transition lyrics <-> play queue
                // do nothing
                if (isLargeLand) {
                    // call this to set start state value to large-land
                    //  on other configs is is initialized by bottom sheet state
                    //  on large-land - this is only moving part
                    showCoverOffset(1f)
                }
            } else if (position >= 1) {
                // transition nowPlaying <-> play queue
                val offset = if (position == 2) 1 - positionOffset else positionOffset
                showCoverOffset(offset)
            } else {
                // transition nowPlaying <-> lyrics
                showCoverOffset(1 - positionOffset)
            }
        }
    }

    fun onSkipToPreviousClick(onClick: OnClickListener) {
        binding.ivSkipToPrevious.setOnClickListener(onClick)
    }

    fun onSkipToPreviousHold(action: () -> Unit) {
        binding.ivSkipToPrevious.onRewindHold(action)
    }

    fun onSkipToNextClick(onClick: OnClickListener) {
        binding.ivSkipToNext.setOnClickListener(onClick)
    }

    fun onSkipToNextHold(action: () -> Unit) {
        binding.ivSkipToNext.onRewindHold(action)
    }

    fun onPanelClick(onClick: OnClickListener) {
        if (!isLargeLand) {
            binding.bgTouchView.setOnClickListener(onClick)
        }
    }

    fun onVolumeButtonClick(onClick: OnClickListener) {
        binding.tvVolume.setOnClickListener(onClick)
    }

    fun onRandomModeClick(onClick: OnClickListener) {
        binding.btnRandomMode.setOnClickListener(onClick)
    }

    fun onCoverLongClick(onClick: () -> Unit) {
        binding.ivCover.onLongClick(onClick)
    }

    fun setSeekbarListeners(
        onProgressChange: SeekBarViewWrapper.ProgressChangeListener,
        onSeekStart: SeekBarViewWrapper.OnSeekStartListener,
        onSeekEnd: SeekBarViewWrapper.OnSeekStopListener,
    ) {
        seekBarViewWrapper.setProgressChangeListener(onProgressChange)
        seekBarViewWrapper.setOnSeekStartListener(onSeekStart)
        seekBarViewWrapper.setOnSeekStopListener(onSeekEnd)
    }

    fun clearVectorAnimationInfo() {
        AndroidUtils.clearVectorAnimationInfo(binding.ivPlayPause)
    }

    fun showTrackState(position: Long, duration: Long) {
        seekBarViewWrapper.setProgress(position, duration)
        val formattedTime = TimeFormatUtils.formatMilliseconds(position)
        binding.sbTrackState.contentDescription = getString(
            R.string.position_template,
            formattedTime
        )
        binding.tvPlayedTime.text = formattedTime
    }

    fun showPlayingState(
        isPlaying: Boolean,
        onStopClick: OnClickListener,
        onPlayClick: OnClickListener,
    ) {
        if (isPlaying) {
            binding.ivPlayPause.setAnimatedVectorDrawable(R.drawable.anim_play_to_pause)
            binding.ivPlayPause.contentDescription = getString(R.string.pause)
            binding.ivPlayPause.setOnClickListener(onStopClick)
        } else {
            binding.ivPlayPause.setAnimatedVectorDrawable(R.drawable.anim_pause_to_play)
            binding.ivPlayPause.contentDescription = getString(R.string.play)
            binding.ivPlayPause.setOnClickListener(onPlayClick)
        }
    }

    fun showPlayErrorState(errorCommand: ErrorCommand?) {
        binding.tvError.text = errorCommand?.message
    }

    fun showCurrentQueueItem(item: PlayQueueItem?, onActionMenuClick: (View, PlayQueueItem) -> Unit) {
        val isMusicControlsEnabled = item != null
        binding.btnActionsMenu.isEnabled = isMusicControlsEnabled
        binding.ivSkipToNext.isEnabled = isMusicControlsEnabled
        binding.ivSkipToPrevious.isEnabled = isMusicControlsEnabled
        binding.ivPlayPause.isEnabled = isMusicControlsEnabled
        binding.btnRepeatMode.isEnabled = isMusicControlsEnabled
        binding.btnRandomMode.isEnabled = isMusicControlsEnabled
        binding.sbTrackState.isEnabled = isMusicControlsEnabled
        binding.tvPlaybackSpeed.isEnabled = isMusicControlsEnabled

        if (item == null) {
            binding.tvPlayedTime.text = TimeFormatUtils.formatMilliseconds(0)
            binding.tvTotalTime.text = TimeFormatUtils.formatMilliseconds(0)
            binding.sbTrackState.progress = 0
            binding.tvCurrentComposition.setText(R.string.no_current_composition)
            binding.tvCurrentCompositionArtist.setText(R.string.unknown_author)
            val noCompositionMessage = getString(R.string.no_current_composition)
            binding.bgTouchView.contentDescription =
                getString(R.string.now_playing_template, noCompositionMessage)
            binding.sbTrackState.contentDescription = noCompositionMessage
            previousCoverComposition = null
            binding.btnActionsMenu.setOnClickListener(null)

        } else {
            val compositionName = CompositionHelper.formatCompositionName(item)
            binding.tvCurrentComposition.text = compositionName
            binding.tvTotalTime.text = TimeFormatUtils.formatMilliseconds(item.duration)
            binding.tvCurrentCompositionArtist.text =
                FormatUtils.formatCompositionAuthor(item, context)
            binding.bgTouchView.contentDescription =
                getString(R.string.now_playing_template, compositionName)
            binding.sbTrackState.contentDescription = null
            binding.btnActionsMenu.setOnClickListener { v -> onActionMenuClick(v, item) }
        }
    }

    fun showCurrentItemCover(item: PlayQueueItem?) {
        if (item == null) {
            previousCoverComposition = null
            binding.ivCover.setImageResource(R.drawable.ic_music_placeholder)
            return
        }
        Components.getAppComponent()
            .imageLoader()
            .displayImageInReusableTarget(
                binding.ivCover,
                item,
                previousCoverComposition,
                R.drawable.ic_music_placeholder
            )
        previousCoverComposition = item
    }

    fun showRepeatMode(mode: Int, onRepeatModeClick: OnClickListener) {
        @DrawableRes val iconRes = FormatUtils.getRepeatModeIcon(mode)
        binding.btnRepeatMode.setImageResource(iconRes)
        val description = getString(FormatUtils.getRepeatModeText(mode))
        binding.btnRepeatMode.contentDescription = description

        binding.btnRepeatMode.setOnClickListener(onRepeatModeClick)
    }

    fun showRandomPlayingButton(active: Boolean) {
        val drawable = if (active) {
            R.drawable.anim_shuffle_off_to_on
        } else {
            R.drawable.anim_shuffle_on_to_off
        }
        binding.btnRandomMode.setAnimatedVectorDrawable(drawable)
    }

    fun showPlaybackSpeed(speed: Float, onClick: OnClickListener) {
        binding.tvPlaybackSpeed.text = getString(R.string.playback_speed_template, speed)
        binding.tvPlaybackSpeed.setOnClickListener(onClick)
    }

    fun showSpeedChangeFeatureVisible(visible: Boolean) {
        binding.tvPlaybackSpeed.visibility = if (visible) View.VISIBLE else View.GONE
    }

    fun showSleepTimerRemainingTime(remainingMillis: Long, onClick: OnClickListener) {
        //setVisibility() doesn't work in motion layout
        if (remainingMillis == NO_TIMER) {
            binding.tvSleepTime.text = ""
            binding.tvSleepTime.background = null
            binding.tvSleepTime.setCompoundDrawables(null, null, null, null)
            binding.tvSleepTime.setOnClickListener(null)
            return
        }
        if (!binding.tvSleepTime.hasOnClickListeners()) {
            //initialize, set visible

            binding.tvSleepTime.setSmallDrawableStart(R.drawable.ic_timer)
            binding.tvSleepTime.setBackgroundResource(R.drawable.bg_outline_text_button)
            binding.tvSleepTime.setOnClickListener(onClick)
        }
        binding.tvSleepTime.text = TimeFormatUtils.formatMilliseconds(remainingMillis)
    }

    fun showCurrentCompositionSyncState(syncState: FileSyncState?, item: PlayQueueItem?) {
        val isFileRemote: Boolean
        val formattedState: FileSyncState?
        if (item == null) {
            formattedState = null
            isFileRemote = false
        } else {
            formattedState = syncState
            isFileRemote = CompositionHelper.isCompositionFileRemote(item)
        }
        binding.pvFileState.showFileSyncState(formattedState, isFileRemote)
    }

    fun showVolume(volumeState: VolumeState) {
        val volumePercent = 100 * volumeState.getVolume() / volumeState.getMaxVolume()
        binding.tvVolume.text = getString(R.string.percentage_template, volumePercent)
        binding.tvVolume.setSmallDrawableStart(getVolumeIcon(volumePercent))
    }

    fun setPlayButtonsSelectableBackground(@DrawableRes resId :Int) {
        binding.ivPlayPause.setBackgroundResource(resId)
        binding.ivSkipToNext.setBackgroundResource(resId)
        binding.ivSkipToPrevious.setBackgroundResource(resId)
    }

    fun getPanelCollapseStateDelegate() = panelCollapseDelegate

    private fun showCoverOffset(offset: Float) {
        if (!isLargeLand) {
            if (offset == 1f || offset == 0f) {
                val id = if (offset != 0f) {
                    R.id.transition_collapsed_expanded
                } else {
                    R.id.transition_collapsed_expanded_no_cover
                }
                motionLayoutDelegate.setTransitionId(id)
            } else {
                motionLayoutDelegate.setTransitionId(R.id.transition_expanded_expanded_no_cover)
            }
        }
        //check for isLargeLand bc otherwise on start we have no start state someway
        if (isLargeLand || (offset != 1f && offset != 0f)) {
            coverTransitionDelegate.onSlide(1 - offset)
        }
    }

    companion object {
        private const val ML_BOUND_START = 0.008f
        private const val ML_BOUND_END = 0.95f
    }

}