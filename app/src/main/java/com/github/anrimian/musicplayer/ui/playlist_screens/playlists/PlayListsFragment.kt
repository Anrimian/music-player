package com.github.anrimian.musicplayer.ui.playlist_screens.playlists

import android.app.AlertDialog
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.IdRes
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.anrimian.musicplayer.Constants
import com.github.anrimian.musicplayer.Constants.PLAYLIST_MIME_TYPE
import com.github.anrimian.musicplayer.R
import com.github.anrimian.musicplayer.data.models.folders.UriFileReference
import com.github.anrimian.musicplayer.databinding.FragmentPlayListsBinding
import com.github.anrimian.musicplayer.di.Components
import com.github.anrimian.musicplayer.domain.models.composition.Composition
import com.github.anrimian.musicplayer.domain.models.playlist.PlayList
import com.github.anrimian.musicplayer.domain.models.utils.ListPosition
import com.github.anrimian.musicplayer.domain.utils.toLongArray
import com.github.anrimian.musicplayer.ui.common.dialogs.shareCompositions
import com.github.anrimian.musicplayer.ui.common.dialogs.showConfirmDeleteDialog
import com.github.anrimian.musicplayer.ui.common.error.ErrorCommand
import com.github.anrimian.musicplayer.ui.common.format.getDeletedPlaylistsMessage
import com.github.anrimian.musicplayer.ui.common.format.getExportedPlaylistsMessage
import com.github.anrimian.musicplayer.ui.common.format.showSnackbar
import com.github.anrimian.musicplayer.ui.common.menu.PopupMenuWindow
import com.github.anrimian.musicplayer.ui.common.toolbar.AdvancedToolbar
import com.github.anrimian.musicplayer.ui.common.view.ViewUtils
import com.github.anrimian.musicplayer.ui.equalizer.EqualizerDialogFragment
import com.github.anrimian.musicplayer.ui.library.common.library.BaseLibraryFragment
import com.github.anrimian.musicplayer.ui.library.common.library.BaseLibraryPresenter
import com.github.anrimian.musicplayer.ui.playlist_screens.choose.ChoosePlayListDialogFragment
import com.github.anrimian.musicplayer.ui.playlist_screens.choose.newChoosePlayListDialogFragment
import com.github.anrimian.musicplayer.ui.playlist_screens.create.CreatePlayListDialogFragment
import com.github.anrimian.musicplayer.ui.playlist_screens.playlist.PlayListFragment
import com.github.anrimian.musicplayer.ui.playlist_screens.playlists.adapter.PlayListViewHolder
import com.github.anrimian.musicplayer.ui.playlist_screens.playlists.adapter.PlayListsAdapter
import com.github.anrimian.musicplayer.ui.playlist_screens.rename.newRenamePlaylistDialog
import com.github.anrimian.musicplayer.ui.sleep_timer.SleepTimerDialogFragment
import com.github.anrimian.musicplayer.ui.utils.fragments.BackButtonListener
import com.github.anrimian.musicplayer.ui.utils.fragments.DialogFragmentRunner
import com.github.anrimian.musicplayer.ui.utils.fragments.navigation.FragmentNavigation
import com.github.anrimian.musicplayer.ui.utils.fragments.navigation.FragmentNavigationListener
import com.github.anrimian.musicplayer.ui.utils.fragments.safeShow
import com.github.anrimian.musicplayer.ui.utils.getMenuItems
import com.github.anrimian.musicplayer.ui.utils.safeLaunch
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.RecyclerViewUtils
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.touch_helper.short_swipe.ShortSwipeCallback
import moxy.ktx.moxyPresenter


