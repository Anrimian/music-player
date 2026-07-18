package com.github.anrimian.musicplayer.ui.settings.headset

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
import com.github.anrimian.musicplayer.ui.common.toolbar.AdvancedToolbar
import com.github.anrimian.musicplayer.ui.utils.compose.components.swipe.SwipeToCloseReporter
import com.github.anrimian.musicplayer.ui.utils.fragments.navigation.FragmentNavigation
import com.github.anrimian.musicplayer.ui.utils.fragments.navigation.FragmentNavigationListener

class HeadsetSettingsFragment : Fragment(), FragmentNavigationListener {

    private val viewModel by viewModel<HeadsetSettingsViewModel>()

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
                    HeadsetSettingsScreen(
                        viewModel = viewModel,
                        navigationCallback = {},
                        swipeToCloseReporter = SwipeToCloseReporter(fn, toolbar)
                    )
                }
            }
        }
    }

    override fun onFragmentResumed() {
        val toolbar: AdvancedToolbar = requireActivity().findViewById(R.id.toolbar)
        toolbar.setTitle(R.string.settings)
        toolbar.setSubtitle(R.string.headset)
        toolbar.setTitleClickListener(null)
    }

}