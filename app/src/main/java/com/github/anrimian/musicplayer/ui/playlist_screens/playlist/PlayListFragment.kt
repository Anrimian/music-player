package com.github.anrimian.musicplayer.ui.playlist_screens.playlist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.annotation.MenuRes
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.anrimian.musicplayer.Constants
import com.github.anrimian.musicplayer.Constants.Tags
import com.github.anrimian.musicplayer.R
import com.github.anrimian.musicplayer.databinding.FragmentBaseFabListBinding
import com.github.anrimian.musicplayer.di.Components
import com.github.anrimian.musicplayer.domain.models.composition.Composition
import com.github.anrimian.musicplayer.domain.models.playlist.PlayList
import com.github.anrimian.musicplayer.domain.models.playlist.PlayListItem
import com.github.anrimian.musicplayer.domain.models.utils.ListPosition
import com.github.anrimian.musicplayer.ui.common.dialogs.composition.showCompositionPopupMenu
import com.github.anrimian.musicplayer.ui.common.dialogs.shareComposition
import com.github.anrimian.musicplayer.ui.common.dialogs.showConfirmDeleteDialog
import com.github.anrimian.musicplayer.ui.common.error.ErrorCommand
import com.github.anrimian.musicplayer.ui.common.format.FormatUtils
import com.github.anrimian.musicplayer.ui.common.format.MessagesUtils
import com.github.anrimian.musicplayer.ui.common.toolbar.AdvancedToolbar
import com.github.anrimian.musicplayer.ui.common.view.ViewUtils
import com.github.anrimian.musicplayer.ui.editor.common.DeleteErrorHandler
import com.github.anrimian.musicplayer.ui.editor.common.ErrorHandler
import com.github.anrimian.musicplayer.ui.editor.composition.newCompositionEditorIntent
import com.github.anrimian.musicplayer.ui.playlist_screens.choose.ChoosePlayListDialogFragment
import com.github.anrimian.musicplayer.ui.playlist_screens.playlist.adapter.PlayListItemAdapter
import com.github.anrimian.musicplayer.ui.playlist_screens.rename.RenamePlayListDialogFragment
import com.github.anrimian.musicplayer.ui.utils.AndroidUtils
import com.github.anrimian.musicplayer.ui.utils.fragments.BackButtonListener
import com.github.anrimian.musicplayer.ui.utils.fragments.DialogFragmentRunner
import com.github.anrimian.musicplayer.ui.utils.fragments.navigation.FragmentLayerListener
import com.github.anrimian.musicplayer.ui.utils.fragments.navigation.FragmentNavigation
import com.github.anrimian.musicplayer.ui.utils.fragments.safeShow
import com.github.anrimian.musicplayer.ui.utils.slidr.SlidrPanel
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.RecyclerViewUtils
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.touch_helper.drag_and_swipe.DragAndSwipeTouchHelperCallback
import com.google.android.material.snackbar.Snackbar
import moxy.MvpAppCompatFragment
import moxy.ktx.moxyPresenter


fun newPlayListFragment(playListId: Long) = PlayListFragment().apply {
    val args = Bundle()
    args.putLong(Constants.Arguments.PLAY_LIST_ID_ARG, playListId)
    arguments = args
}


class PlayListFragment : MvpAppCompatFragment(), PlayListView, BackButtonListener, FragmentLayerListener {

    private val presenter by moxyPresenter {
        Components.getPlayListComponent(getPlayListId()).playListPresenter()
    }

    private lateinit var viewBinding: FragmentBaseFabListBinding

    private lateinit var toolbar: AdvancedToolbar
    private lateinit var adapter: PlayListItemAdapter
    private lateinit var layoutManager: LinearLayoutManager

    private lateinit var choosePlaylistDialogRunner: DialogFragmentRunner<ChoosePlayListDialogFragment>

    private lateinit var deletingErrorHandler: ErrorHandler

    private lateinit var touchHelperCallback: DragAndSwipeTouchHelperCallback

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewBinding = FragmentBaseFabListBinding.inflate(inflater, container, false)
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        toolbar = requireActivity().findViewById(R.id.toolbar)

