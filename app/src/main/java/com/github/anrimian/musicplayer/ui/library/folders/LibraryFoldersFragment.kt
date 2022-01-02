package com.github.anrimian.musicplayer.ui.library.folders

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.annotation.AttrRes
import androidx.annotation.MenuRes
import androidx.annotation.StringRes
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.anrimian.musicplayer.Constants
import com.github.anrimian.musicplayer.Constants.Tags
import com.github.anrimian.musicplayer.R
import com.github.anrimian.musicplayer.databinding.FragmentLibraryFoldersBinding
import com.github.anrimian.musicplayer.di.Components
import com.github.anrimian.musicplayer.domain.models.composition.Composition
import com.github.anrimian.musicplayer.domain.models.composition.CurrentComposition
import com.github.anrimian.musicplayer.domain.models.folders.FileSource
import com.github.anrimian.musicplayer.domain.models.folders.FolderFileSource
import com.github.anrimian.musicplayer.domain.models.folders.IgnoredFolder
import com.github.anrimian.musicplayer.domain.models.order.Order
import com.github.anrimian.musicplayer.domain.models.order.OrderType
import com.github.anrimian.musicplayer.domain.models.playlist.PlayList
import com.github.anrimian.musicplayer.domain.models.utils.ListPosition
import com.github.anrimian.musicplayer.ui.common.dialogs.DialogUtils
import com.github.anrimian.musicplayer.ui.common.dialogs.composition.CompositionActionDialogFragment
import com.github.anrimian.musicplayer.ui.common.dialogs.input.InputTextDialogFragment
import com.github.anrimian.musicplayer.ui.common.error.ErrorCommand
import com.github.anrimian.musicplayer.ui.common.format.FormatUtils
import com.github.anrimian.musicplayer.ui.common.format.MessagesUtils
import com.github.anrimian.musicplayer.ui.common.menu.PopupMenuWindow
import com.github.anrimian.musicplayer.ui.common.toolbar.AdvancedToolbar
import com.github.anrimian.musicplayer.ui.common.view.ViewUtils
import com.github.anrimian.musicplayer.ui.editor.common.DeleteErrorHandler
import com.github.anrimian.musicplayer.ui.editor.common.ErrorHandler
import com.github.anrimian.musicplayer.ui.editor.composition.newCompositionEditorIntent
import com.github.anrimian.musicplayer.ui.equalizer.EqualizerDialogFragment
import com.github.anrimian.musicplayer.ui.library.common.order.SelectOrderDialogFragment
import com.github.anrimian.musicplayer.ui.library.folders.adapter.MusicFileSourceAdapter
import com.github.anrimian.musicplayer.ui.library.folders.wrappers.HeaderViewWrapper
import com.github.anrimian.musicplayer.ui.playlist_screens.choose.ChoosePlayListDialogFragment
import com.github.anrimian.musicplayer.ui.playlist_screens.choose.newChoosePlayListDialogFragment
import com.github.anrimian.musicplayer.ui.settings.folders.ExcludedFoldersFragment
import com.github.anrimian.musicplayer.ui.sleep_timer.SleepTimerDialogFragment
import com.github.anrimian.musicplayer.ui.utils.ViewUtils.animateVisibility
import com.github.anrimian.musicplayer.ui.utils.dialogs.ProgressDialogFragment
import com.github.anrimian.musicplayer.ui.utils.fragments.BackButtonListener
import com.github.anrimian.musicplayer.ui.utils.fragments.DialogFragmentDelayRunner
import com.github.anrimian.musicplayer.ui.utils.fragments.DialogFragmentRunner
import com.github.anrimian.musicplayer.ui.utils.fragments.navigation.FragmentLayerListener
import com.github.anrimian.musicplayer.ui.utils.fragments.navigation.FragmentNavigation
import com.github.anrimian.musicplayer.ui.utils.slidr.SlidrPanel
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.RecyclerViewUtils
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.touch_helper.short_swipe.ShortSwipeCallback
import com.google.android.material.snackbar.Snackbar
import com.r0adkll.slidr.model.SlidrConfig
import com.r0adkll.slidr.model.SlidrPosition
import io.reactivex.rxjava3.disposables.CompositeDisposable
import moxy.MvpAppCompatFragment
import moxy.ktx.moxyPresenter

