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
    
    private lateinit var viewBinding: FragmentSettingsDisplayBinding
  
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewBinding = FragmentSettingsDisplayBinding.inflate(inflater, container, false)
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val toolbar: AdvancedToolbar = requireActivity().findViewById(R.id.toolbar)
        toolbar.setTitle(R.string.settings)
        toolbar.setSubtitle(R.string.display)
        toolbar.setTitleClickListener(null)
        
        SlidrPanel.simpleSwipeBack(viewBinding.nsvContainer, this, toolbar::onStackFragmentSlided)

        viewBinding.cbColoredNotification.visibility =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) VISIBLE else GONE
        
        onCheckChanged(viewBinding.cbUseFileName, presenter::onFileNameChecked)
        onCheckChanged(viewBinding.cbCovers, presenter::onCoversChecked)
        onCheckChanged(viewBinding.cbCoversInNotification, presenter::onCoversInNotificationChecked)
        onCheckChanged(viewBinding.cbColoredNotification, presenter::onColoredNotificationChecked)
        onCheckChanged(viewBinding.cbShowCoverStubInNotification, presenter::onNotificationCoverStubChecked)
        onCheckChanged(viewBinding.cbNotificationOnLockScreen, presenter::onCoversOnLockScreenChecked)

        val localeController = Components.getAppComponent().localeController()
        viewBinding.tvLocale.text = localeController.getCurrentLocaleName()
        viewBinding.tvLocaleClickableArea.setOnClickListener {
            localeController.openLocaleChooser(requireActivity())
        }
    }

    override fun showFileNameEnabled(enabled: Boolean) {
        setChecked(viewBinding.cbUseFileName, enabled)
    }

    override fun showCoversChecked(checked: Boolean) {
        setChecked(viewBinding.cbCovers, checked)
    }

    override fun showCoversInNotificationChecked(checked: Boolean) {
        setChecked(viewBinding.cbCoversInNotification, checked)
    }

    override fun showColoredNotificationChecked(checked: Boolean) {
        setChecked(viewBinding.cbColoredNotification, checked)
    }

    override fun showCoversOnLockScreenChecked(checked: Boolean) {
        setChecked(viewBinding.cbNotificationOnLockScreen, checked)
    }

    override fun showCoversInNotificationEnabled(enabled: Boolean) {
        viewBinding.cbCoversInNotification.isEnabled = enabled
    }

    override fun showColoredNotificationEnabled(enabled: Boolean) {
        viewBinding.cbColoredNotification.isEnabled = enabled
    }

    override fun showCoversOnLockScreenEnabled(enabled: Boolean) {
        viewBinding.cbNotificationOnLockScreen.isEnabled = enabled
    }

    override fun showNotificationCoverStubChecked(checked: Boolean) {
        setChecked(viewBinding.cbShowCoverStubInNotification, checked)
    }

    override fun showNotificationCoverStubEnabled(enabled: Boolean) {
        viewBinding.cbShowCoverStubInNotification.isEnabled = enabled
    }
}