package com.github.anrimian.musicplayer.ui.library.compositions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.anrimian.fsync.models.state.file.FileSyncState
import com.github.anrimian.musicplayer.AppConstants.Tags
import com.github.anrimian.musicplayer.R
import com.github.anrimian.musicplayer.databinding.FragmentLibraryCompositionsBinding
import com.github.anrimian.musicplayer.di.Components
import com.github.anrimian.musicplayer.domain.models.composition.Composition
import com.github.anrimian.musicplayer.domain.models.composition.CurrentComposition
import com.github.anrimian.musicplayer.domain.models.composition.DeletedComposition
import com.github.anrimian.musicplayer.domain.models.order.Order
import com.github.anrimian.musicplayer.domain.models.order.OrderType
import com.github.anrimian.musicplayer.domain.models.utils.ListPosition
import com.github.anrimian.musicplayer.ui.common.applyFabBottomInsets
import com.github.anrimian.musicplayer.ui.common.applyLibraryProgressViewOffset
import com.github.anrimian.musicplayer.ui.common.dialogs.shareCompositions
import com.github.anrimian.musicplayer.ui.common.dialogs.showConfirmDeleteDialog
import com.github.anrimian.musicplayer.ui.common.error.ErrorCommand
import com.github.anrimian.musicplayer.ui.common.format.FormatUtils
import com.github.anrimian.musicplayer.ui.common.format.MessagesUtils
import com.github.anrimian.musicplayer.ui.common.format.showSnackbar
import com.github.anrimian.musicplayer.ui.common.toolbar.AdvancedToolbar
import com.github.anrimian.musicplayer.ui.common.view.ViewUtils
import com.github.anrimian.musicplayer.ui.common.view.ViewUtils.onLongVibrationClick
import com.github.anrimian.musicplayer.ui.editor.common.DeleteErrorHandler
import com.github.anrimian.musicplayer.ui.editor.common.ErrorHandler
import com.github.anrimian.musicplayer.ui.equalizer.EqualizerDialogFragment
import com.github.anrimian.musicplayer.ui.library.common.compositions.BaseLibraryCompositionsFragment
import com.github.anrimian.musicplayer.ui.library.common.order.SelectOrderDialogFragment
import com.github.anrimian.musicplayer.ui.library.common.setupLibraryTitle
import com.github.anrimian.musicplayer.ui.library.compositions.adapter.CompositionsAdapter
import com.github.anrimian.musicplayer.ui.playlists.choose.ChoosePlayListDialogFragment
import com.github.anrimian.musicplayer.ui.sleep_timer.SleepTimerDialogFragment
import com.github.anrimian.musicplayer.ui.utils.applyBottomInsets
import com.github.anrimian.musicplayer.ui.utils.fragments.DialogFragmentRunner
import com.github.anrimian.musicplayer.ui.utils.fragments.navigation.FragmentNavigationListener
import com.github.anrimian.musicplayer.ui.utils.fragments.safeShow
import com.github.anrimian.musicplayer.ui.utils.isTabletLand
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.RecyclerViewUtils
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.touch_helper.short_swipe.ShortSwipeCallback
import com.google.android.material.snackbar.Snackbar
import moxy.ktx.moxyPresenter

