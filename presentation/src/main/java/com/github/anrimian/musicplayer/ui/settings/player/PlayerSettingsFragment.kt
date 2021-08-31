package com.github.anrimian.musicplayer.ui.settings.player

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import com.github.anrimian.musicplayer.R
import com.github.anrimian.musicplayer.data.controllers.music.equalizer.EqualizerType
import com.github.anrimian.musicplayer.databinding.FragmentSettingsPlayerBinding
import com.github.anrimian.musicplayer.di.Components
import com.github.anrimian.musicplayer.ui.common.toolbar.AdvancedToolbar
import com.github.anrimian.musicplayer.ui.equalizer.EqualizerDialogFragment
import com.github.anrimian.musicplayer.ui.utils.ViewUtils
import com.github.anrimian.musicplayer.ui.utils.slidr.SlidrPanel
import moxy.MvpAppCompatFragment
import moxy.ktx.moxyPresenter

class PlayerSettingsFragment : MvpAppCompatFragment(), PlayerSettingsView {

    private val presenter by moxyPresenter { Components.getSettingsComponent().playerSettingsPresenter() }
    
    private lateinit var viewBinding: FragmentSettingsPlayerBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewBinding = FragmentSettingsPlayerBinding.inflate(inflater, container, false)
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val toolbar: AdvancedToolbar = requireActivity().findViewById(R.id.toolbar)
        toolbar.setTitle(R.string.settings)
        toolbar.setSubtitle(R.string.playing)
        toolbar.setTitleClickListener(null)

        ViewUtils.onCheckChanged(viewBinding.cbDecreaseVolume, presenter::onDecreaseVolumeOnAudioFocusLossChecked)
        ViewUtils.onCheckChanged(viewBinding.cbPauseOnAudioFocusLoss, presenter::onPauseOnAudioFocusLossChecked)
        ViewUtils.onCheckChanged(viewBinding.cbPauseOnZeroVolumeLevel, presenter::onPauseOnZeroVolumeLevelChecked)

        viewBinding.flEqualizerClickableArea.setOnClickListener { showEqualizerDialog() }

        SlidrPanel.simpleSwipeBack(viewBinding.nsvContainer, this, toolbar::onStackFragmentSlided)
    }

    override fun showDecreaseVolumeOnAudioFocusLossEnabled(checked: Boolean) {
        ViewUtils.setChecked(viewBinding.cbDecreaseVolume, checked)
    }

    override fun showPauseOnAudioFocusLossEnabled(checked: Boolean) {
        ViewUtils.setChecked(viewBinding.cbPauseOnAudioFocusLoss, checked)
    }

    override fun showPauseOnZeroVolumeLevelEnabled(enabled: Boolean) {
        ViewUtils.setChecked(viewBinding.cbPauseOnZeroVolumeLevel, enabled)
    }

    override fun showSelectedEqualizerType(type: Int) {
        viewBinding.tvEqualizerState.setText(getEqualizerTypeDescription(type))
    }

    @StringRes
    private fun getEqualizerTypeDescription(type: Int): Int {
        return when (type) {
            EqualizerType.EXTERNAL -> R.string.system_equalizer
            EqualizerType.APP -> R.string.app_equalizer
            else -> R.string.no_equalizer
        }
    }

    private fun showEqualizerDialog() {
        EqualizerDialogFragment().show(childFragmentManager, null)
    }
}