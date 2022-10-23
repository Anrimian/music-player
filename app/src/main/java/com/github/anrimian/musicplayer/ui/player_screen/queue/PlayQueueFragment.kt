package com.github.anrimian.musicplayer.ui.player_screen.queue

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.ActionMenuView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.anrimian.musicplayer.Constants
import com.github.anrimian.musicplayer.R
import com.github.anrimian.musicplayer.databinding.FragmentPlayQueueBinding
import com.github.anrimian.musicplayer.di.Components
import com.github.anrimian.musicplayer.domain.models.composition.Composition
import com.github.anrimian.musicplayer.domain.models.play_queue.PlayQueueItem
import com.github.anrimian.musicplayer.domain.models.playlist.PlayList
import com.github.anrimian.musicplayer.ui.common.dialogs.shareComposition
import com.github.anrimian.musicplayer.ui.common.dialogs.showConfirmDeleteDialog
import com.github.anrimian.musicplayer.ui.common.error.ErrorCommand
import com.github.anrimian.musicplayer.ui.common.format.FormatUtils
import com.github.anrimian.musicplayer.ui.common.format.MessagesUtils
import com.github.anrimian.musicplayer.ui.common.menu.PopupMenuWindow
import com.github.anrimian.musicplayer.ui.editor.common.DeleteErrorHandler
import com.github.anrimian.musicplayer.ui.editor.common.ErrorHandler
import com.github.anrimian.musicplayer.ui.editor.composition.newCompositionEditorIntent
import com.github.anrimian.musicplayer.ui.equalizer.EqualizerDialogFragment
import com.github.anrimian.musicplayer.ui.player_screen.PlayerFragment
import com.github.anrimian.musicplayer.ui.player_screen.queue.adapter.PlayQueueAdapter
import com.github.anrimian.musicplayer.ui.playlist_screens.choose.ChoosePlayListDialogFragment
import com.github.anrimian.musicplayer.ui.playlist_screens.create.CreatePlayListDialogFragment
import com.github.anrimian.musicplayer.ui.sleep_timer.SleepTimerDialogFragment
import com.github.anrimian.musicplayer.ui.utils.AndroidUtils
import com.github.anrimian.musicplayer.ui.utils.ViewUtils
import com.github.anrimian.musicplayer.ui.utils.colorFromAttr
import com.github.anrimian.musicplayer.ui.utils.fragments.DialogFragmentRunner
import com.github.anrimian.musicplayer.ui.utils.fragments.safeShow
import com.github.anrimian.musicplayer.ui.utils.views.menu.ActionMenuUtil
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.RecyclerViewUtils
import com.google.android.material.snackbar.Snackbar
import moxy.MvpAppCompatFragment
import moxy.ktx.moxyPresenter
import kotlin.math.abs

class PlayQueueFragment: MvpAppCompatFragment(), PlayQueueView {

    private val presenter by moxyPresenter {
        Components.getLibraryComponent().playQueuePresenter()
    }

    private lateinit var viewBinding: FragmentPlayQueueBinding

    private lateinit var clPlayQueueContainer: CoordinatorLayout
    private lateinit var acvToolbar: ActionMenuView
    private lateinit var tvQueueSubtitle: TextView

    private lateinit var playQueueAdapter: PlayQueueAdapter

    private lateinit var playQueueLayoutManager: LinearLayoutManager

    private lateinit var deletingErrorHandler: ErrorHandler

    private lateinit var createPlayListFragmentRunner: DialogFragmentRunner<CreatePlayListDialogFragment>
    private lateinit var choosePlayListFragmentRunner: DialogFragmentRunner<ChoosePlayListDialogFragment>

    private val secondScrollHandler = Handler(Looper.getMainLooper())
    private var currentPosition = -2 //for immediate first scroll

    private var isActionMenuEnabled = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewBinding = FragmentPlayQueueBinding.inflate(inflater, container, false)
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        clPlayQueueContainer = requireActivity().findViewById(R.id.cl_play_queue_container)
        acvToolbar = requireActivity().findViewById(R.id.acvPlayQueue)
        tvQueueSubtitle = requireActivity().findViewById(R.id.tvQueueSubtitle)

        playQueueLayoutManager = LinearLayoutManager(requireContext())
        viewBinding.rvPlayQueue.layoutManager = playQueueLayoutManager
        playQueueAdapter = PlayQueueAdapter(
            this,
            viewBinding.rvPlayQueue,
            presenter::onQueueItemClicked,
            this::onPlayItemMenuClicked,
            presenter::onQueueItemIconClicked
        )

        viewBinding.rvPlayQueue.adapter = playQueueAdapter
        val callback = FormatUtils.withSwipeToDelete(
            viewBinding.rvPlayQueue,
            AndroidUtils.getColorFromAttr(requireContext(), R.attr.listItemBottomBackground),
            presenter::onItemSwipedToDelete,
            ItemTouchHelper.START,
            R.drawable.ic_remove_from_queue,
            R.string.delete_from_queue
        )
        callback.setOnMovedListener(presenter::onItemMoved)
        callback.setOnStartDragListener { presenter.onDragStarted() }
        callback.setOnEndDragListener { presenter.onDragEnded() }
        val itemTouchHelper = ItemTouchHelper(callback)
        itemTouchHelper.attachToRecyclerView(viewBinding.rvPlayQueue)

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

