package com.github.anrimian.musicplayer.ui.settings.library

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
import com.github.anrimian.musicplayer.ui.common.effects.CommonEffect
import com.github.anrimian.musicplayer.ui.common.navigation.Screen
import com.github.anrimian.musicplayer.ui.common.toolbar.AdvancedToolbar
import com.github.anrimian.musicplayer.ui.settings.folders.ExcludedFoldersFragment
import com.github.anrimian.musicplayer.ui.utils.compose.components.swipe.SwipeToCloseReporter
import com.github.anrimian.musicplayer.ui.utils.fragments.navigation.FragmentNavigation
import com.github.anrimian.musicplayer.ui.utils.fragments.navigation.FragmentNavigationListener

/**
 * Created on 19.10.2017.
 */
class LibrarySettingsFragment : Fragment(), FragmentNavigationListener {

    private val viewModel by viewModel<LibrarySettingsViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val fn = FragmentNavigation.from(parentFragmentManager)
        val toolbar: AdvancedToolbar = requireActivity().findViewById(R.id.toolbar)

        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                AppTheme {
                    LibrarySettingsScreen(
                        viewModel = viewModel,
                        navigationCallback = ::handleNavigationEffect,
                        actionsCallback = {},
                        swipeToCloseReporter = SwipeToCloseReporter(fn, toolbar)
                    )
                }
            }
        }
    }

    override fun onFragmentResumed() {
        val toolbar: AdvancedToolbar = requireActivity().findViewById(R.id.toolbar)
        toolbar.setTitle(R.string.settings)
        toolbar.setSubtitle(R.string.library)
        toolbar.setTitleClickListener(null)
    }

    private fun handleNavigationEffect(effect: CommonEffect.NavigationEffect) {
        val fn = FragmentNavigation.from(parentFragmentManager)
        when(effect.screen) {
            is Screen.ExcludedFolders -> {
                fn.addNewFragment(ExcludedFoldersFragment())
            }
        }
    }

}