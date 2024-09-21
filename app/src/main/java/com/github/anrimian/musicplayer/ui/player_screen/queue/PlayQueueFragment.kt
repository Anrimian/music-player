package com.github.anrimian.musicplayer.ui.player_screen.queue

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.ActionMenuView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.anrimian.filesync.models.state.file.FileSyncState
import com.github.anrimian.musicplayer.Constants
import com.github.anrimian.musicplayer.R
import com.github.anrimian.musicplayer.databinding.FragmentPlayQueueBinding
import com.github.anrimian.musicplayer.di.Components
import com.github.anrimian.musicplayer.domain.models.composition.Composition
import com.github.anrimian.musicplayer.domain.models.composition.DeletedComposition
import com.github.anrimian.musicplayer.domain.models.play_queue.PlayQueueItem
import com.github.anrimian.musicplayer.ui.common.dialogs.composition.showCompositionPopupMenu
import com.github.anrimian.musicplayer.ui.common.dialogs.shareComposition
import com.github.anrimian.musicplayer.ui.common.dialogs.showConfirmDeleteDialog
import com.github.anrimian.musicplayer.ui.common.error.ErrorCommand
import com.github.anrimian.musicplayer.ui.common.format.FormatUtils
import com.github.anrimian.musicplayer.ui.common.format.MessagesUtils
import com.github.anrimian.musicplayer.ui.editor.common.DeleteErrorHandler
import com.github.anrimian.musicplayer.ui.editor.common.ErrorHandler
import com.github.anrimian.musicplayer.ui.editor.composition.CompositionEditorActivity
import com.github.anrimian.musicplayer.ui.equalizer.EqualizerDialogFragment
import com.github.anrimian.musicplayer.ui.library.common.library.BaseLibraryFragment
import com.github.anrimian.musicplayer.ui.library.common.library.BaseLibraryPresenter
import com.github.anrimian.musicplayer.ui.main.MainActivity
import com.github.anrimian.musicplayer.ui.player_screen.queue.adapter.PlayQueueAdapter
import com.github.anrimian.musicplayer.ui.playlist_screens.choose.ChoosePlayListDialogFragment
import com.github.anrimian.musicplayer.ui.playlist_screens.create.CreatePlayListDialogFragment
import com.github.anrimian.musicplayer.ui.sleep_timer.SleepTimerDialogFragment
import com.github.anrimian.musicplayer.ui.utils.ViewUtils
import com.github.anrimian.musicplayer.ui.utils.attrColor
import com.github.anrimian.musicplayer.ui.utils.colorFromAttr
import com.github.anrimian.musicplayer.ui.utils.fragments.DialogFragmentRunner
import com.github.anrimian.musicplayer.ui.utils.fragments.safeShow
import com.github.anrimian.musicplayer.ui.utils.views.menu.ActionMenuUtil
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.RecyclerViewUtils
import com.google.android.material.snackbar.Snackbar
import moxy.ktx.moxyPresenter
import kotlin.math.abs

/**
 * Queue checklist:
 *  + Press shuffle button multiple times. -> We should be on the right position
 *  + Start playing with disabled random mode and with position in the middle.
 *    -> We should be on the right position
 *  + Remove current queue item. -> Scrolling should work correctly
 *  + OnStop, then press shuffle button from widget, open again.
 *    -> We should be on the right position
 *    Repeat several times.
 *  + Manually scroll to position, rotate screen -> We should be on the scrolled position
 */
class PlayQueueFragment: BaseLibraryFragment(), PlayQueueView {

    private val presenter by moxyPresenter {
        Components.getLibraryComponent().playQueuePresenter()
    }

    private lateinit var binding: FragmentPlayQueueBinding

    private lateinit var clPlayQueueContainer: CoordinatorLayout
    private lateinit var acvToolbar: ActionMenuView
    private lateinit var tvQueueSubtitle: TextView

    private lateinit var playQueueAdapter: PlayQueueAdapter

    private lateinit var playQueueLayoutManager: LinearLayoutManager

    private lateinit var deletingErrorHandler: ErrorHandler

    private lateinit var createPlayListFragmentRunner: DialogFragmentRunner<CreatePlayListDialogFragment>
    private lateinit var choosePlayListFragmentRunner: DialogFragmentRunner<ChoosePlayListDialogFragment>

    private var currentPosition = -2 //for immediate first scroll

    private var isActionMenuEnabled = false

