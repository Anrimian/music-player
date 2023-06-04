package com.github.anrimian.musicplayer.ui.playlist_screens.playlists

import android.app.AlertDialog
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.anrimian.musicplayer.Constants
import com.github.anrimian.musicplayer.Constants.PLAYLIST_MIME_TYPE
import com.github.anrimian.musicplayer.R
import com.github.anrimian.musicplayer.data.models.folders.UriFileReference
import com.github.anrimian.musicplayer.databinding.FragmentPlayListsBinding
import com.github.anrimian.musicplayer.di.Components
import com.github.anrimian.musicplayer.domain.models.playlist.PlayList
import com.github.anrimian.musicplayer.domain.models.utils.ListPosition
import com.github.anrimian.musicplayer.ui.common.dialogs.showConfirmDeleteDialog
import com.github.anrimian.musicplayer.ui.common.error.ErrorCommand
import com.github.anrimian.musicplayer.ui.common.format.getExportedPlaylistsMessage
import com.github.anrimian.musicplayer.ui.common.format.showSnackbar
import com.github.anrimian.musicplayer.ui.common.menu.PopupMenuWindow
import com.github.anrimian.musicplayer.ui.common.toolbar.AdvancedToolbar
import com.github.anrimian.musicplayer.ui.common.view.ViewUtils
import com.github.anrimian.musicplayer.ui.playlist_screens.create.CreatePlayListDialogFragment
import com.github.anrimian.musicplayer.ui.playlist_screens.playlist.newPlayListFragment
import com.github.anrimian.musicplayer.ui.playlist_screens.playlists.adapter.PlayListsAdapter
import com.github.anrimian.musicplayer.ui.playlist_screens.rename.RenamePlayListDialogFragment
import com.github.anrimian.musicplayer.ui.utils.fragments.navigation.FragmentNavigation
import com.github.anrimian.musicplayer.ui.utils.fragments.navigation.FragmentNavigationListener
import com.github.anrimian.musicplayer.ui.utils.fragments.safeShow
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.RecyclerViewUtils
import moxy.MvpAppCompatFragment
import moxy.ktx.moxyPresenter

fun newPlaylistsFragment(playlistUri: String? = null) = PlayListsFragment().apply {
    arguments = Bundle().apply {
        putString(Constants.Arguments.PLAYLIST_IMPORT_ARG, playlistUri)
    }
}

