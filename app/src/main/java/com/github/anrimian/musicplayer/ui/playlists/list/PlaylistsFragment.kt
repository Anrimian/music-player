package com.github.anrimian.musicplayer.ui.playlists.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.IdRes
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import com.github.anrimian.musicplayer.AppConstants
import com.github.anrimian.musicplayer.AppConstants.PLAYLIST_MIME_TYPE
import com.github.anrimian.musicplayer.R
import com.github.anrimian.musicplayer.data.models.folders.UriFileReference
import com.github.anrimian.musicplayer.di.utils.viewModel
import com.github.anrimian.musicplayer.ui.common.compose.AppTheme
import com.github.anrimian.musicplayer.ui.common.effects.CommonEffect
import com.github.anrimian.musicplayer.ui.common.navigation.Screen
import com.github.anrimian.musicplayer.ui.common.toolbar.AdvancedToolbar
import com.github.anrimian.musicplayer.ui.equalizer.EqualizerDialogFragment
import com.github.anrimian.musicplayer.ui.playlists.choose.ChoosePlayListDialogFragment
import com.github.anrimian.musicplayer.ui.playlists.details.PlaylistDetailsFragment
import com.github.anrimian.musicplayer.ui.sleep_timer.SleepTimerDialogFragment
import com.github.anrimian.musicplayer.ui.utils.fragments.DialogFragmentRunner
import com.github.anrimian.musicplayer.ui.utils.fragments.navigation.FragmentNavigation
import com.github.anrimian.musicplayer.ui.utils.fragments.navigation.FragmentNavigationListener
import com.github.anrimian.musicplayer.ui.utils.fragments.safeShow
import com.github.anrimian.musicplayer.ui.utils.safeLaunch


class PlaylistsFragment : Fragment(), FragmentNavigationListener {

    companion object Companion {
        fun newInstance(playlistUri: String? = null) = PlaylistsFragment().apply {
            arguments = Bundle().apply {
                putString(AppConstants.Arguments.PLAYLIST_IMPORT_ARG, playlistUri)
            }
        }
    }

    private val viewModel by viewModel<PlaylistsViewModel>()

    private lateinit var toolbar: AdvancedToolbar

    private lateinit var choosePlayListDialogRunner: DialogFragmentRunner<ChoosePlayListDialogFragment>

    private val pickFolderLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        if (uri != null) {
            viewModel.onFolderForExportSelected(UriFileReference(uri))
        }
    }

    private val pickPlaylistFileLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            viewModel.onPlaylistFileReceived(UriFileReference(uri))
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        toolbar = requireActivity().findViewById(R.id.toolbar)

        choosePlayListDialogRunner = DialogFragmentRunner(
            childFragmentManager,
            AppConstants.Tags.SELECT_PLAYLIST_TAG
        ) { fragment ->
            fragment.setComplexCompleteListener { playlist, extra ->
                viewModel.onPlayListToAddingSelected(
                    playlist,
                    extra.getLongArray(AppConstants.Arguments.IDS_ARG)!!,
                    extra.getBoolean(AppConstants.Arguments.CLOSE_MULTISELECT_ARG)
                )
            }
        }

        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                AppTheme {
                    PlaylistsScreen(
                        viewModel = viewModel,
                        navigationCallback = ::handleNavigationEffect,
                        actionsCallback = ::handleActionEffect,
                        selectionModeCallback = ::onSelectionModeChanged
                    )
                }
            }
        }
    }

    override fun onFragmentResumed() {
        requireActivity().findViewById<AdvancedToolbar>(R.id.toolbar).setup {
            setTitle(R.string.play_lists)
            setSubtitle(null)
            setupSearch(viewModel::onSearchTextChanged, text = viewModel.getSearchText())
            setupOptionsMenu(R.menu.play_lists_toolbar_menu, ::onOptionsItemClicked)
            setupSelectionModeMenu(
                R.menu.play_lists_selection_menu,
                ::onActionModeItemClicked,
                viewModel::onExitSelectionModeClicked
            )
        }
        viewModel.onFragmentResumed()
    }

    fun importPlaylist(uriStr: String) {
        viewModel.onPlaylistFileReceived(UriFileReference(uriStr.toUri()))
    }

    private fun onOptionsItemClicked(item: MenuItem) {
        when (item.itemId) {
            R.id.menu_search -> toolbar.setSearchModeEnabled(true)
            R.id.menu_import_playlist -> pickPlaylistFileLauncher.safeLaunch(requireContext(), PLAYLIST_MIME_TYPE)
            R.id.menu_sleep_timer -> SleepTimerDialogFragment().safeShow(childFragmentManager)
            R.id.menu_equalizer -> EqualizerDialogFragment().safeShow(childFragmentManager)
        }
    }

    private fun onActionModeItemClicked(menuItem: MenuItem) {
        when (menuItem.itemId) {
            R.id.menu_play -> viewModel.onPlayAllSelectedClicked()
            R.id.menu_select_all -> viewModel.onSelectAllButtonClicked()
            R.id.menu_play_next -> viewModel.onPlayNextSelectedPlaylistsClicked()
            R.id.menu_add_to_queue -> viewModel.onAddToQueueSelectedPlaylistsClicked()
            R.id.menu_add_to_playlist -> viewModel.onAddSelectedPlaylistsToPlayListClicked()
            R.id.menu_export_playlist -> viewModel.onExportSelectedPlaylistsClicked()
            R.id.menu_share -> viewModel.onShareSelectedPlaylistsClicked()
            R.id.menu_delete -> viewModel.onDeleteSelectedPlaylistsButtonClicked()
        }
    }

    private fun handleNavigationEffect(effect: CommonEffect.NavigationEffect) {
        val fn = FragmentNavigation.from(parentFragmentManager)
        when (val screen = effect.screen) {
            is Screen.PlaylistDetails -> {
                fn.addNewFragment(PlaylistDetailsFragment.newInstance(screen.playlistId))
            }
        }
    }

    private fun handleActionEffect(effect: PlaylistsEffect) {
        when (effect) {
            is PlaylistsEffect.ShowSelectPlayListDialog -> {
                val args = Bundle().apply {
                    putLongArray(AppConstants.Arguments.IDS_ARG, effect.playlistIds)
                    putBoolean(AppConstants.Arguments.CLOSE_MULTISELECT_ARG, effect.closeSelectionMode)
                }
                choosePlayListDialogRunner.show(ChoosePlayListDialogFragment.newInstance(args))
            }
            is PlaylistsEffect.LaunchPickFolder -> {
                pickFolderLauncher.safeLaunch(requireContext(), null)
            }
            else -> {}
        }
    }

    private fun onSelectionModeChanged(selectionModeState: SelectionModeState?) { //
        if (selectionModeState == null) {
            toolbar.showSelectionMode(0)
        } else {
            toolbar.showSelectionMode(selectionModeState.selectedItemsCount)
            toolbar.updateSelectionMenu { item ->
                item.isVisible = isSelectionItemVisible(
                    item.itemId,
                    selectionModeState.totalSelectedCompositionsCount
                )
            }
        }
    }

    private fun isSelectionItemVisible(@IdRes itemId: Int, compositionCount: Int): Boolean {
        return compositionCount != 0
                || (itemId != R.id.menu_play
                && itemId != R.id.menu_play_next
                && itemId != R.id.menu_add_to_queue
                && itemId != R.id.menu_add_to_playlist
                && itemId != R.id.menu_share)
    }

}