/**
 * Created on 23.10.2017.
 */

fun newFolderFragment(folderId: Long?): LibraryFoldersFragment {
    val args = Bundle()
    args.putLong(Constants.Arguments.ID_ARG, folderId ?: 0)
    val fragment = LibraryFoldersFragment()
    fragment.arguments = args
    return fragment
}

class LibraryFoldersFragment : MvpAppCompatFragment(), LibraryFoldersView, BackButtonListener,
    FragmentLayerListener {

    private val presenter by moxyPresenter {
        Components.getLibraryFolderComponent(getFolderId()).storageLibraryPresenter()
    }
    private lateinit var viewBinding: FragmentLibraryFoldersBinding

    private val fragmentDisposable = CompositeDisposable()

    private lateinit var toolbar: AdvancedToolbar
    private lateinit var adapter: MusicFileSourceAdapter
    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var headerViewWrapper: HeaderViewWrapper

    private lateinit var filenameDialogFragmentRunner: DialogFragmentRunner<InputTextDialogFragment>
    private lateinit var newFolderDialogFragmentRunner: DialogFragmentRunner<InputTextDialogFragment>
    private lateinit var compositionActionDialogRunner: DialogFragmentRunner<CompositionActionDialogFragment>
    private lateinit var choosePlaylistForFolderDialogRunner: DialogFragmentRunner<ChoosePlayListDialogFragment>
    private lateinit var progressDialogRunner: DialogFragmentDelayRunner

    private lateinit var editorErrorHandler: ErrorHandler
    private lateinit var deletingErrorHandler: ErrorHandler

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewBinding = FragmentLibraryFoldersBinding.inflate(inflater, container, false)
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        toolbar = requireActivity().findViewById(R.id.toolbar)
        fragmentDisposable.add(toolbar.getSelectionModeObservable()
            .subscribe(this::onSelectionModeChanged)
        )
        viewBinding.progressStateView.onTryAgainClick { presenter.onTryAgainButtonClicked() }

        layoutManager = LinearLayoutManager(context)
        viewBinding.recyclerView.layoutManager = layoutManager
        RecyclerViewUtils.attachFastScroller(viewBinding.recyclerView, true)
        adapter = MusicFileSourceAdapter(viewBinding.recyclerView,
            presenter.getSelectedFiles(),
            presenter.getSelectedMoveFiles(),
            presenter::onCompositionClicked,
            presenter::onFolderClicked,
            presenter::onItemLongClick,
            this::onFolderMenuClicked,
            presenter::onCompositionIconClicked
        ) { _, musicFileSource -> presenter.onCompositionMenuClick(musicFileSource) }
        viewBinding.recyclerView.adapter = adapter

        val callback = ShortSwipeCallback(requireContext(),
            R.drawable.ic_play_next,
            R.string.play_next,
            swipeCallback = presenter::onPlayNextSourceClicked
        )
        val itemTouchHelper = ItemTouchHelper(callback)
        itemTouchHelper.attachToRecyclerView(viewBinding.recyclerView)

        headerViewWrapper = HeaderViewWrapper(viewBinding.headerContainer)
        headerViewWrapper.setOnClickListener { presenter.onBackPathButtonClicked() }

        viewBinding.fab.setOnClickListener { presenter.onPlayAllButtonClicked() }

        viewBinding.vgFileMenu.visibility = View.INVISIBLE
        viewBinding.vgMoveFileMenu.visibility = View.INVISIBLE
        FormatUtils.formatLinkedFabView(viewBinding.vgFileMenu, viewBinding.fab)
        FormatUtils.formatLinkedFabView(viewBinding.vgMoveFileMenu, viewBinding.fab)

        viewBinding.ivCut.setOnClickListener { presenter.onMoveSelectedFoldersButtonClicked() }
        viewBinding.ivCopy.setOnClickListener { presenter.onCopySelectedFoldersButtonClicked() }

        //maybe will be moved to root fragment later
        view.findViewById<View>(R.id.iv_close)
            .setOnClickListener { presenter.onCloseMoveMenuClicked() }
        view.findViewById<View>(R.id.iv_paste)
            .setOnClickListener { presenter.onPasteButtonClicked() }
        view.findViewById<View>(R.id.iv_paste_in_new_folder)
            .setOnClickListener { presenter.onPasteInNewFolderButtonClicked() }

        val fm = childFragmentManager
        val orderFragment = fm.findFragmentByTag(Tags.ORDER_TAG) as SelectOrderDialogFragment?
        orderFragment?.setOnCompleteListener(presenter::onOrderSelected)
        val playListDialog = fm.findFragmentByTag(Tags.SELECT_PLAYLIST_TAG) as ChoosePlayListDialogFragment?
        playListDialog?.setOnCompleteListener(presenter::onPlayListToAddingSelected)
        editorErrorHandler = ErrorHandler(
            fm,
            presenter::onRetryFailedEditActionClicked,
            this::showEditorRequestDeniedMessage
        )
        deletingErrorHandler = DeleteErrorHandler(
            fm,
            presenter::onRetryFailedDeleteActionClicked,
            this::showEditorRequestDeniedMessage
        )
        choosePlaylistForFolderDialogRunner = DialogFragmentRunner(
            fm,
            Tags.SELECT_PLAYLIST_FOR_FOLDER_TAG
        ) { fragment ->
            fragment.setComplexCompleteListener { playlist, bundle ->
                val folderId = bundle.getLong(Constants.Arguments.ID_ARG)
                presenter.onPlayListForFolderSelected(folderId, playlist)
            }
        }
        compositionActionDialogRunner = DialogFragmentRunner(
            fm,
            Tags.COMPOSITION_ACTION_TAG
        ) { f -> f.setOnCompleteListener(this::onCompositionActionSelected) }
        filenameDialogFragmentRunner = DialogFragmentRunner(
            fm,
            Tags.FILE_NAME_TAG
        ) { fragment ->
            fragment.setComplexCompleteListener { name, extra ->
                presenter.onNewFolderNameEntered(extra.getLong(Constants.Arguments.ID_ARG), name)
            }
        }
        newFolderDialogFragmentRunner = DialogFragmentRunner(
            fm,
            Tags.NEW_FOLDER_NAME_TAG
        ) { fragment -> fragment.setOnCompleteListener(presenter::onNewFileNameForPasteEntered) }
        progressDialogRunner = DialogFragmentDelayRunner(fm, Tags.PROGRESS_DIALOG_TAG)

        if (getFolderId() != null) {
            val slidrConfig = SlidrConfig.Builder().position(SlidrPosition.LEFT).build()
            SlidrPanel.replace(
                viewBinding.contentContainer,
                {
                    toolbar.showSelectionMode(0)
                    FragmentNavigation.from(parentFragmentManager).goBack()
                },
                slidrConfig
            )
        }
    }

    override fun onStart() {
        super.onStart()
        presenter.onStart()
    }

    override fun onStop() {
        super.onStop()
        presenter.onStop(ViewUtils.getListPosition(layoutManager))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        fragmentDisposable.clear()
    }

    override fun onFragmentMovedOnTop() {
        presenter.onFragmentDisplayed()
        val act: Activity = requireActivity()
        val toolbar: AdvancedToolbar = act.findViewById(R.id.toolbar)
        toolbar.setupSelectionModeMenu(R.menu.library_folders_selection_menu, this::onActionModeItemClicked)
        toolbar.setupOptionsMenu(R.menu.library_files_menu, this::onOptionsItemClicked)
    }

    override fun updateList(list: List<FileSource>) {
        adapter.submitList(list)
    }

    override fun showFolderInfo(folder: FolderFileSource) {
        headerViewWrapper.setVisible(true)
        headerViewWrapper.bind(folder)
    }

    override fun hideFolderInfo() {
        headerViewWrapper.setVisible(false)
    }

    override fun showEmptyList() {
        viewBinding.fab.visibility = View.GONE
        viewBinding.progressStateView.showMessage(
            R.string.compositions_on_device_not_found,
            false
        )
    }

    override fun showEmptySearchResult() {
        viewBinding.fab.visibility = View.GONE
        viewBinding.progressStateView.showMessage(
            R.string.compositions_and_folders_for_search_not_found,
            false
        )
    }

    override fun showList() {
        viewBinding.fab.visibility = View.VISIBLE
        viewBinding.progressStateView.hideAll()
    }

    override fun showLoading() {
        viewBinding.progressStateView.showProgress()
    }

    override fun showError(errorCommand: ErrorCommand) {
        viewBinding.progressStateView.showMessage(errorCommand.message, true)
    }

    override fun goBackToParentFolderScreen() {
        FragmentNavigation.from(parentFragmentManager).goBack()
    }

    override fun onBackPressed(): Boolean {
        if (toolbar.isInActionMode) {
            presenter.onSelectionModeBackPressed()
            return true
        }
        if (toolbar.isInSearchMode) {
            toolbar.setSearchModeEnabled(false)
            return true
        }
        if (getFolderId() != null) {
            presenter.onBackPathButtonClicked()
            return true
        }
        return false
    }

    override fun showSearchMode(show: Boolean) {
        toolbar.setSearchModeEnabled(show)
    }

    override fun showAddingToPlayListError(errorCommand: ErrorCommand) {
        MessagesUtils.makeSnackbar(
            viewBinding.listContainer,
            getString(R.string.add_to_playlist_error_template, errorCommand.message),
            Snackbar.LENGTH_SHORT
        ).show()
    }

    override fun showAddingToPlayListComplete(playList: PlayList, compositions: List<Composition>) {
        val text = MessagesUtils.getAddToPlayListCompleteMessage(requireActivity(), playList, compositions)
        MessagesUtils.makeSnackbar(viewBinding.listContainer, text, Snackbar.LENGTH_SHORT).show()
    }

    override fun showSelectPlayListForFolderDialog(folder: FolderFileSource) {
        val bundle = Bundle()
        bundle.putLong(Constants.Arguments.ID_ARG, folder.id)
        val dialog = newChoosePlayListDialogFragment(bundle)
        choosePlaylistForFolderDialogRunner.show(dialog)
    }

    override fun showSelectOrderScreen(folderOrder: Order) {
        val fragment = SelectOrderDialogFragment.newInstance(
            folderOrder,
            true,
            OrderType.NAME,
            OrderType.FILE_NAME,
            OrderType.ADD_TIME,
            OrderType.DURATION,
            OrderType.SIZE
        )
        fragment.setOnCompleteListener(presenter::onOrderSelected)
        fragment.show(childFragmentManager, Tags.ORDER_TAG)
    }

    override fun showSelectPlayListDialog() {
        val dialog = ChoosePlayListDialogFragment()
        dialog.setOnCompleteListener(presenter::onPlayListToAddingSelected)
        dialog.show(childFragmentManager, Tags.SELECT_PLAYLIST_TAG)
    }

    override fun showConfirmDeleteDialog(compositionsToDelete: List<Composition>) {
        DialogUtils.showConfirmDeleteDialog(
            requireContext(),
            compositionsToDelete,
            presenter::onDeleteCompositionsDialogConfirmed
        )
    }

    override fun showConfirmDeleteDialog(folder: FolderFileSource) {
        DialogUtils.showConfirmDeleteDialog(
            requireContext(),
            folder
        ) { presenter.onDeleteFolderDialogConfirmed(folder) }
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

    override fun showDeleteCompositionMessage(compositionsToDelete: List<Composition>) {
        val text = MessagesUtils.getDeleteCompleteMessage(requireActivity(), compositionsToDelete)
        MessagesUtils.makeSnackbar(viewBinding.listContainer, text, Snackbar.LENGTH_SHORT).show()
    }

    override fun sendCompositions(compositions: List<Composition>) {
        DialogUtils.shareCompositions(requireContext(), compositions)
    }

    override fun showReceiveCompositionsForSendError(errorCommand: ErrorCommand) {
        MessagesUtils.makeSnackbar(
            viewBinding.listContainer,
            getString(R.string.can_not_receive_file_for_send, errorCommand.message),
            Snackbar.LENGTH_SHORT
        ).show()
    }

    override fun goToMusicStorageScreen(folderId: Long) {
        FragmentNavigation.from(parentFragmentManager).addNewFragment(newFolderFragment(folderId))
    }

    override fun showCompositionActionDialog(composition: Composition) {
        @AttrRes val statusBarColor = if (toolbar.isInActionMode) {
            R.attr.actionModeStatusBarColor
        } else {
            android.R.attr.statusBarColor
        }
        val fragment = CompositionActionDialogFragment.newInstance(
            composition,
            R.menu.composition_actions_menu,
            statusBarColor
        )
        compositionActionDialogRunner.show(fragment)
    }

    override fun showErrorMessage(errorCommand: ErrorCommand) {
        editorErrorHandler.handleError(errorCommand) {
            MessagesUtils.makeSnackbar(
                viewBinding.listContainer, errorCommand.message, Snackbar.LENGTH_LONG
            ).show()
        }
    }

    override fun setDisplayCoversEnabled(isCoversEnabled: Boolean) {
        adapter.setCoversEnabled(isCoversEnabled)
    }

    override fun showInputFolderNameDialog(folder: FolderFileSource) {
        val extra = Bundle()
        extra.putLong(Constants.Arguments.ID_ARG, folder.id)
        val fragment = InputTextDialogFragment.newInstance(
            R.string.rename_folder,
            R.string.change,
            R.string.cancel,
            R.string.folder_name,
            folder.name,
            false,
            extra
        )
        filenameDialogFragmentRunner.show(fragment)
    }

    override fun showInputNewFolderNameDialog() {
        val fragment = InputTextDialogFragment.newInstance(
            R.string.new_folder,
            R.string.create,
            R.string.cancel,
            R.string.folder_name,
            null,
            false
        )
        newFolderDialogFragmentRunner.show(fragment)
    }

    override fun showSelectionMode(count: Int) {
        toolbar.showSelectionMode(count)
    }

    override fun onItemSelected(item: FileSource, position: Int) {
        adapter.setItemSelected(position)
    }

    override fun onItemUnselected(item: FileSource, position: Int) {
        adapter.setItemUnselected(position)
    }

    override fun setItemsSelected(selected: Boolean) {
        adapter.setItemsSelected(selected)
    }

    override fun updateMoveFilesList() {
        adapter.updateItemsToMove()
    }

    override fun showMoveFileMenu(show: Boolean) {
        animateVisibility(
            viewBinding.vgMoveFileMenu,
            if (show) View.VISIBLE else View.INVISIBLE
        )
    }

    override fun showCurrentComposition(currentComposition: CurrentComposition) {
        adapter.showCurrentComposition(currentComposition)
    }

    override fun restoreListPosition(listPosition: ListPosition) {
        ViewUtils.scrollToPosition(layoutManager, listPosition)
    }

    override fun showAddedIgnoredFolderMessage(folder: IgnoredFolder) {
        val message = getString(R.string.ignored_folder_added, folder.relativePath)
        MessagesUtils.makeSnackbar(viewBinding.listContainer, message, Snackbar.LENGTH_LONG)
            .setAction(R.string.cancel, presenter::onRemoveIgnoredFolderClicked)
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

    override fun hideProgressDialog() {
        progressDialogRunner.cancel()
    }

    override fun showMoveProgress() {
        showProgressDialog(R.string.move_progress)
    }

    override fun showDeleteProgress() {
        showProgressDialog(R.string.delete_progress)
    }

    override fun showRenameProgress() {
        showProgressDialog(R.string.rename_progress)
    }

    private fun showProgressDialog(@StringRes resId: Int) {
        progressDialogRunner.show(ProgressDialogFragment.newInstance(resId))
    }

    private fun onSelectionModeChanged(enabled: Boolean) {
        animateVisibility(
            viewBinding.vgFileMenu,
            if (enabled) View.VISIBLE else View.INVISIBLE
        )
    }

    private fun onCompositionActionSelected(composition: Composition, @MenuRes menuItemId: Int) {
        when (menuItemId) {
            R.id.menu_play -> presenter.onPlayActionSelected(composition)
            R.id.menu_play_next -> presenter.onPlayNextCompositionClicked(composition)
            R.id.menu_add_to_queue -> presenter.onAddToQueueCompositionClicked(composition)
            R.id.menu_add_to_playlist -> presenter.onAddToPlayListButtonClicked(composition)
            R.id.menu_edit -> startActivity(newCompositionEditorIntent(requireContext(), composition.id))
            R.id.menu_share -> DialogUtils.shareComposition(requireContext(), composition)
            R.id.menu_delete -> presenter.onDeleteCompositionButtonClicked(composition)
        }
    }

    private fun onActionModeItemClicked(menuItem: MenuItem) {
        when (menuItem.itemId) {
            R.id.menu_play -> presenter.onPlayAllSelectedClicked()
            R.id.menu_select_all -> presenter.onSelectAllButtonClicked()
            R.id.menu_play_next -> presenter.onPlayNextSelectedSourcesClicked()
            R.id.menu_add_to_queue -> presenter.onAddToQueueSelectedSourcesClicked()
            R.id.menu_add_to_playlist -> presenter.onAddSelectedSourcesToPlayListClicked()
            R.id.menu_share -> presenter.onShareSelectedSourcesClicked()
            R.id.menu_delete -> presenter.onDeleteSelectedCompositionButtonClicked()
        }
    }

    private fun onFolderMenuClicked(view: View, folder: FolderFileSource) {
        PopupMenuWindow.showPopup(
            view,
            R.menu.folder_item_menu
        ) { item ->
            when (item.itemId) {
                R.id.menu_play -> presenter.onPlayFolderClicked(folder)
                R.id.menu_play_next -> presenter.onPlayNextFolderClicked(folder)
                R.id.menu_add_to_queue -> presenter.onAddToQueueFolderClicked(folder)
                R.id.menu_add_to_playlist -> presenter.onAddFolderToPlayListButtonClicked(folder)
                R.id.menu_rename_folder -> presenter.onRenameFolderClicked(folder)
                R.id.menu_share -> presenter.onShareFolderClicked(folder)
                R.id.menu_hide -> presenter.onExcludeFolderClicked(folder)
                R.id.menu_delete -> presenter.onDeleteFolderButtonClicked(folder)
            }
        }
    }

    private fun getFolderId(): Long? {
        val id = requireArguments().getLong(Constants.Arguments.ID_ARG)
        return if (id == 0L) null else id
    }

    private fun onOptionsItemClicked(item: MenuItem) {
        when (item.itemId) {
            R.id.menu_order -> presenter.onOrderMenuItemClicked()
            R.id.menu_excluded_folders -> {
                val parentFragment = parentFragment
                if (parentFragment != null) {
                    FragmentNavigation.from(parentFragment.parentFragmentManager)
                        .addNewFragment(ExcludedFoldersFragment())
                }
            }
            R.id.menu_sleep_timer -> SleepTimerDialogFragment().show(childFragmentManager, null)
            R.id.menu_equalizer -> EqualizerDialogFragment().show(childFragmentManager, null)
            R.id.menu_search -> presenter.onSearchButtonClicked()
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