    override fun onStart() {
        super.onStart()
        presenter.onStart()
    }

    override fun onStop() {
        super.onStop()
        //battery saving
        presenter.onStop()
    }

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

    override fun showCurrentQueueItem(item: PlayQueueItem?, showCover: Boolean) {
        ViewUtils.animateVisibility(viewBinding.rvPlayQueue, View.VISIBLE)
        if (item == null) {
            viewBinding.rvPlayQueue.contentDescription = getString(R.string.no_current_composition)
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

    override fun showDeleteCompositionMessage(compositionsToDelete: List<Composition>) {
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

    override fun showErrorMessage(errorCommand: ErrorCommand) {
        MessagesUtils.makeSnackbar(clPlayQueueContainer, errorCommand.message).show()
    }

    override fun showAddingToPlayListComplete(playList: PlayList?, compositions: List<Composition>) {
        val text = MessagesUtils.getAddToPlayListCompleteMessage(requireActivity(), playList, compositions)
        MessagesUtils.makeSnackbar(clPlayQueueContainer, text, Snackbar.LENGTH_SHORT).show()
    }

    override fun showAddingToPlayListError(errorCommand: ErrorCommand) {
        MessagesUtils.makeSnackbar(
            clPlayQueueContainer,
            getString(R.string.add_to_playlist_error_template, errorCommand.message),
            Snackbar.LENGTH_SHORT
        ).show()
    }

    override fun setPlayQueueCoversEnabled(isCoversEnabled: Boolean) {
        playQueueAdapter.setCoversEnabled(isCoversEnabled)
    }

    override fun updatePlayQueue(items: List<PlayQueueItem>) {
        tvQueueSubtitle.text = FormatUtils.formatCompositionsCount(requireContext(), items.size)
        playQueueAdapter.submitList(items)
        val bgColor = if (items.isEmpty()) {
            viewBinding.progressStateView.showMessage(R.string.play_queue_is_empty)
            requireContext().colorFromAttr(android.R.attr.colorBackground)
        } else {
            viewBinding.progressStateView.hideAll()
            requireContext().colorFromAttr(R.attr.listBackground)
        }
        viewBinding.root.setBackgroundColor(bgColor)
        isActionMenuEnabled = items.isNotEmpty()
        showMenuState()
    }

    //check by:
    //switch order mode(working)
    //new queue(so-so, but pass)
    //remove queue item(working)
    override fun scrollQueueToPosition(position: Int, isSmoothScrollAllowed: Boolean) {
        secondScrollHandler.removeCallbacksAndMessages(null)
        val positionDiff = abs(position - currentPosition)
        currentPosition = position
        if (RecyclerViewUtils.isPositionVisible(playQueueLayoutManager, position)) {
            return
        }

        val smooth = positionDiff == 1
                || position == playQueueLayoutManager.findFirstVisibleItemPosition()
                || position == playQueueLayoutManager.findLastVisibleItemPosition()
        RecyclerViewUtils.scrollToPosition(
            viewBinding.rvPlayQueue,
            playQueueLayoutManager,
            position,
            isSmoothScrollAllowed && smooth
        )

        //sometimes can not scroll, check twice
        secondScrollHandler.postDelayed({
            if (!RecyclerViewUtils.isPositionVisible(playQueueLayoutManager, position)) {
                RecyclerViewUtils.scrollToPosition(
                    viewBinding.rvPlayQueue,
                    playQueueLayoutManager,
                    position,
                    false
                )
            }
        }, 1600)
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

    private fun onPlayItemMenuClicked(view: View, playQueueItem: PlayQueueItem) {
        val composition = playQueueItem.composition
        PopupMenuWindow.showPopup(view, R.menu.play_queue_item_menu) { item ->
            when (item.itemId) {
                R.id.menu_add_to_playlist -> presenter.onAddQueueItemToPlayListButtonClicked(composition)
                R.id.menu_edit -> startActivity(newCompositionEditorIntent(requireContext(), composition.id))
                R.id.menu_show_in_folders -> onShowInFolderCompositionClicked(composition)
                R.id.menu_share -> shareComposition(this, composition)
                R.id.menu_delete_from_queue -> presenter.onDeleteQueueItemClicked(playQueueItem)
                R.id.menu_delete -> presenter.onDeleteCompositionButtonClicked(composition)
            }
        }
    }

    private fun onShowInFolderCompositionClicked(composition: Composition) {
        val parentFragment = requireActivity().supportFragmentManager.findFragmentById(R.id.main_activity_container)
        (parentFragment as? PlayerFragment)?.locateCompositionInFolders(composition)
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