package com.github.anrimian.musicplayer.ui.common.menu

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import com.github.anrimian.musicplayer.R
import com.github.anrimian.musicplayer.databinding.MenuVolumePopupBinding
import com.github.anrimian.musicplayer.di.Components
import com.github.anrimian.musicplayer.ui.common.view.onVolumeHold
import com.github.anrimian.musicplayer.ui.utils.getDimensionPixelSize
import com.github.anrimian.musicplayer.ui.utils.views.seek_bar.SeekBarViewWrapper

fun showVolumePopup(
    anchorView: View,
    gravity: Int,
) {
    val context = anchorView.context
    val binding = MenuVolumePopupBinding.inflate(LayoutInflater.from(context))

    val systemMusicController = Components.getAppComponent().systemMusicController()

    SeekBarViewWrapper(binding.sbVolume).setProgressChangeListener(systemMusicController::setVolume)

    binding.btnVolumeUp.setOnClickListener { systemMusicController.changeVolumeBy(1) }
    binding.btnVolumeUp.onVolumeHold { systemMusicController.changeVolumeBy(1) }
    binding.btnVolumeDown.setOnClickListener { systemMusicController.changeVolumeBy(-1) }
    binding.btnVolumeDown.onVolumeHold { systemMusicController.changeVolumeBy( -1) }

    val screenMargin = context.getDimensionPixelSize(R.dimen.popup_screen_margin)
    val menu = AppPopupWindow.showPopupWindow(anchorView, binding.root, Gravity.END, gravity, screenMargin)

    val disposable = systemMusicController.volumeStateObservable.subscribe { volume ->
        binding.sbVolume.max = volume.max
        binding.sbVolume.progress = volume.getVolume()
    }
    menu?.setOnDismissListener {
        disposable.dispose()
    }
}