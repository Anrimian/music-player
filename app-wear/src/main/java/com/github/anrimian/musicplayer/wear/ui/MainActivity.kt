package com.github.anrimian.musicplayer.wear.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager.widget.PagerAdapter
import com.github.anrimian.domain.models.ExternalWearableComposition
import com.github.anrimian.domain.models.LibraryWearableComposition
import com.github.anrimian.domain.models.WearableComposition
import com.github.anrimian.musicplayer.domain.models.volume.VolumeState
import com.github.anrimian.musicplayer.domain.utils.functions.Opt
import com.github.anrimian.musicplayer.ui.common.format.TimeFormatUtils
import com.github.anrimian.musicplayer.ui.utils.views.seek_bar.SeekBarViewWrapper
import com.github.anrimian.musicplayer.wear.Constants
import com.github.anrimian.musicplayer.wear.R
import com.github.anrimian.musicplayer.wear.databinding.ActivityMainBinding
import com.github.anrimian.musicplayer.wear.di.Components
import com.github.anrimian.musicplayer.wear.domain.models.DeviceState
import com.github.anrimian.musicplayer.wear.domain.models.ErrorEvent
import com.github.anrimian.musicplayer.wear.domain.models.PlayQueueItem
import com.github.anrimian.musicplayer.wear.ui.common.AppWearUtils
import com.github.anrimian.musicplayer.wear.ui.common.FormatUtils
import com.github.anrimian.musicplayer.wear.ui.common.onRewindHold
import com.github.anrimian.musicplayer.wear.ui.common.onVolumeHold
import com.github.anrimian.musicplayer.wear.ui.queue.PlayQueueAdapter
import com.github.anrimian.musicplayer.wear.utils.logger.onRotaryInputChanged
import com.github.anrimian.musicplayer.wear.utils.logger.playTickVibration
import com.github.anrimian.utils.setAnimatedVectorDrawable
import moxy.MvpAppCompatActivity
import moxy.ktx.moxyPresenter


/*
    TODO-W strings with main module. How to unite resources? - almost done
         move only common strings to common module - no, all strings in shared module

    TODO-W ui: main screen: current composition + skip-play-skip + repeat mode + play queue at bottom
    TODO-W watch face composition + skip-play-pause + repeat mode
    TODO-W main screen icon - current cover(https://developer.android.com/codelabs/data-sources#0)
    TODO-W external composition source: return to library source logic:
         - when background playing is disabled - return to library source on close
         - add close action to notification
    TODO-W seek state - do not send value every second, mimic it
    TODO-W wearable should "predict state": after play move to play state, if no event - after timeout return to previous state
    TODO-W shared preferences helper - move list position stuff to device domain module
    TODO-W cover&palette&dynamic_accent_color. Calculate accent color on host app and send to wear
         (DynamicThemeController)
 */
class MainActivity : MvpAppCompatActivity(), MainView {

    private val presenter by moxyPresenter { Components.getAppComponent().mainPresenter() }

    private lateinit var binding: ActivityMainBinding

    private lateinit var seekBarViewWrapper: SeekBarViewWrapper

    private lateinit var playQueueAdapter: PlayQueueAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.clContentContainer.requestFocus()
        binding.clContentContainer.onRotaryInputChanged { v -> onRotaryInputChanged(v) }

        binding.ivPlayPause.setOnClickListener { presenter.onPlayPauseClicked() }
        binding.ivPrevious.setOnClickListener { presenter.onPreviousClicked() }
        binding.ivPrevious.onRewindHold {   }
        binding.ivNext.setOnClickListener { presenter.onNextClicked() }
        binding.ivNext.onRewindHold {   }
        binding.ivVolumeMinus.setOnClickListener { presenter.onVolumeChangeRequested(false) }
        binding.ivVolumeMinus.onVolumeHold { presenter.onVolumeChangeRequested(false) }
        binding.ivVolumePlus.setOnClickListener { presenter.onVolumeChangeRequested(true) }
        binding.ivVolumePlus.onVolumeHold { presenter.onVolumeChangeRequested(true) }
        //TODO-W skip and volume buttons hold -> start delay is shorter, delay between calls is shorter too
        //TODO-W (?) on volume hold - show shadow overlay when hold is active and button on side to cancel hold
        //TODO-W when volume hold reaches border, play stronger vibration

        seekBarViewWrapper = SeekBarViewWrapper(binding.seekBar)
        seekBarViewWrapper.setProgressChangeListener(presenter::onTrackRewoundTo)
        seekBarViewWrapper.setOnSeekStartListener(presenter::onPositionSeekStart)
        seekBarViewWrapper.setOnSeekStopListener(presenter::onPositionSeekStop)

        //scrolling issues with list:
        // rv consumes down fling scroll
        // pager can be scrolled to 0 in disabled swipe state
        // other solution: https://stackoverflow.com/a/60641357/5541688
        // + consider adding top page with other actions
        binding.pager.adapter = object: PagerAdapter() {

            override fun instantiateItem(container: ViewGroup, position: Int): Any {
                return when(position) {
                    0 -> container.findViewById(R.id.clContentContainer)
                    1 -> container.findViewById<ViewGroup>(R.id.rvQueue)
                    else -> throw IllegalStateException()
                }
            }

            override fun getCount(): Int {
                return 2
            }

            override fun isViewFromObject(view: View, obj: Any): Boolean {
                return view == obj as View
            }
        }

