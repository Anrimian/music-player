package com.github.anrimian.musicplayer.ui.playlist_screens.playlist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.MenuRes
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.anrimian.filesync.models.state.file.FileSyncState
import com.github.anrimian.musicplayer.Constants
import com.github.anrimian.musicplayer.Constants.Tags
import com.github.anrimian.musicplayer.R
import com.github.anrimian.musicplayer.data.models.folders.UriFileReference
import com.github.anrimian.musicplayer.databinding.FragmentBaseFabListBinding
import com.github.anrimian.musicplayer.di.Components
import com.github.anrimian.musicplayer.domain.models.composition.Composition
import com.github.anrimian.musicplayer.domain.models.composition.DeletedComposition
import com.github.anrimian.musicplayer.domain.models.playlist.PlayList
import com.github.anrimian.musicplayer.domain.models.playlist.PlayListItem
import com.github.anrimian.musicplayer.domain.models.utils.ListPosition
import com.github.anrimian.musicplayer.ui.common.dialogs.composition.showCompositionPopupMenu
import com.github.anrimian.musicplayer.ui.common.dialogs.shareComposition
import com.github.anrimian.musicplayer.ui.common.dialogs.showConfirmDeleteDialog
import com.github.anrimian.musicplayer.ui.common.error.ErrorCommand
import com.github.anrimian.musicplayer.ui.common.format.FormatUtils
import com.github.anrimian.musicplayer.ui.common.format.MessagesUtils
import com.github.anrimian.musicplayer.ui.common.format.getExportedPlaylistsMessage
import com.github.anrimian.musicplayer.ui.common.format.showSnackbar
import com.github.anrimian.musicplayer.ui.common.toolbar.AdvancedToolbar
import com.github.anrimian.musicplayer.ui.common.view.ViewUtils
import com.github.anrimian.musicplayer.ui.editor.common.DeleteErrorHandler
import com.github.anrimian.musicplayer.ui.editor.common.ErrorHandler
import com.github.anrimian.musicplayer.ui.editor.composition.CompositionEditorActivity
import com.github.anrimian.musicplayer.ui.equalizer.EqualizerDialogFragment
import com.github.anrimian.musicplayer.ui.library.common.library.BaseLibraryFragment
import com.github.anrimian.musicplayer.ui.library.common.library.BaseLibraryPresenter
import com.github.anrimian.musicplayer.ui.main.MainActivity
import com.github.anrimian.musicplayer.ui.playlist_screens.choose.ChoosePlayListDialogFragment
import com.github.anrimian.musicplayer.ui.playlist_screens.playlist.adapter.PlayListItemAdapter
import com.github.anrimian.musicplayer.ui.playlist_screens.rename.newRenamePlaylistDialog
import com.github.anrimian.musicplayer.ui.sleep_timer.SleepTimerDialogFragment
import com.github.anrimian.musicplayer.ui.utils.AndroidUtils
import com.github.anrimian.musicplayer.ui.utils.fragments.BackButtonListener
import com.github.anrimian.musicplayer.ui.utils.fragments.DialogFragmentRunner
import com.github.anrimian.musicplayer.ui.utils.fragments.navigation.FragmentNavigation
import com.github.anrimian.musicplayer.ui.utils.fragments.navigation.FragmentNavigationListener
import com.github.anrimian.musicplayer.ui.utils.fragments.safeShow
import com.github.anrimian.musicplayer.ui.utils.slidr.SlidrPanel
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.RecyclerViewUtils
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.touch_helper.drag_and_swipe.DragAndSwipeTouchHelperCallback
import com.google.android.material.snackbar.Snackbar
import moxy.ktx.moxyPresenter


