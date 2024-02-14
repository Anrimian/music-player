package com.github.anrimian.musicplayer.ui.settings

import android.annotation.SuppressLint
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
import com.github.anrimian.musicplayer.ui.common.view.ViewUtils
import com.github.anrimian.musicplayer.ui.settings.display.DisplaySettingsFragment
import com.github.anrimian.musicplayer.ui.settings.headset.HeadsetSettingsFragment
import com.github.anrimian.musicplayer.ui.settings.library.LibrarySettingsFragment
import com.github.anrimian.musicplayer.ui.settings.player.PlayerSettingsFragment
import com.github.anrimian.musicplayer.ui.settings.themes.ThemeSettingsFragment
import com.github.anrimian.musicplayer.ui.utils.fragments.navigation.FragmentNavigation
import com.github.anrimian.musicplayer.ui.utils.fragments.navigation.FragmentNavigationListener
import com.github.anrimian.musicplayer.ui.utils.slidr.SlidrPanel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers

/**
 * Created on 19.10.2017.
 */
class SettingsFragment : Fragment(),
    FragmentNavigationListener {
    
    private lateinit var binding: FragmentSettingsBinding
    private lateinit var navigation: FragmentNavigation
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val toolbar = requireActivity().findViewById<AdvancedToolbar>(R.id.toolbar)
        navigation = FragmentNavigation.from(parentFragmentManager)
        binding.tvDisplay.setOnClickListener { navigation.addNewFragment(DisplaySettingsFragment()) }
        binding.tvLibrary.setOnClickListener { navigation.addNewFragment(LibrarySettingsFragment()) }
        binding.tvPlayer.setOnClickListener { navigation.addNewFragment(PlayerSettingsFragment()) }
        binding.tvTheme.setOnClickListener { navigation.addNewFragment(ThemeSettingsFragment()) }
        binding.tvHeadset.setOnClickListener { navigation.addNewFragment(HeadsetSettingsFragment()) }
        binding.llRunRescanStorage.setOnClickListener { onRescanStorageButtonClicked() }
        ViewUtils.onLongVibrationClick(binding.llRunRescanStorage) { onRescanStorageButtonLongClick() }

        SlidrPanel.simpleSwipeBack(binding.flContainer, this, toolbar::onStackFragmentSlided)
    }

    override fun onFragmentResumed() {
        val toolbar = requireActivity().findViewById<AdvancedToolbar>(R.id.toolbar)
        toolbar.setTitle(R.string.settings)
        toolbar.setSubtitle(null)
        toolbar.setTitleClickListener(null)
        toolbar.clearOptionsMenu()
    }

    @SuppressLint("CheckResult")
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

    @SuppressLint("CheckResult")
    private fun onRescanStorageButtonLongClick() {
        val appContext = requireContext().applicationContext
        Components.getAppComponent()
            .mediaScannerRepository()
            .rescanStoragePlaylists()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                Toast.makeText(appContext, R.string.playlists_scanning_completed, Toast.LENGTH_SHORT).show()
            }
    }

}