class PlayListsFragment : BaseLibraryFragment(), PlayListsView, FragmentNavigationListener,
    BackButtonListener {

    companion object {
        fun newInstance(playlistUri: String? = null) = PlayListsFragment().apply {
            arguments = Bundle().apply {
                putString(Constants.Arguments.PLAYLIST_IMPORT_ARG, playlistUri)
            }
        }
    }

    private val presenter by moxyPresenter { Components.getAppComponent().playListsPresenter() }

    private lateinit var binding: FragmentPlayListsBinding

    private lateinit var toolbar: AdvancedToolbar

    private lateinit var adapter: PlayListsAdapter
    private lateinit var layoutManager: LinearLayoutManager

    private lateinit var choosePlayListDialogRunner: DialogFragmentRunner<ChoosePlayListDialogFragment>

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

    override fun getLibraryPresenter(): BaseLibraryPresenter<*> = presenter

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
        toolbar = requireActivity().findViewById(R.id.toolbar)

        layoutManager = LinearLayoutManager(context)
        binding.rvPlayLists.layoutManager = layoutManager

        RecyclerViewUtils.attachFastScroller(binding.rvPlayLists)
        val callback = ShortSwipeCallback(
            requireContext(),
            R.drawable.ic_play_next,
            R.string.play_next,
            shouldNotSwipeViewHolder = this::isPlaylistSwipeNotAllowed,
            swipeCallback = presenter::onPlayNextPlaylistClicked
        )
        val itemTouchHelper = ItemTouchHelper(callback)
        itemTouchHelper.attachToRecyclerView(binding.rvPlayLists)

        adapter = PlayListsAdapter(
            this,
            binding.rvPlayLists,
            presenter.getSelectedPlaylists(),
            presenter::onPlaylistClicked,
            presenter::onPlaylistLongClicked,
            this::onPlaylistMenuClicked
        )

        binding.rvPlayLists.adapter = adapter
        binding.fab.setOnClickListener { onCreatePlayListButtonClicked() }

        binding.progressStateView.onTryAgainClick { presenter.onTryAgainButtonClicked() }

        choosePlayListDialogRunner = DialogFragmentRunner(
            childFragmentManager,
            Constants.Tags.SELECT_PLAYLIST_TAG
        ) { fragment ->
            fragment.setComplexCompleteListener { playlist, extra ->
                presenter.onPlayListToAddingSelected(
                    playlist,
                    extra.getLongArray(Constants.Arguments.IDS_ARG)!!,
                    extra.getBoolean(Constants.Arguments.CLOSE_MULTISELECT_ARG)
                )
            }
        }

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
            config.setupSearch(presenter::onSearchTextChanged, presenter.getSearchText())
            config.setupOptionsMenu(R.menu.play_lists_toolbar_menu, this::onOptionsItemClicked)
            config.setupSelectionModeMenu(R.menu.play_lists_selection_menu, this::onActionModeItemClicked)
        }
        presenter.onFragmentResumed()
    }

    override fun onStop() {
        super.onStop()
        presenter.onStop(ViewUtils.getListPosition(layoutManager))
    }

    override fun onBackPressed(): Boolean {
        if (toolbar.isInActionMode()) {
            presenter.onSelectionModeBackPressed()
            return true
        }
        if (toolbar.isInSearchMode()) {
            toolbar.setSearchModeEnabled(false)
            return true
        }
        return false
    }

    override fun getCoordinatorLayout() = binding.root

    override fun showEmptyList() {
        binding.progressStateView.showMessage(R.string.play_lists_on_device_not_found, false)
    }

    override fun showEmptySearchResult() {
        binding.progressStateView.showMessage(R.string.no_matching_search_results_found)
    }

    override fun showList() {
        binding.progressStateView.hideAll()
    }

    override fun showLoading() {
        binding.progressStateView.showProgress()
    }

    override fun showErrorState(errorCommand: ErrorCommand) {
        binding.progressStateView.showMessage(errorCommand.message, true)
    }

    override fun updateList(lists: List<PlayList>) {
        adapter.submitList(lists)
    }

    override fun restoreListPosition(listPosition: ListPosition) {
        ViewUtils.scrollToPosition(layoutManager, listPosition)
    }

    override fun showConfirmDeletePlayListsDialog(playLists: Collection<PlayList>) {
        showConfirmDeleteDialog(requireContext(), playLists) {
            presenter.onDeletePlayListDialogConfirmed(playLists)
        }
    }

    override fun showPlayListsDeleteSuccess(playLists: Collection<PlayList>) {
        binding.listContainer.showSnackbar(getDeletedPlaylistsMessage(requireContext(), playLists))
    }

    override fun showDeletePlayListError(errorCommand: ErrorCommand) {
        binding.listContainer.showSnackbar(getString(R.string.play_list_delete_error, errorCommand.message))
    }

    override fun showEditPlayListNameDialog(playList: PlayList) {
        newRenamePlaylistDialog(playList.id).safeShow(childFragmentManager)
    }

    override fun launchPickFolderScreen() {
        pickFolderLauncher.safeLaunch(requireContext(),null)
    }

    override fun showPlaylistExportSuccess(playlists: List<PlayList>) {
        binding.listContainer.showSnackbar(getExportedPlaylistsMessage(requireContext(), playlists))
    }

    override fun launchPlayListScreen(playlistId: Long) {
        FragmentNavigation.from(parentFragmentManager)
            .addNewFragment(PlayListFragment.newInstance(playlistId))
    }

    override fun onPlaylistSelected(playlist: PlayList, position: Int) {
        adapter.setItemSelected(position)
    }

    override fun onPlaylistUnselected(playlist: PlayList, position: Int) {
        adapter.setItemUnselected(position)
    }

    override fun setItemsSelected(selected: Boolean) {
        adapter.setItemsSelected(selected)
    }

    override fun showSelectionMode(playlists: Set<PlayList>) {
        toolbar.showSelectionMode(playlists.size)

        if (playlists.isNotEmpty()) {
            var compositionsCount = 0
            playlists.forEach { playList -> compositionsCount += playList.compositionsCount }
            toolbar.updateSelectionMenu { item ->
                item.isVisible = isSelectionItemVisible(item.itemId, compositionsCount)
            }
        }
    }

    override fun showSelectPlayListDialog(
        playlists: Collection<PlayList>,
        closeMultiselect: Boolean,
    ) {
        val args = Bundle().apply {
            putLongArray(Constants.Arguments.IDS_ARG, playlists.toLongArray(PlayList::getId))
            putBoolean(Constants.Arguments.CLOSE_MULTISELECT_ARG, closeMultiselect)
        }
        choosePlayListDialogRunner.show(newChoosePlayListDialogFragment(args))
    }

    override fun sendCompositions(compositions: List<Composition>) {
        shareCompositions(this, compositions)
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
        val menu = getMenuItems(requireContext(), R.menu.play_list_menu) { item ->
            item.isVisible = isSelectionItemVisible(item.itemId, playList.compositionsCount)
        }

        PopupMenuWindow.showPopup(view, menu) { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_play -> presenter.onPlayPlaylistClicked(playList)
                R.id.menu_play_next -> presenter.onPlayNextPlaylistClicked(playList)
                R.id.menu_add_to_queue -> presenter.onAddToQueuePlaylistClicked(playList)
                R.id.menu_add_to_playlist -> presenter.onAddPlaylistToPlayListClicked(playList)
                R.id.menu_change_play_list_name -> {
                    presenter.onChangePlayListNameButtonClicked(playList)
                }
                R.id.menu_export_playlist -> {
                    presenter.onExportPlaylistClicked(playList)
                }
                R.id.menu_share -> presenter.onSharePlaylistClicked(playList)
                R.id.menu_delete_play_list -> {
                    presenter.onDeletePlayListButtonClicked(playList)
                }
            }
        }
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
            R.id.menu_play -> presenter.onPlayAllSelectedClicked()
            R.id.menu_select_all -> presenter.onSelectAllButtonClicked()
            R.id.menu_play_next -> presenter.onPlayNextSelectedPlaylistsClicked()
            R.id.menu_add_to_queue -> presenter.onAddToQueueSelectedPlaylistsClicked()
            R.id.menu_add_to_playlist -> presenter.onAddSelectedPlaylistsToPlayListClicked()
            R.id.menu_export_playlist -> presenter.onExportSelectedPlaylistsClicked()
            R.id.menu_share -> presenter.onShareSelectedPlaylistsClicked()
            R.id.menu_delete -> presenter.onDeleteSelectedPlaylistsButtonClicked()
        }
    }

    private fun onCreatePlayListButtonClicked() {
        CreatePlayListDialogFragment().safeShow(childFragmentManager)
    }

    private fun isSelectionItemVisible(@IdRes itemId: Int, compositionCount: Int): Boolean {
        return compositionCount != 0
                || (itemId != R.id.menu_play
                && itemId != R.id.menu_play_next
                && itemId != R.id.menu_add_to_queue
                && itemId != R.id.menu_add_to_playlist
                && itemId != R.id.menu_share)
    }

    private fun isPlaylistSwipeNotAllowed(viewHolder: RecyclerView.ViewHolder): Boolean {
        return (viewHolder as PlayListViewHolder).getPlaylist().compositionsCount == 0
    }

}