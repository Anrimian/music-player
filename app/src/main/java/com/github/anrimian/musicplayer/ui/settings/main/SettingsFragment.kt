package com.github.anrimian.musicplayer.ui.settings.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import com.github.anrimian.musicplayer.R
import com.github.anrimian.musicplayer.di.utils.viewModel
import com.github.anrimian.musicplayer.ui.common.compose.AppTheme
import com.github.anrimian.musicplayer.ui.common.dialogs.missing.MissingFilesDialogFragment
import com.github.anrimian.musicplayer.ui.common.effects.BaseEffect
import com.github.anrimian.musicplayer.ui.common.effects.CommonEffect
import com.github.anrimian.musicplayer.ui.common.navigation.Screen
import com.github.anrimian.musicplayer.ui.common.toolbar.AdvancedToolbar
import com.github.anrimian.musicplayer.ui.settings.display.DisplaySettingsFragment
import com.github.anrimian.musicplayer.ui.settings.headset.HeadsetSettingsFragment
import com.github.anrimian.musicplayer.ui.settings.library.LibrarySettingsFragment
import com.github.anrimian.musicplayer.ui.settings.player.PlayerSettingsFragment
import com.github.anrimian.musicplayer.ui.settings.themes.ThemeSettingsFragment
import com.github.anrimian.musicplayer.ui.utils.compose.components.swipe.SwipeToCloseReporter
import com.github.anrimian.musicplayer.ui.utils.fragments.navigation.FragmentNavigation
import com.github.anrimian.musicplayer.ui.utils.fragments.navigation.FragmentNavigationListener
import com.github.anrimian.musicplayer.ui.utils.fragments.safeShow

/**
 * Created on 19.10.2017.
 */
class SettingsFragment : Fragment(), FragmentNavigationListener {

    private val viewModel by viewModel<SettingsViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {

        val fn = FragmentNavigation.from(parentFragmentManager)
        val toolbar: AdvancedToolbar = requireActivity().findViewById(R.id.toolbar)

        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                AppTheme {
                    SettingsScreen(
                        viewModel = viewModel,
                        navigationCallback = ::handleNavigationEffect,
                        actionsCallback = ::handleActionEffect,
                        swipeToCloseReporter = SwipeToCloseReporter(fn, toolbar)
                    )
                }
            }
        }
    }

    override fun onFragmentResumed() {
        val toolbar = requireActivity().findViewById<AdvancedToolbar>(R.id.toolbar)
        toolbar.setTitle(R.string.settings)
        toolbar.setSubtitle(null)
        toolbar.setTitleClickListener(null)
        toolbar.clearOptionsMenu()
    }

    private fun handleActionEffect(effect: BaseEffect) {
        when (effect) {
            SettingsEffect.OpenMissingFilesDialog -> MissingFilesDialogFragment().safeShow(this)
        }
    }

    private fun handleNavigationEffect(effect: CommonEffect.NavigationEffect) {
        val fn = FragmentNavigation.from(parentFragmentManager)
        when (effect.screen) {
            is Screen.DisplaySettings -> {
                fn.addNewFragment(DisplaySettingsFragment())
            }
            is Screen.LibrarySettings -> {
                fn.addNewFragment(LibrarySettingsFragment())
            }
            is Screen.PlayerSettings -> {
                fn.addNewFragment(PlayerSettingsFragment())
            }
            is Screen.HeadsetSettings -> {
                fn.addNewFragment(HeadsetSettingsFragment())
            }
            is Screen.ThemeSettings -> {
                fn.addNewFragment(ThemeSettingsFragment())
            }
        }
    }

}