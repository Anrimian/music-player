package com.github.anrimian.musicplayer.ui.playlists.details

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import com.github.anrimian.musicplayer.AppConstants
import com.github.anrimian.musicplayer.R
import com.github.anrimian.musicplayer.data.models.folders.UriFileReference
import com.github.anrimian.musicplayer.di.utils.viewModel
import com.github.anrimian.musicplayer.domain.models.playlist.Playlist
import com.github.anrimian.musicplayer.ui.common.compose.AppTheme
import com.github.anrimian.musicplayer.ui.common.effects.CommonEffect
import com.github.anrimian.musicplayer.ui.common.format.FormatUtils
import com.github.anrimian.musicplayer.ui.common.mvvm.BaseViewModel
import com.github.anrimian.musicplayer.ui.common.navigation.CloseScreen
import com.github.anrimian.musicplayer.ui.common.navigation.Screen
import com.github.anrimian.musicplayer.ui.common.toolbar.AdvancedToolbar
import com.github.anrimian.musicplayer.ui.editor.composition.CompositionEditorActivity
import com.github.anrimian.musicplayer.ui.equalizer.EqualizerDialogFragment
import com.github.anrimian.musicplayer.ui.main.MainActivity
import com.github.anrimian.musicplayer.ui.playlists.choose.ChoosePlayListDialogFragment
import com.github.anrimian.musicplayer.ui.sleep_timer.SleepTimerDialogFragment
import com.github.anrimian.musicplayer.ui.utils.compose.components.swipe.SwipeToCloseReporter
import com.github.anrimian.musicplayer.ui.utils.fragments.DialogFragmentRunner
import com.github.anrimian.musicplayer.ui.utils.fragments.navigation.FragmentNavigation
import com.github.anrimian.musicplayer.ui.utils.fragments.navigation.FragmentNavigationListener
import com.github.anrimian.musicplayer.ui.utils.fragments.safeShow
import com.github.anrimian.musicplayer.ui.utils.safeLaunch

class PlaylistDetailsFragment : Fragment(), FragmentNavigationListener {

    companion object Companion {
        fun newInstance(playListId: Long) = PlaylistDetailsFragment().apply {
            arguments = Bundle().apply {
                putParcelable(BaseViewModel.VM_ARG_KEY, Screen.PlaylistDetails(playListId))
            }
        }
    }

    private val viewModel by viewModel<PlaylistDetailsViewModel>()

    private lateinit var toolbar: AdvancedToolbar

    private lateinit var choosePlaylistDialogRunner: DialogFragmentRunner<ChoosePlayListDialogFragment>

    private val pickFolderLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        if (uri != null) {
            viewModel.onFolderForExportSelected(UriFileReference(uri))
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val fn = FragmentNavigation.from(parentFragmentManager)

        toolbar = requireActivity().findViewById(R.id.toolbar)

        val fm = childFragmentManager
        choosePlaylistDialogRunner = DialogFragmentRunner(
            fm,
            AppConstants.Tags.SELECT_PLAYLIST_TAG
        ) { fragment ->
            fragment.setComplexCompleteListener { playlist, extra ->
                viewModel.onPlaylistToAddingSelected(
                    playlist,
                    extra.getLongArray(AppConstants.Arguments.IDS_ARG)!!,
                )
            }
        }

        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                AppTheme {
                    PlaylistDetailsScreen(
                        viewModel = viewModel,
                        navigationCallback = ::handleNavigationEffect,
                        actionsCallback = ::handleActionEffect,
                        toolbarCallback = ::showPlayListInfo,
                        swipeToCloseReporter = SwipeToCloseReporter(fn, toolbar)
                    )
                }
            }
        }
    }

    override fun onFragmentResumed() {
        requireActivity().findViewById<AdvancedToolbar>(R.id.toolbar).setup {
            setupSearch(viewModel::onSearchTextChanged, text = viewModel.getSearchText())
            setupOptionsMenu(R.menu.play_list_toolbar_menu, ::onOptionsItemClicked)
        }
        viewModel.onFragmentResumed()
    }

    private fun showPlayListInfo(playList: Playlist) {
        toolbar.setTitle(playList.name)
        toolbar.setSubtitle(
            FormatUtils.formatPlaylistAdditionalInfo(
            requireContext(),
            playList,
            R.drawable.ic_secondary_text_circle_inverse
        ))
    }

    private fun onOptionsItemClicked(item: MenuItem) {
        when (item.itemId) {
            R.id.menu_search -> toolbar.setSearchModeEnabled(true)
            R.id.menu_sort -> viewModel.onSortButtonClicked()
            R.id.menu_change_play_list_name -> viewModel.onChangePlaylistNameButtonClicked()
            R.id.menu_export_playlist -> viewModel.onExportPlaylistClicked()
            R.id.menu_sleep_timer -> SleepTimerDialogFragment().safeShow(childFragmentManager)
            R.id.menu_equalizer -> EqualizerDialogFragment().safeShow(childFragmentManager)
            R.id.menu_delete_play_list -> viewModel.onDeletePlaylistButtonClicked()
        }
    }

    private fun handleNavigationEffect(effect: CommonEffect.NavigationEffect) {
        when (val screen = effect.screen) {
            is Screen.TagsEditor -> {
                startActivity(CompositionEditorActivity.newIntent(requireContext(), screen.compositionId))
            }
            is CloseScreen -> {
                FragmentNavigation.from(parentFragmentManager).goBack()
            }
            else -> {}
        }
    }

    private fun handleActionEffect(effect: PlaylistDetailsEffect) {
        when (effect) {
            is PlaylistDetailsEffect.ShowSelectPlaylistDialog -> {
                val args = Bundle().apply {
                    putLongArray(AppConstants.Arguments.IDS_ARG, effect.compositionIds)
                }
                choosePlaylistDialogRunner.show(ChoosePlayListDialogFragment.newInstance(args))
            }
            is PlaylistDetailsEffect.ShowInFolders -> {
                MainActivity.showInFolders(requireActivity(), effect.compositionId)
            }
            is PlaylistDetailsEffect.LaunchPickFolder -> {
                pickFolderLauncher.safeLaunch(requireContext(), null)
            }
            else -> {}
        }
    }

}