        touchHelperCallback = FormatUtils.withSwipeToDelete(
            viewBinding.recyclerView,
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
        itemTouchHelper.attachToRecyclerView(viewBinding.recyclerView)

        layoutManager = LinearLayoutManager(context)
        viewBinding.recyclerView.layoutManager = layoutManager
        RecyclerViewUtils.attachFastScroller(viewBinding.recyclerView, true)
        adapter = PlayListItemAdapter(
            this,
            viewBinding.recyclerView,
            presenter.isCoversEnabled(),
            this::onItemMenuClicked,
            presenter::onItemIconClicked
        )
        viewBinding.recyclerView.adapter = adapter

        viewBinding.fab.setOnClickListener { presenter.onPlayAllButtonClicked() }
        ViewUtils.onLongVibrationClick(viewBinding.fab, presenter::onChangeRandomModePressed)

        SlidrPanel.simpleSwipeBack(
            viewBinding.listContainer,
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

    override fun onFragmentMovedOnTop() {
        requireActivity().findViewById<AdvancedToolbar>(R.id.toolbar).setup { config ->
            config.setupSearch(presenter::onSearchTextChanged, presenter.getSearchText())
            config.setupOptionsMenu(R.menu.play_list_toolbar_menu, this::onOptionsItemClicked)
        }
        presenter.onFragmentMovedToTop()
    }

    override fun onStop() {
        super.onStop()
        presenter.onStop(ViewUtils.getListPosition(layoutManager))
    }

    override fun onBackPressed(): Boolean {
        if (toolbar.isInSearchMode) {
            toolbar.setSearchModeEnabled(false)
            return true
        }
        return false
    }

    override fun showEmptyList() {
        viewBinding.fab.visibility = View.GONE
        viewBinding.progressStateView.showMessage(R.string.play_list_is_empty, false)
    }

    override fun showEmptySearchResult() {
        viewBinding.fab.visibility = View.GONE
        viewBinding.progressStateView.showMessage(R.string.compositions_for_search_not_found, false)
    }

    override fun showList() {
        viewBinding.fab.visibility = View.VISIBLE
        viewBinding.progressStateView.hideAll()
    }

    override fun showLoading() {
        viewBinding.progressStateView.showProgress()
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
        toolbar.title = playList.name
        toolbar.subtitle =
            FormatUtils.formatCompositionsCount(requireContext(), playList.compositionsCount)
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
                viewBinding.listContainer,
                getString(R.string.delete_composition_error_template, errorCommand.message),
                Snackbar.LENGTH_SHORT
            ).show()
        }
    }

    override fun showDeletedCompositionMessage(compositionsToDelete: List<Composition>) {
        val text = MessagesUtils.getDeleteCompleteMessage(requireActivity(), compositionsToDelete)
        MessagesUtils.makeSnackbar(viewBinding.listContainer, text, Snackbar.LENGTH_SHORT).show()
    }

    override fun showAddingToPlayListError(errorCommand: ErrorCommand) {
        MessagesUtils.makeSnackbar(
            viewBinding.listContainer,
            getString(R.string.add_to_playlist_error_template, errorCommand.message),
            Snackbar.LENGTH_SHORT
        ).show()
    }

    override fun showAddingToPlayListComplete(playList: PlayList, compositions: List<Composition>) {
        val text =
            MessagesUtils.getAddToPlayListCompleteMessage(requireActivity(), playList, compositions)
        MessagesUtils.makeSnackbar(viewBinding.listContainer, text, Snackbar.LENGTH_SHORT).show()
    }

    override fun showDeleteItemError(errorCommand: ErrorCommand) {
        MessagesUtils.makeSnackbar(
            viewBinding.listContainer,
            getString(R.string.add_item_to_playlist_error_template, errorCommand.message),
            Snackbar.LENGTH_SHORT
        ).show()
    }

    override fun showDeleteItemCompleted(playList: PlayList, items: List<PlayListItem>) {
        val text =
            MessagesUtils.getDeletePlayListItemCompleteMessage(requireActivity(), playList, items)
        MessagesUtils.makeSnackbar(viewBinding.listContainer, text, Snackbar.LENGTH_LONG)
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
            viewBinding.listContainer,
            getString(R.string.play_list_deleted, playList.name),
            Snackbar.LENGTH_SHORT
        ).show()
    }

    override fun showDeletePlayListError(errorCommand: ErrorCommand) {
        MessagesUtils.makeSnackbar(
            viewBinding.listContainer,
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
        RenamePlayListDialogFragment.newInstance(playList.id).safeShow(childFragmentManager)
    }

    override fun showErrorMessage(errorCommand: ErrorCommand) {
        MessagesUtils.makeSnackbar(viewBinding.listContainer, errorCommand.message, Snackbar.LENGTH_SHORT)
            .show()
    }

    override fun onCompositionsAddedToPlayNext(compositions: List<Composition>) {
        val message = MessagesUtils.getPlayNextMessage(requireContext(), compositions)
        MessagesUtils.makeSnackbar(viewBinding.listContainer, message, Snackbar.LENGTH_SHORT).show()
    }

    override fun onCompositionsAddedToQueue(compositions: List<Composition>) {
        val message = MessagesUtils.getAddedToQueueMessage(requireContext(), compositions)
        MessagesUtils.makeSnackbar(viewBinding.listContainer, message, Snackbar.LENGTH_SHORT).show()
    }

    override fun showRandomMode(isRandomModeEnabled: Boolean) {
        FormatUtils.formatPlayAllButton(viewBinding.fab, isRandomModeEnabled)
    }

    override fun setDragEnabled(enabled: Boolean) {
        touchHelperCallback.setDragEnabled(enabled)
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
                startActivity(newCompositionEditorIntent(requireContext(), composition.id))
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
            R.id.menu_delete_play_list -> presenter.onDeletePlayListButtonClicked()
        }
    }

    private fun showEditorRequestDeniedMessage() {
        MessagesUtils.makeSnackbar(
            viewBinding.listContainer,
            R.string.android_r_edit_file_permission_denied,
            Snackbar.LENGTH_LONG
        ).show()
    }

}