class PlayListsFragment : MvpAppCompatFragment(), PlayListsView,
    FragmentNavigationListener {

    private val presenter by moxyPresenter { Components.getAppComponent().playListsPresenter() }

    private lateinit var binding: FragmentPlayListsBinding

    private lateinit var adapter: PlayListsAdapter
    private lateinit var layoutManager: LinearLayoutManager

    private val pickFolderLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        if (uri != null) {
            presenter.onFolderForExportSelected(UriFileReference(uri))
        }
    }

    private val pickPlaylistFileLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            presenter.onPlaylistFileReceived(UriFileReference(uri))
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentPlayListsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        layoutManager = LinearLayoutManager(context)
        binding.rvPlayLists.layoutManager = layoutManager

        RecyclerViewUtils.attachFastScroller(binding.rvPlayLists)

        adapter = PlayListsAdapter(
            binding.rvPlayLists,
            this::goToPlayListScreen,
            this::onPlaylistMenuClicked
        )

        binding.rvPlayLists.adapter = adapter
        binding.fab.setOnClickListener { onCreatePlayListButtonClicked() }

        val playlistImportUri = requireArguments().getString(Constants.Arguments.PLAYLIST_IMPORT_ARG)
        if (playlistImportUri != null) {
            requireArguments().remove(Constants.Arguments.PLAYLIST_IMPORT_ARG)
            importPlaylist(playlistImportUri)
        }
    }

    override fun onFragmentResumed() {
        requireActivity().findViewById<AdvancedToolbar>(R.id.toolbar).setup { config ->
            config.setTitle(R.string.play_lists)
            config.setSubtitle(null)
            config.setupOptionsMenu(R.menu.play_lists_toolbar_menu, this::onOptionsItemClicked)
        }
        presenter.onFragmentMovedToTop()
    }

    override fun onStop() {
        super.onStop()
        presenter.onStop(ViewUtils.getListPosition(layoutManager))
    }

    override fun showEmptyList() {
        binding.progressStateView.showMessage(R.string.play_lists_on_device_not_found, false)
    }

    override fun showList() {
        binding.progressStateView.hideAll()
    }

    override fun showLoading() {
        binding.progressStateView.showProgress()
    }

    override fun updateList(lists: List<PlayList>) {
        adapter.submitList(lists)
    }

    override fun restoreListPosition(listPosition: ListPosition) {
        ViewUtils.scrollToPosition(layoutManager, listPosition)
    }

    override fun showConfirmDeletePlayListDialog(playList: PlayList) {
        showConfirmDeleteDialog(requireContext(), playList) {
            presenter.onDeletePlayListDialogConfirmed(playList)
        }
    }

    override fun showPlayListDeleteSuccess(playList: PlayList) {
        binding.listContainer.showSnackbar(getString(R.string.play_list_deleted, playList.name))
    }

    override fun showDeletePlayListError(errorCommand: ErrorCommand) {
        binding.listContainer.showSnackbar(getString(R.string.play_list_delete_error, errorCommand.message))
    }

    override fun showEditPlayListNameDialog(playList: PlayList) {
        RenamePlayListDialogFragment.newInstance(playList.id).safeShow(childFragmentManager)
    }

    override fun launchPickFolderScreen() {
        pickFolderLauncher.launch(null)
    }

    override fun showErrorMessage(errorCommand: ErrorCommand) {
        binding.listContainer.showSnackbar(errorCommand.message)
    }

    override fun showPlaylistExportSuccess(playlists: List<PlayList>) {
        binding.listContainer.showSnackbar(getExportedPlaylistsMessage(requireContext(), playlists))
    }

    override fun launchPlayListScreen(playlistId: Long) {
        FragmentNavigation.from(parentFragmentManager).addNewFragment(newPlayListFragment(playlistId))
    }

    override fun showOverwritePlaylistDialog() {
        AlertDialog.Builder(requireContext())
            .setMessage(R.string.overwrite_playlist)
            .setNegativeButton(android.R.string.cancel) { _, _ -> }
            .setPositiveButton(android.R.string.ok) { _, _ -> presenter.onOverwritePlaylistConfirmed()}
            .show()
    }

    override fun showNotCompletelyImportedPlaylistDialog(
        playlistId: Long,
        notFoundFilesCount: Int,
    ) {
        val message = resources.getQuantityString(
            R.plurals.playlist_import_partial_success,
            notFoundFilesCount,
            notFoundFilesCount
        )
        AlertDialog.Builder(requireContext())
            .setMessage(message)
            .setPositiveButton(android.R.string.ok) { _, _ -> launchPlayListScreen(playlistId) }
            .show()
    }

    fun importPlaylist(uriStr: String) {
        presenter.onPlaylistFileReceived(UriFileReference(Uri.parse(uriStr)))
    }

    private fun onPlaylistMenuClicked(playList: PlayList, view: View) {
        PopupMenuWindow.showPopup(view, R.menu.play_list_menu) { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_change_play_list_name -> {
                    presenter.onChangePlayListNameButtonClicked(playList)
                }
                R.id.menu_export_playlist -> {
                    presenter.onExportPlaylistClicked(playList)
                }
                R.id.menu_delete_play_list -> {
                    presenter.onDeletePlayListButtonClicked(playList)
                }
            }
        }
    }

    private fun onOptionsItemClicked(item: MenuItem) {
        when (item.itemId) {
            R.id.menu_import_playlist -> pickPlaylistFileLauncher.launch(PLAYLIST_MIME_TYPE)
        }
    }

    private fun onCreatePlayListButtonClicked() {
        CreatePlayListDialogFragment().safeShow(childFragmentManager)
    }

    private fun goToPlayListScreen(playList: PlayList) {
        launchPlayListScreen(playList.id)
    }
}