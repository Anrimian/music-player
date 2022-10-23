package com.github.anrimian.musicplayer.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.github.anrimian.musicplayer.R
import com.github.anrimian.musicplayer.databinding.FragmentSettingsBinding
import com.github.anrimian.musicplayer.di.Components
import com.github.anrimian.musicplayer.ui.common.toolbar.AdvancedToolbar
import com.github.anrimian.musicplayer.ui.settings.display.DisplaySettingsFragment
import com.github.anrimian.musicplayer.ui.settings.headset.HeadsetSettingsFragment
import com.github.anrimian.musicplayer.ui.settings.library.LibrarySettingsFragment
import com.github.anrimian.musicplayer.ui.settings.player.PlayerSettingsFragment
import com.github.anrimian.musicplayer.ui.settings.themes.ThemeSettingsFragment
import com.github.anrimian.musicplayer.ui.utils.fragments.navigation.FragmentLayerListener
import com.github.anrimian.musicplayer.ui.utils.fragments.navigation.FragmentNavigation
import com.github.anrimian.musicplayer.ui.utils.slidr.SlidrPanel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers

/**
 * Created on 19.10.2017.
 */
class SettingsFragment : Fragment(), FragmentLayerListener {
    
    private lateinit var viewBinding: FragmentSettingsBinding
    private lateinit var navigation: FragmentNavigation
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewBinding = FragmentSettingsBinding.inflate(inflater, container, false)
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val toolbar = requireActivity().findViewById<AdvancedToolbar>(R.id.toolbar)
        navigation = FragmentNavigation.from(parentFragmentManager)
        viewBinding.tvDisplay.setOnClickListener { navigation.addNewFragment(DisplaySettingsFragment()) }
        viewBinding.tvLibrary.setOnClickListener { navigation.addNewFragment(LibrarySettingsFragment()) }
        viewBinding.tvPlayer.setOnClickListener { navigation.addNewFragment(PlayerSettingsFragment()) }
        viewBinding.tvTheme.setOnClickListener { navigation.addNewFragment(ThemeSettingsFragment()) }
        viewBinding.tvHeadset.setOnClickListener { navigation.addNewFragment(HeadsetSettingsFragment()) }
        viewBinding.llRunRescanStorage.setOnClickListener { onRescanStorageButtonClicked() }

        SlidrPanel.simpleSwipeBack(viewBinding.flContainer, this, toolbar::onStackFragmentSlided)
    }

    override fun onFragmentMovedOnTop() {
        val toolbar = requireActivity().findViewById<AdvancedToolbar>(R.id.toolbar)
        toolbar.setTitle(R.string.settings)
        toolbar.subtitle = null
        toolbar.setTitleClickListener(null)
        toolbar.clearOptionsMenu()
    }

    private fun onRescanStorageButtonClicked() {
        val appContext = requireContext().applicationContext
        Components.getAppComponent()
            .mediaScannerRepository()
            .runStorageScanner()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                Toast.makeText(appContext, R.string.scanning_completed, Toast.LENGTH_SHORT).show()
            }
    }
}