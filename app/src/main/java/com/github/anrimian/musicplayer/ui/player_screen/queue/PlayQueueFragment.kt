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
import androidx.recyclerview.widget.RecyclerView
import com.github.anrimian.fsync.models.state.file.FileSyncState
import com.github.anrimian.musicplayer.AppConstants
import com.github.anrimian.musicplayer.R
import com.github.anrimian.musicplayer.databinding.FragmentPlayQueueBinding
import com.github.anrimian.musicplayer.di.Components
import com.github.anrimian.musicplayer.domain.models.composition.Composition
import com.github.anrimian.musicplayer.domain.models.composition.DeletedComposition
import com.github.anrimian.musicplayer.domain.models.play_queue.PlayQueueItem
import com.github.anrimian.musicplayer.ui.common.dialogs.shareComposition
import com.github.anrimian.musicplayer.ui.common.dialogs.showConfirmDeleteDialog
import com.github.anrimian.musicplayer.ui.common.error.ErrorCommand
import com.github.anrimian.musicplayer.ui.common.format.FormatUtils
import com.github.anrimian.musicplayer.ui.common.format.MessagesUtils
import com.github.anrimian.musicplayer.ui.common.format.showSnackbar
import com.github.anrimian.musicplayer.ui.common.menu.composition.showCompositionPopupMenu
import com.github.anrimian.musicplayer.ui.editor.common.DeleteErrorHandler
import com.github.anrimian.musicplayer.ui.editor.common.ErrorHandler
import com.github.anrimian.musicplayer.ui.editor.composition.CompositionEditorActivity
import com.github.anrimian.musicplayer.ui.equalizer.EqualizerDialogFragment
import com.github.anrimian.musicplayer.ui.library.common.library.BaseLibraryFragment
import com.github.anrimian.musicplayer.ui.library.common.library.BaseLibraryPresenter
import com.github.anrimian.musicplayer.ui.main.MainActivity
import com.github.anrimian.musicplayer.ui.player_screen.queue.adapter.PlayQueueAdapter
import com.github.anrimian.musicplayer.ui.playlists.choose.ChoosePlayListDialogFragment
import com.github.anrimian.musicplayer.ui.playlists.create.CreatePlayListDialogFragment
import com.github.anrimian.musicplayer.ui.sleep_timer.SleepTimerDialogFragment
import com.github.anrimian.musicplayer.ui.utils.ViewUtils
import com.github.anrimian.musicplayer.ui.utils.applyBottomInsets
import com.github.anrimian.musicplayer.ui.utils.attrColor
import com.github.anrimian.musicplayer.ui.utils.colorFromAttr
import com.github.anrimian.musicplayer.ui.utils.fragments.DialogFragmentRunner
import com.github.anrimian.musicplayer.ui.utils.fragments.safeShow
import com.github.anrimian.musicplayer.ui.utils.isLandscape
import com.github.anrimian.musicplayer.ui.utils.isTablet
import com.github.anrimian.musicplayer.ui.utils.views.menu.ActionMenuUtil
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.ListWindowPositionFetcher
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.windowedScrollToPosition
import com.google.android.material.snackbar.Snackbar
import moxy.ktx.moxyPresenter

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

    private lateinit var listWindowPositionFetcher: ListWindowPositionFetcher
    private var isManualScrollActive = false
    private var lastManualScrollTime = 0L
    private var scrollWindowTopOffset: Int = 0
    private var scrollWindowBottomOffset: Int = 0
    
    private var isActionMenuEnabled = false

    override fun getLibraryPresenter(): BaseLibraryPresenter<*> = presenter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentPlayQueueBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (requireContext().isLandscape() && !requireContext().isTablet()) {
            binding.rvPlayQueue.applyBottomInsets()
        }

        clPlayQueueContainer = requireActivity().findViewById(R.id.clPlayerPagerContainer)
        acvToolbar = requireActivity().findViewById(R.id.acvPlayQueue)
        tvQueueSubtitle = requireActivity().findViewById(R.id.tvQueueSubtitle)

        binding.progressStateView.onTryAgainClick(presenter::onLoadAgainQueueClicked)

        scrollWindowTopOffset = resources.getInteger(R.integer.play_queue_scroll_window_top_offset)
        scrollWindowBottomOffset = resources.getInteger(R.integer.play_queue_scroll_window_bottom_offset)
        playQueueLayoutManager = LinearLayoutManager(requireContext())
        listWindowPositionFetcher = ListWindowPositionFetcher(playQueueLayoutManager)
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

        binding.rvPlayQueue.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (isManualScrollActive && newState != RecyclerView.SCROLL_STATE_DRAGGING) {
                    lastManualScrollTime = System.currentTimeMillis()
                }
                isManualScrollActive = newState == RecyclerView.SCROLL_STATE_DRAGGING
            }
        })

        deletingErrorHandler = DeleteErrorHandler(
            this,
            presenter::onRetryFailedDeleteActionClicked,
            this::showEditorRequestDeniedMessage
        )

        createPlayListFragmentRunner = DialogFragmentRunner(
            childFragmentManager,
            AppConstants.Tags.CREATE_PLAYLIST_TAG
        ) { fragment -> fragment.setOnCompleteListener(presenter::onPlayListForAddingCreated) }

        choosePlayListFragmentRunner = DialogFragmentRunner(
            childFragmentManager,
            AppConstants.Tags.SELECT_PLAYLIST_TAG
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
        clPlayQueueContainer.showSnackbar(text)
    }

    override fun showDeleteCompositionError(errorCommand: ErrorCommand) {
        deletingErrorHandler.handleError(errorCommand) {
            clPlayQueueContainer.showSnackbar(
                getString(R.string.delete_composition_error_template, errorCommand.message),
            )
        }
    }

    override fun notifyItemMoved(from: Int, to: Int) {
        playQueueAdapter.notifyItemMoved(from, to)
    }

    override fun showDeletedItemMessage() {
        clPlayQueueContainer.showSnackbar(
            R.string.queue_item_removed,
            Snackbar.LENGTH_LONG,
            actionText = getString(R.string.cancel),
            action = presenter::onRestoreDeletedItemClicked
        )
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

    override fun scrollQueueToPosition(position: Int) {
        if (isManualScrollActive || lastManualScrollTime + AUTOSCROLL_ON_SCROLL_LOCK_MILLIS > System.currentTimeMillis()) {
            return
        }
        listWindowPositionFetcher.requestWindowPositions { first, last ->
            val ctx = context ?: return@requestWindowPositions
            windowedScrollToPosition(
                ctx,
                playQueueLayoutManager,
                playQueueAdapter,
                position,
                first,
                last,
                scrollWindowTopOffset,
                scrollWindowBottomOffset
            )
        }
    }

    override fun showFilesSyncState(states: Map<Long, FileSyncState>) {
        playQueueAdapter.showFileSyncStates(states)
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
                R.id.menu_show_in_folders -> MainActivity.showInFolders(requireActivity(), queueItem.id)
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
        clPlayQueueContainer.showSnackbar(
            R.string.android_r_edit_file_permission_denied,
            Snackbar.LENGTH_LONG
        )
    }

    private companion object {
        const val AUTOSCROLL_ON_SCROLL_LOCK_MILLIS = 120L
    }

}