class LibraryCompositionsFragment : BaseLibraryCompositionsFragment(), LibraryCompositionsView,
    FragmentNavigationListener {

    private val presenter by moxyPresenter {
        Components.getLibraryCompositionsComponent().libraryCompositionsPresenter()
    }

    private lateinit var binding: FragmentLibraryCompositionsBinding
    private lateinit var layoutManager: LinearLayoutManager

    private lateinit var toolbar: AdvancedToolbar
    private lateinit var adapter: CompositionsAdapter<Composition>

    private lateinit var choosePlayListDialogRunner: DialogFragmentRunner<ChoosePlayListDialogFragment>
    private lateinit var selectOrderDialogRunner: DialogFragmentRunner<SelectOrderDialogFragment>
    private lateinit var deletingErrorHandler: ErrorHandler

    override fun getLibraryPresenter(): LibraryCompositionsPresenter = presenter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLibraryCompositionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        toolbar = requireActivity().findViewById(R.id.toolbar)

        binding.fab.applyFabBottomInsets()
        binding.progressStateView.applyLibraryProgressViewOffset(requireActivity())
        if (isTabletLand()) {
            binding.rvCompositions.applyBottomInsets()
        }

        binding.progressStateView.onTryAgainClick { presenter.onTryAgainLoadCompositionsClicked() }

        layoutManager = LinearLayoutManager(context)
        binding.rvCompositions.layoutManager = layoutManager
        RecyclerViewUtils.attachFastScroller(binding.rvCompositions, true)
        adapter = CompositionsAdapter(
            this,
            binding.rvCompositions,
            presenter.getSelectedCompositions(),
            presenter::onCompositionClicked,
            presenter::onCompositionLongClick,
            presenter::onCompositionIconClicked,
            this::onCompositionMenuClicked
        )
        binding.rvCompositions.adapter = adapter
        val callback = ShortSwipeCallback(requireContext(),
            R.drawable.ic_play_next,
            R.string.play_next,
            swipeCallback = presenter::onPlayNextCompositionClicked
        )
        val itemTouchHelper = ItemTouchHelper(callback)
        itemTouchHelper.attachToRecyclerView(binding.rvCompositions)

        binding.fab.setOnClickListener { presenter.onPlayAllButtonClicked() }
        onLongVibrationClick(binding.fab, presenter::onChangeRandomModePressed)

        val fm = childFragmentManager
        deletingErrorHandler = DeleteErrorHandler(
            this,
            presenter::onRetryFailedDeleteActionClicked,
            this::showEditorRequestDeniedMessage
        )

        selectOrderDialogRunner = DialogFragmentRunner(fm, Tags.ORDER_TAG) {
                f -> f.setOnCompleteListener(presenter::onOrderSelected)
        }
        choosePlayListDialogRunner = DialogFragmentRunner(fm, Tags.SELECT_PLAYLIST_TAG) {
                f -> f.setOnCompleteListener(presenter::onPlayListToAddingSelected)
        }
    }

    override fun onFragmentResumed() {
        requireActivity().findViewById<AdvancedToolbar>(R.id.toolbar).setup {
            setupLibraryTitle(this@LibraryCompositionsFragment)
            setSubtitle(R.string.compositions)
            setupSearch(presenter::onSearchTextChanged, text = presenter.getSearchText())
            setupSelectionModeMenu(
                R.menu.library_compositions_selection_menu,
                ::onActionModeItemClicked,
                presenter::onExitSelectionModeClicked
            )
            setupOptionsMenu(R.menu.library_compositions_menu, ::onOptionsItemClicked)
        }
    }

    override fun onStop() {
        super.onStop()
        presenter.onStop(ViewUtils.getListPosition(layoutManager))
    }

    override fun getCoordinatorLayout() = binding.root

    override fun getFloatingActionButton() = binding.fab

    override fun showEmptyList() {
        binding.fab.visibility = View.GONE
        binding.progressStateView.showMessage(R.string.compositions_on_device_not_found)
    }

    override fun showEmptySearchResult() {
        binding.fab.visibility = View.GONE
        binding.progressStateView.showMessage(R.string.no_matching_search_results_found)
    }

    override fun showList() {
        binding.fab.visibility = View.VISIBLE
        binding.progressStateView.hideAll()
    }

    override fun showLoading() {
        binding.progressStateView.showProgress()
    }

    override fun showLoadingError(errorCommand: ErrorCommand) {
        binding.progressStateView.showMessage(errorCommand.message, true)
    }

    override fun updateList(genres: List<Composition>) {
        adapter.submitList(genres)
    }

    override fun restoreListPosition(listPosition: ListPosition) {
        ViewUtils.scrollToPosition(layoutManager, listPosition)
    }

    override fun onCompositionSelected(composition: Composition, position: Int) {
        adapter.setItemSelected(position)
    }

    override fun onCompositionUnselected(composition: Composition, position: Int) {
        adapter.setItemUnselected(position)
    }

    override fun setItemsSelected(selected: Boolean) {
        adapter.setItemsSelected(selected)
    }

    override fun showSelectionMode(count: Int) {
        toolbar.showSelectionMode(count)
    }

    override fun showSelectPlayListDialog() {
        choosePlayListDialogRunner.show(ChoosePlayListDialogFragment())
    }

    override fun showSelectOrderScreen(order: Order) {
        val fragment = SelectOrderDialogFragment.newInstance(
            order,
            true,
            OrderType.NAME,
            OrderType.FILE_NAME,
            OrderType.ARTIST,
            OrderType.ADD_TIME,
            OrderType.DURATION,
            OrderType.SIZE
        )
        selectOrderDialogRunner.show(fragment)
    }

    override fun showConfirmDeleteDialog(compositionsToDelete: List<Composition>) {
        showConfirmDeleteDialog(requireContext(), compositionsToDelete) {
            presenter.onDeleteCompositionsDialogConfirmed()
        }
    }

    override fun showDeleteCompositionError(errorCommand: ErrorCommand) {
        deletingErrorHandler.handleError(errorCommand) {
            binding.listContainer.showSnackbar(
                getString(R.string.delete_composition_error_template, errorCommand.message),
                anchorView = binding.fab
            )
        }
    }

    override fun showDeleteCompositionMessage(compositionsToDelete: List<DeletedComposition>) {
        val text = MessagesUtils.getDeleteCompleteMessage(requireActivity(), compositionsToDelete)
        binding.listContainer.showSnackbar(text, anchorView = binding.fab)
    }

    override fun shareCompositions(selectedCompositions: Collection<Composition>) {
        shareCompositions(this, selectedCompositions)
    }

    override fun showCurrentComposition(currentComposition: CurrentComposition) {
        adapter.showCurrentComposition(currentComposition)
    }

    override fun setDisplayCoversEnabled(isCoversEnabled: Boolean) {
        adapter.setCoversEnabled(isCoversEnabled)
    }

    override fun showRandomMode(isRandomModeEnabled: Boolean) {
        FormatUtils.formatPlayAllButton(binding.fab, isRandomModeEnabled)
    }

    override fun showFilesSyncState(states: Map<Long, FileSyncState>) {
        adapter.showFileSyncStates(states)
    }

    private fun onOptionsItemClicked(item: MenuItem) {
        when (item.itemId) {
            R.id.menu_order -> presenter.onOrderMenuItemClicked()
            R.id.menu_search -> toolbar.setSearchModeEnabled(true)
            R.id.menu_sleep_timer -> SleepTimerDialogFragment().safeShow(childFragmentManager)
            R.id.menu_equalizer -> EqualizerDialogFragment().safeShow(childFragmentManager)
        }
    }

    private fun showEditorRequestDeniedMessage() {
        binding.listContainer.showSnackbar(
            R.string.android_r_edit_file_permission_denied,
            Snackbar.LENGTH_LONG,
            binding.fab
        )
    }
}