    override fun getLibraryPresenter(): BaseLibraryPresenter<*> = presenter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPlayQueueBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        clPlayQueueContainer = requireActivity().findViewById(R.id.cl_play_queue_container)
        acvToolbar = requireActivity().findViewById(R.id.acvPlayQueue)
        tvQueueSubtitle = requireActivity().findViewById(R.id.tvQueueSubtitle)

        binding.progressStateView.onTryAgainClick(presenter::onLoadAgainQueueClicked)

        playQueueLayoutManager = LinearLayoutManager(requireContext())
        binding.rvPlayQueue.layoutManager = playQueueLayoutManager
        playQueueAdapter = PlayQueueAdapter(
            this,
            binding.rvPlayQueue,
            presenter::onQueueItemClicked,
            this::onPlayItemMenuClicked,
            presenter::onQueueItemIconClicked
        )

        binding.rvPlayQueue.adapter = playQueueAdapter
        val callback = FormatUtils.withSwipeToDelete(
            binding.rvPlayQueue,
            attrColor(R.attr.listItemBottomBackground),
            presenter::onItemSwipedToDelete,
            ItemTouchHelper.START,
            R.drawable.ic_remove_from_queue,
            R.string.delete_from_queue
        )
        callback.setOnMovedListener(presenter::onItemMoved)
        callback.setOnStartDragListener { presenter.onDragStarted() }
        callback.setOnEndDragListener { presenter.onDragEnded() }
        val itemTouchHelper = ItemTouchHelper(callback)
        itemTouchHelper.attachToRecyclerView(binding.rvPlayQueue)

        deletingErrorHandler = DeleteErrorHandler(
            this,
            presenter::onRetryFailedDeleteActionClicked,
            this::showEditorRequestDeniedMessage
        )

        createPlayListFragmentRunner = DialogFragmentRunner(
            childFragmentManager,
            Constants.Tags.CREATE_PLAYLIST_TAG
        ) { fragment -> fragment.setOnCompleteListener(presenter::onPlayListForAddingCreated) }

