package com.github.anrimian.musicplayer.ui.settings.display

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import com.github.anrimian.musicplayer.R
import com.github.anrimian.musicplayer.databinding.FragmentSettingsDisplayBinding
import com.github.anrimian.musicplayer.di.Components
import com.github.anrimian.musicplayer.ui.common.toolbar.AdvancedToolbar
import com.github.anrimian.musicplayer.ui.utils.ViewUtils.onCheckChanged
import com.github.anrimian.musicplayer.ui.utils.ViewUtils.setChecked
import com.github.anrimian.musicplayer.ui.utils.slidr.SlidrPanel
import moxy.MvpAppCompatFragment
import moxy.ktx.moxyPresenter

class DisplaySettingsFragment : MvpAppCompatFragment(), DisplaySettingsView {

    private val presenter by moxyPresenter { Components.getSettingsComponent().displaySettingsPresenter() }
    
    private lateinit var binding: FragmentSettingsDisplayBinding
  
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSettingsDisplayBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val toolbar: AdvancedToolbar = requireActivity().findViewById(R.id.toolbar)
        toolbar.setTitle(R.string.settings)
        toolbar.setSubtitle(R.string.display)
        toolbar.setTitleClickListener(null)
        
        SlidrPanel.simpleSwipeBack(binding.nsvContainer, this, toolbar::onStackFragmentSlided)

        binding.cbColoredNotification.visibility = if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
            && Build.VERSION.SDK_INT < Build.VERSION_CODES.S
        ) VISIBLE else GONE

        binding.cbShowCoverStubInNotification.visibility = if (
            Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU
        ) VISIBLE else GONE
        
        onCheckChanged(binding.cbUseFileName, presenter::onFileNameChecked)
        onCheckChanged(binding.cbPlayerScreensSwipe, presenter::onSwipePlayerScreensChecked)
        onCheckChanged(binding.cbCovers, presenter::onCoversChecked)
        onCheckChanged(binding.cbCoversInNotification, presenter::onCoversInNotificationChecked)
        onCheckChanged(binding.cbColoredNotification, presenter::onColoredNotificationChecked)
        onCheckChanged(binding.cbShowCoverStubInNotification, presenter::onNotificationCoverStubChecked)
        onCheckChanged(binding.cbNotificationOnLockScreen, presenter::onCoversOnLockScreenChecked)

        val localeController = Components.getAppComponent().localeController()
        binding.tvLocale.text = localeController.getCurrentLocaleName()
        binding.tvLocaleClickableArea.setOnClickListener {
            localeController.openLocaleChooser(requireActivity())
        }
    }

    override fun showPlayerScreensSwipeEnabled(enabled: Boolean) {
        setChecked(binding.cbPlayerScreensSwipe, enabled)
    }

    override fun showFileNameEnabled(enabled: Boolean) {
        setChecked(binding.cbUseFileName, enabled)
    }

    override fun showCoversChecked(checked: Boolean) {
        setChecked(binding.cbCovers, checked)
    }

    override fun showCoversInNotificationChecked(checked: Boolean) {
        setChecked(binding.cbCoversInNotification, checked)
    }

    override fun showColoredNotificationChecked(checked: Boolean) {
        setChecked(binding.cbColoredNotification, checked)
    }

    override fun showCoversOnLockScreenChecked(checked: Boolean) {
        setChecked(binding.cbNotificationOnLockScreen, checked)
    }

    override fun showCoversInNotificationEnabled(enabled: Boolean) {
        binding.cbCoversInNotification.isEnabled = enabled
    }

    override fun showColoredNotificationEnabled(enabled: Boolean) {
        binding.cbColoredNotification.isEnabled = enabled
    }

    override fun showCoversOnLockScreenEnabled(enabled: Boolean) {
        binding.cbNotificationOnLockScreen.isEnabled = enabled
    }

    override fun showNotificationCoverStubChecked(checked: Boolean) {
        setChecked(binding.cbShowCoverStubInNotification, checked)
    }

    override fun showNotificationCoverStubEnabled(enabled: Boolean) {
        binding.cbShowCoverStubInNotification.isEnabled = enabled
    }
}