class PlayListFragment : BaseLibraryFragment(), PlayListView, BackButtonListener,
    FragmentNavigationListener {

    companion object {
        fun newInstance(playListId: Long) = PlayListFragment().apply {
            arguments = Bundle().apply {
                putLong(Constants.Arguments.PLAY_LIST_ID_ARG, playListId)
            }
        }
    }

    private val presenter by moxyPresenter {
        Components.getPlayListComponent(getPlayListId()).playListPresenter()
    }

    private lateinit var binding: FragmentBaseFabListBinding

    private lateinit var toolbar: AdvancedToolbar
    private lateinit var adapter: PlayListItemAdapter
    private lateinit var layoutManager: LinearLayoutManager

    private lateinit var choosePlaylistDialogRunner: DialogFragmentRunner<ChoosePlayListDialogFragment>

    private lateinit var deletingErrorHandler: ErrorHandler

    private lateinit var touchHelperCallback: DragAndSwipeTouchHelperCallback

    private val pickFolderLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        if (uri != null) {
            presenter.onFolderForExportSelected(UriFileReference(uri))
        }
    }

    override fun getLibraryPresenter(): BaseLibraryPresenter<*> = presenter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentBaseFabListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        toolbar = requireActivity().findViewById(R.id.toolbar)

        touchHelperCallback = FormatUtils.withSwipeToDelete(
            binding.recyclerView,
            AndroidUtils.getColorFromAttr(requireContext(), R.attr.listItemBottomBackground),
            presenter::onItemSwipedToDelete,
            ItemTouchHelper.START,
            R.drawable.ic_playlist_remove,
            R.string.delete_from_play_list
        )
        touchHelperCallback.setOnMovedListener(presenter::onItemMoved)
        touchHelperCallback.setOnStartDragListener(presenter::onDragStarted)
        touchHelperCallback.setOnEndDragListener(presenter::onDragEnded)
        val itemTouchHelper = ItemTouchHelper(touchHelperCallback)
        itemTouchHelper.attachToRecyclerView(binding.recyclerView)

        layoutManager = LinearLayoutManager(context)
        binding.recyclerView.layoutManager = layoutManager
        RecyclerViewUtils.attachFastScroller(binding.recyclerView, true)
        adapter = PlayListItemAdapter(
            this,
            binding.recyclerView,
            presenter.isCoversEnabled(),
            this::onItemMenuClicked,
            presenter::onItemIconClicked
        )
        binding.recyclerView.adapter = adapter

        binding.fab.setOnClickListener { presenter.onPlayAllButtonClicked() }
        ViewUtils.onLongVibrationClick(binding.fab, presenter::onChangeRandomModePressed)

        SlidrPanel.simpleSwipeBack(
            binding.listContainer,
            this,
            toolbar::onStackFragmentSlided
        )

        val fm = childFragmentManager
        deletingErrorHandler = DeleteErrorHandler(
            this,
            presenter::onRetryFailedDeleteActionClicked,
            this::showEditorRequestDeniedMessage
        )

        choosePlaylistDialogRunner = DialogFragmentRunner(
            fm,
            Tags.SELECT_PLAYLIST_TAG
        ) { fragment -> fragment.setOnCompleteListener(presenter::onPlayListToAddingSelected) }
    }

    override fun onFragmentResumed() {
        requireActivity().findViewById<AdvancedToolbar>(R.id.toolbar).setup { config ->
            config.setupSearch(presenter::onSearchTextChanged, presenter.getSearchText())
            config.setupOptionsMenu(R.menu.play_list_toolbar_menu, this::onOptionsItemClicked)
        }
        presenter.onFragmentResumed()
    }

    override fun onStop() {
        super.onStop()
        presenter.onStop(ViewUtils.getListPosition(layoutManager))
    }

    override fun onBackPressed(): Boolean {
        if (toolbar.isInSearchMode()) {
            toolbar.setSearchModeEnabled(false)
            return true
        }
        return false
    }

    override fun getCoordinatorLayout() = binding.listContainer

    override fun showEmptyList() {
        binding.fab.visibility = View.GONE
        binding.progressStateView.showMessage(R.string.play_list_is_empty, false)
    }

    override fun showEmptySearchResult() {
        binding.fab.visibility = View.GONE
        binding.progressStateView.showMessage(R.string.no_matching_search_results_found, false)
    }

    override fun showList() {
        binding.fab.visibility = View.VISIBLE
        binding.progressStateView.hideAll()
    }

    override fun showLoading() {
        binding.progressStateView.showProgress()
    }

    override fun updateItemsList(list: List<PlayListItem>) {
        adapter.submitList(list)
    }

    override fun restoreListPosition(listPosition: ListPosition) {
        ViewUtils.scrollToPosition(layoutManager, listPosition)
    }

    override fun closeScreen() {
        FragmentNavigation.from(parentFragmentManager).goBack()
    }

    override fun showPlayListInfo(playList: PlayList) {
        toolbar.setTitle(playList.name)
        toolbar.setSubtitle(FormatUtils.formatPlaylistAdditionalInfo(
            requireContext(),
            playList,
            R.drawable.ic_description_text_circle_inverse
        ))
    }

    override fun showConfirmDeleteDialog(compositionsToDelete: List<Composition>) {
        showConfirmDeleteDialog(
            requireContext(),
            compositionsToDelete,
            presenter::onDeleteCompositionsDialogConfirmed
        )
    }

    override fun showSelectPlayListDialog() {
        choosePlaylistDialogRunner.show(ChoosePlayListDialogFragment())
    }

    override fun showDeleteCompositionError(errorCommand: ErrorCommand) {
        deletingErrorHandler.handleError(errorCommand) {
            MessagesUtils.makeSnackbar(
                binding.listContainer,
                getString(R.string.delete_composition_error_template, errorCommand.message),
                Snackbar.LENGTH_SHORT
            ).show()
        }
    }

    override fun showDeletedCompositionMessage(compositionsToDelete: List<DeletedComposition>) {
        val text = MessagesUtils.getDeleteCompleteMessage(requireActivity(), compositionsToDelete)
        MessagesUtils.makeSnackbar(binding.listContainer, text, Snackbar.LENGTH_SHORT).show()
    }

    override fun showDeleteItemError(errorCommand: ErrorCommand) {
        MessagesUtils.makeSnackbar(
            binding.listContainer,
            getString(R.string.add_item_to_playlist_error_template, errorCommand.message),
            Snackbar.LENGTH_SHORT
        ).show()
    }

    override fun showDeleteItemCompleted(playList: PlayList, items: List<PlayListItem>) {
        val text =
            MessagesUtils.getDeletePlayListItemCompleteMessage(requireActivity(), playList, items)
        MessagesUtils.makeSnackbar(binding.listContainer, text, Snackbar.LENGTH_LONG)
            .setAction(R.string.cancel, presenter::onRestoreRemovedItemClicked)
            .show()
    }

    override fun showConfirmDeletePlayListDialog(playList: PlayList) {
        showConfirmDeleteDialog(requireContext(), playList) {
            presenter.onDeletePlayListDialogConfirmed(playList)
        }
    }

    override fun showPlayListDeleteSuccess(playList: PlayList) {
        MessagesUtils.makeSnackbar(
            binding.listContainer,
            getString(R.string.play_list_deleted, playList.name),
            Snackbar.LENGTH_SHORT
        ).show()
    }

    override fun showDeletePlayListError(errorCommand: ErrorCommand) {
        MessagesUtils.makeSnackbar(
            binding.listContainer,
            getString(R.string.play_list_delete_error, errorCommand.message),
            Snackbar.LENGTH_SHORT
        ).show()
    }

    override fun notifyItemMoved(from: Int, to: Int) {
        adapter.notifyItemMoved(from, to)
    }

    override fun notifyItemRemoved(position: Int) {
        adapter.notifyItemRemoved(position)
    }

    override fun showEditPlayListNameDialog(playList: PlayList) {
        newRenamePlaylistDialog(playList.id).safeShow(childFragmentManager)
    }

    override fun showRandomMode(isRandomModeEnabled: Boolean) {
        FormatUtils.formatPlayAllButton(binding.fab, isRandomModeEnabled)
    }

    override fun setDragEnabled(enabled: Boolean) {
        touchHelperCallback.setDragEnabled(enabled)
    }

    override fun showFilesSyncState(states: Map<Long, FileSyncState>) {
        adapter.showFileSyncStates(states)
    }

    override fun showPlaylistExportSuccess(playlist: PlayList) {
        binding.listContainer.showSnackbar(
            getExportedPlaylistsMessage(requireContext(), listOf(playlist))
        )
    }

    private fun onItemMenuClicked(view: View, position: Int, playListItem: PlayListItem) {
        val composition = playListItem.composition
        showCompositionPopupMenu(view, R.menu.play_list_item_menu, composition) { item ->
            onCompositionActionSelected(playListItem, item.itemId, position)
        }
    }

    private fun onCompositionActionSelected(
        item: PlayListItem,
        @MenuRes menuItemId: Int,
        position: Int
    ) {
        val composition = item.composition
        when (menuItemId) {
            R.id.menu_play -> presenter.onPlayActionSelected(position)
            R.id.menu_play_next -> presenter.onPlayNextCompositionClicked(composition)
            R.id.menu_add_to_queue -> presenter.onAddToQueueCompositionClicked(composition)
            R.id.menu_add_to_playlist -> presenter.onAddToPlayListButtonClicked(composition)
            R.id.menu_edit ->
                startActivity(CompositionEditorActivity.newIntent(requireContext(), composition.id))
            R.id.menu_show_in_folders -> MainActivity.showInFolders(requireActivity(), composition)
            R.id.menu_share -> shareComposition(this, composition)
            R.id.menu_delete_from_play_list -> presenter.onDeleteFromPlayListButtonClicked(item)
            R.id.menu_delete -> presenter.onDeleteCompositionButtonClicked(composition)
        }
    }

    private fun getPlayListId() = requireArguments().getLong(Constants.Arguments.PLAY_LIST_ID_ARG)

    private fun onOptionsItemClicked(item: MenuItem) {
        when (item.itemId) {
            R.id.menu_search -> toolbar.setSearchModeEnabled(true)
            R.id.menu_change_play_list_name -> presenter.onChangePlayListNameButtonClicked()
            R.id.menu_export_playlist -> pickFolderLauncher.launch(null)
            R.id.menu_sleep_timer -> SleepTimerDialogFragment().safeShow(childFragmentManager)
            R.id.menu_equalizer -> EqualizerDialogFragment().safeShow(childFragmentManager)
            R.id.menu_delete_play_list -> presenter.onDeletePlayListButtonClicked()
        }
    }

    private fun showEditorRequestDeniedMessage() {
        MessagesUtils.makeSnackbar(
            binding.listContainer,
            R.string.android_r_edit_file_permission_denied,
            Snackbar.LENGTH_LONG
        ).show()
    }

}