        choosePlayListFragmentRunner = DialogFragmentRunner(
            childFragmentManager,
            Constants.Tags.SELECT_PLAYLIST_TAG
        ) { fragment -> fragment.setOnCompleteListener(presenter::onPlayListForAddingSelected) }
    }

    override fun getCoordinatorLayout() = clPlayQueueContainer

    override fun showPlayerState(isPlaying: Boolean) {
        playQueueAdapter.showPlaying(isPlaying)
    }

    override fun setMenuVisibility(menuVisible: Boolean) {
        super.setMenuVisibility(menuVisible)
        if (menuVisible) {
            ActionMenuUtil.setupMenu(acvToolbar, R.menu.play_queue_menu, this::onQueueMenuItemClicked)
            showMenuState()
        }
    }

    override fun showCurrentQueueItem(item: PlayQueueItem?) {
        ViewUtils.animateVisibility(binding.rvPlayQueue, View.VISIBLE)
        if (item == null) {
            binding.rvPlayQueue.contentDescription = getString(R.string.no_current_composition)
        } else {
            playQueueAdapter.onCurrentItemChanged(item)
        }
    }

    override fun showSelectPlayListDialog() {
        choosePlayListFragmentRunner.show(ChoosePlayListDialogFragment())
    }

    override fun showConfirmDeleteDialog(compositionsToDelete: List<Composition>) {
        showConfirmDeleteDialog(requireContext(), compositionsToDelete) {
            presenter.onDeleteCompositionsDialogConfirmed(compositionsToDelete)
        }
    }

    override fun showDeleteCompositionMessage(compositionsToDelete: List<DeletedComposition>) {
        val text = MessagesUtils.getDeleteCompleteMessage(requireActivity(), compositionsToDelete)
        MessagesUtils.makeSnackbar(clPlayQueueContainer, text, Snackbar.LENGTH_SHORT).show()
    }

    override fun showDeleteCompositionError(errorCommand: ErrorCommand) {
        deletingErrorHandler.handleError(errorCommand) {
            MessagesUtils.makeSnackbar(
                clPlayQueueContainer,
                getString(R.string.delete_composition_error_template, errorCommand.message),
                Snackbar.LENGTH_SHORT
            ).show()
        }
    }

    override fun notifyItemMoved(from: Int, to: Int) {
        playQueueAdapter.notifyItemMoved(from, to)
    }

    override fun showDeletedItemMessage() {
        MessagesUtils.makeSnackbar(
            clPlayQueueContainer,
            R.string.queue_item_removed,
            Snackbar.LENGTH_LONG
        ).setAction(R.string.cancel, presenter::onRestoreDeletedItemClicked)
            .show()
    }

    override fun setPlayQueueCoversEnabled(isCoversEnabled: Boolean) {
        playQueueAdapter.setCoversEnabled(isCoversEnabled)
    }

    override fun showList(itemsCount: Int) {
        tvQueueSubtitle.text = FormatUtils.formatCompositionsCount(requireContext(), itemsCount)

        val isEmpty = itemsCount == 0
        val bgColor = if (isEmpty) {
            binding.progressStateView.showMessage(R.string.play_queue_is_empty)
            requireContext().colorFromAttr(android.R.attr.colorBackground)
        } else {
            binding.progressStateView.hideAll()
            requireContext().colorFromAttr(R.attr.listBackground)
        }
        binding.root.setBackgroundColor(bgColor)

        isActionMenuEnabled = !isEmpty
        showMenuState()
    }

    override fun showListError(errorCommand: ErrorCommand) {
        binding.progressStateView.showMessage(errorCommand.message, true)
    }

    override fun updatePlayQueue(items: List<PlayQueueItem>?) {
        playQueueAdapter.submitList(items)
    }

    override fun scrollQueueToPosition(position: Int, isSmoothScrollAllowed: Boolean) {
        //hypothetically mis scroll still can happen when we get 2 fast list updates with scroll
        playQueueAdapter.runSafeAction { scrollToPosition(position, isSmoothScrollAllowed) }
    }

    override fun showFilesSyncState(states: Map<Long, FileSyncState>) {
        playQueueAdapter.showFileSyncStates(states)
    }

    private fun scrollToPosition(position: Int, isSmoothScrollAllowed: Boolean) {
        val positionDiff = abs(position - currentPosition)
        currentPosition = position
        if (RecyclerViewUtils.isPositionVisible(playQueueLayoutManager, position)) {
            return
        }

        val smooth = positionDiff == 1
                || position == playQueueLayoutManager.findFirstVisibleItemPosition()
                || position == playQueueLayoutManager.findLastVisibleItemPosition()
        RecyclerViewUtils.scrollToPosition(
            binding.rvPlayQueue,
            playQueueLayoutManager,
            position,
            isSmoothScrollAllowed && smooth
        )
    }

    private fun onQueueMenuItemClicked(menuItem: MenuItem) {
        when (menuItem.itemId) {
            R.id.menu_save_as_playlist -> {
                createPlayListFragmentRunner.show(CreatePlayListDialogFragment())
            }
            R.id.menu_sleep_timer -> SleepTimerDialogFragment().safeShow(childFragmentManager)
            R.id.menu_equalizer -> EqualizerDialogFragment().safeShow(childFragmentManager)
            R.id.menu_clear_play_queue -> presenter.onClearPlayQueueClicked()
        }
    }

    private fun onPlayItemMenuClicked(view: View, queueItem: PlayQueueItem) {
        showCompositionPopupMenu(view, R.menu.play_queue_item_menu, queueItem) { item ->
            when (item.itemId) {
                R.id.menu_add_to_playlist -> presenter.onAddQueueItemToPlayListButtonClicked(queueItem)
                R.id.menu_edit -> startActivity(CompositionEditorActivity.newIntent(requireContext(), queueItem.id))
                R.id.menu_show_in_folders -> MainActivity.showInFolders(requireActivity(), queueItem)
                R.id.menu_share -> shareComposition(this, queueItem)
                R.id.menu_delete_from_queue -> presenter.onDeleteQueueItemClicked(queueItem)
                R.id.menu_delete -> presenter.onDeleteCompositionButtonClicked(queueItem)
            }
        }
    }

    private fun showMenuState() {
        val menu = acvToolbar.menu
        menu.findItem(R.id.menu_save_as_playlist)?.isEnabled = isActionMenuEnabled
        menu.findItem(R.id.menu_clear_play_queue)?.isEnabled = isActionMenuEnabled
    }

    private fun showEditorRequestDeniedMessage() {
        MessagesUtils.makeSnackbar(
            clPlayQueueContainer,
            R.string.android_r_edit_file_permission_denied,
            Snackbar.LENGTH_LONG
        ).show()
    }
}