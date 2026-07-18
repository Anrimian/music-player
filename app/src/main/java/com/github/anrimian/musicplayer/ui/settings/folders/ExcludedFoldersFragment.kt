package com.github.anrimian.musicplayer.ui.settings.folders

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

class ExcludedFoldersFragment : Fragment(), FragmentNavigationListener {

    private val viewModel by viewModel<ExcludedFoldersViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val fn = FragmentNavigation.from(parentFragmentManager)

        val toolbar = requireActivity().findViewById<AdvancedToolbar>(R.id.toolbar)
        toolbar.setTitle(R.string.excluded_folders)
        toolbar.setSubtitle(null)
        toolbar.setTitleClickListener(null)

        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                AppTheme {
                    ExcludedFoldersScreen(
                        viewModel = viewModel,
                        swipeToCloseReporter = SwipeToCloseReporter(fn, toolbar)
                    )
                }
            }
        }
    }

    override fun onFragmentResumed() {
        val toolbar = requireActivity().findViewById<AdvancedToolbar>(R.id.toolbar)
        toolbar.clearOptionsMenu()
    }

}