        binding.rvQueue.layoutManager = LinearLayoutManager(this)
        playQueueAdapter = PlayQueueAdapter(this, binding.rvQueue)
        binding.rvQueue.adapter = playQueueAdapter
//        binding.rvQueue.addOnScrollListener(object :RecyclerView.OnScrollListener(){
//            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
//                super.onScrolled(recyclerView, dx, dy)
////                binding.pager.setSwipeEnabled(binding.rvQueue.computeVerticalScrollOffset() == 0)
//            }
//        })
    }

    override fun onStart() {
        super.onStart()
        presenter.onScreenStarted()
    }

    override fun onStop() {
        super.onStop()
        presenter.onScreenStopped()
    }

    override fun showIsPlaying(isPlaying: Boolean) {
        if (isPlaying) {
            binding.ivPlayPause.setAnimatedVectorDrawable(R.drawable.anim_play_to_pause)
        } else {
            binding.ivPlayPause.setAnimatedVectorDrawable(R.drawable.anim_pause_to_play)
        }
    }

    override fun showComposition(compositionOpt: Opt<WearableComposition>) {
        when(val composition = compositionOpt.value) {
            is LibraryWearableComposition -> {
                binding.tvCompositionTitle.text = composition.title
                binding.tvCompositionArtist.text = composition.artist//format artist
                binding.tvTotalTime.text = TimeFormatUtils.formatMilliseconds(composition.duration)

                binding.ivNext.setImageResource(R.drawable.ic_skip_next)
                binding.ivPrevious.setImageResource(R.drawable.ic_skip_previous)
            }
            is ExternalWearableComposition -> {
                binding.tvCompositionTitle.text = composition.title
                binding.tvCompositionArtist.text = composition.artist//format artist
                binding.tvTotalTime.text = TimeFormatUtils.formatMilliseconds(composition.duration)

                binding.ivNext.setImageResource(R.drawable.ic_fast_forward)
                binding.ivPrevious.setImageResource(R.drawable.ic_rewind)
            }
            else -> {
                binding.tvCompositionTitle.text = "No current composition"//TODO-W move string resources to common. Check length for this one(too long)
                binding.tvCompositionArtist.text = null
                binding.tvTotalTime.text = TimeFormatUtils.formatMilliseconds(0)
                binding.tvPlayedTime.text = TimeFormatUtils.formatMilliseconds(0)
            }
        }
    }

    override fun showDeviceState(deviceState: DeviceState) {
        if (deviceState == DeviceState.CONNECTED) {
            binding.pager.visibility = View.VISIBLE
            binding.llError.visibility = View.INVISIBLE
            return
        }
        binding.pager.visibility = View.INVISIBLE
        binding.llError.visibility = View.VISIBLE

        binding.tvError.setText(FormatUtils.formatDeviceStateError(deviceState))
        binding.btnError.setText(R.string.locate_app)
        binding.btnError.setOnClickListener { onLocateAppClicked(deviceState) }
    }

    override fun showTrackState(trackPosition: Long, duration: Long) {
        seekBarViewWrapper.setProgress(trackPosition, duration)
        val formattedTime = TimeFormatUtils.formatMilliseconds(trackPosition)
        //TODO-W finish
//        binding.seekBar.contentDescription = getString(R.string.position_template, formattedTime)
        binding.tvPlayedTime.text = formattedTime
    }

    override fun showErrorEvent(errorEvent: ErrorEvent) {
        Log.d("KEK", "showErrorEvent: $errorEvent")
        //TODO-W for no permission show button to open main app
        //TODO-W format error "Main/device app has no permission"
        //DialogUtils.showActionDialog
        //should be swipeable bottom sheet with title and action(s)?
        Toast.makeText(this, FormatUtils.formatErrorEvent(this, errorEvent), Toast.LENGTH_LONG).show()
    }

    override fun showCurrentVolume(volume: Long) {
        val volumeState = VolumeState.from(volume)
        val text = if (volumeState.toLong() == Constants.NO_STATE) {
            ""
        } else {
            //move to strings, FormatUtils and duplicate for tiles+PlayerFragment+ExternalPlayerActivity
            //getString(R.string.percentage_template, volumePercent)
            val percent = 100 * volumeState.getVolume() / volumeState.getMaxVolume()
            "$percent%"
        }
        binding.tvVolume.text = text
    }

    override fun updatePlayQueue(playQueueItems: List<PlayQueueItem>) {
        playQueueAdapter.submitList(playQueueItems)
    }

    private fun onLocateAppClicked(deviceState: DeviceState) {
        val data = Uri.parse("https://play.google.com/store/apps/details?id=${packageName}")
        if (deviceState == DeviceState.WEAR_UPDATE_REQUIRED) {
            //locate wear app
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = data
            intent.setPackage("com.android.vending")
            startActivity(intent)
            return
        }
        //locate host app
        val intent = Intent(Intent.ACTION_VIEW)
            .addCategory(Intent.CATEGORY_BROWSABLE)
            .setData(data)
        AppWearUtils.launchDeviceIntent(this, intent, getString(R.string.check_your_device))
    }

    private fun onRotaryInputChanged(value: Float) {
        if (presenter.onVolumeChangeRequested(value < 0)) {
            playTickVibration()
        }
    }

}