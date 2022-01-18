package com.github.anrimian.musicplayer.ui.library.artists.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.anrimian.musicplayer.Constants
import com.github.anrimian.musicplayer.Constants.Tags
import com.github.anrimian.musicplayer.R
import com.github.anrimian.musicplayer.databinding.FragmentLibraryArtistsBinding
import com.github.anrimian.musicplayer.di.Components
import com.github.anrimian.musicplayer.domain.models.artist.Artist
import com.github.anrimian.musicplayer.domain.models.order.Order
import com.github.anrimian.musicplayer.domain.models.order.OrderType
import com.github.anrimian.musicplayer.domain.models.utils.ListPosition
import com.github.anrimian.musicplayer.ui.common.dialogs.input.InputTextDialogFragment
import com.github.anrimian.musicplayer.ui.common.error.ErrorCommand
import com.github.anrimian.musicplayer.ui.common.format.MessagesUtils
import com.github.anrimian.musicplayer.ui.common.menu.PopupMenuWindow
import com.github.anrimian.musicplayer.ui.common.toolbar.AdvancedToolbar
import com.github.anrimian.musicplayer.ui.common.view.ViewUtils
import com.github.anrimian.musicplayer.ui.editor.common.ErrorHandler
import com.github.anrimian.musicplayer.ui.equalizer.EqualizerDialogFragment
import com.github.anrimian.musicplayer.ui.library.LibraryFragment
import com.github.anrimian.musicplayer.ui.library.artists.items.newInstance
import com.github.anrimian.musicplayer.ui.library.artists.list.adapter.ArtistsAdapter
import com.github.anrimian.musicplayer.ui.library.common.order.SelectOrderDialogFragment
import com.github.anrimian.musicplayer.ui.sleep_timer.SleepTimerDialogFragment
import com.github.anrimian.musicplayer.ui.utils.dialogs.ProgressDialogFragment
import com.github.anrimian.musicplayer.ui.utils.fragments.BackButtonListener
import com.github.anrimian.musicplayer.ui.utils.fragments.DialogFragmentDelayRunner
import com.github.anrimian.musicplayer.ui.utils.fragments.DialogFragmentRunner
import com.github.anrimian.musicplayer.ui.utils.fragments.navigation.FragmentLayerListener
import com.github.anrimian.musicplayer.ui.utils.fragments.navigation.FragmentNavigation
import com.github.anrimian.musicplayer.ui.utils.fragments.safeShow
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.RecyclerViewUtils
import com.google.android.material.snackbar.Snackbar
import moxy.ktx.moxyPresenter

class ArtistsListFragment : LibraryFragment(), ArtistsListView, FragmentLayerListener,
    BackButtonListener {

    private val presenter by moxyPresenter {
        Components.artistsComponent().artistsListPresenter()
    }

    private lateinit var binding: FragmentLibraryArtistsBinding

    private lateinit var toolbar: AdvancedToolbar
    private lateinit var adapter: ArtistsAdapter
    private lateinit var layoutManager: LinearLayoutManager

    private lateinit var editArtistNameDialogRunner: DialogFragmentRunner<InputTextDialogFragment>
    private lateinit var selectOrderDialogRunner: DialogFragmentRunner<SelectOrderDialogFragment>
    private lateinit var progressDialogRunner: DialogFragmentDelayRunner

    private lateinit var editorErrorHandler: ErrorHandler

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLibraryArtistsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        toolbar = requireActivity().findViewById(R.id.toolbar)

        binding.progressStateView.onTryAgainClick(presenter::onTryAgainLoadCompositionsClicked)

        adapter = ArtistsAdapter(
            binding.recyclerView,
            this::goToArtistScreen,
            this::onArtistMenuClicked
        )
        binding.recyclerView.adapter = adapter
        layoutManager = LinearLayoutManager(context)
        binding.recyclerView.layoutManager = layoutManager
        RecyclerViewUtils.attachFastScroller(binding.recyclerView)

        val fm = childFragmentManager
        editArtistNameDialogRunner = DialogFragmentRunner(fm, Tags.ARTIST_NAME_TAG) { fragment ->
            fragment.setComplexCompleteListener { name, extra ->
                presenter.onNewArtistNameEntered(name, extra.getLong(Constants.Arguments.ID_ARG))
            }
        }
        selectOrderDialogRunner = DialogFragmentRunner(fm, Tags.ORDER_TAG) { fragment ->
            fragment.setOnCompleteListener(presenter::onOrderSelected)
        }

        progressDialogRunner = DialogFragmentDelayRunner(fm, Tags.PROGRESS_DIALOG_TAG)
        
        editorErrorHandler = ErrorHandler(
            fm,
            presenter::onRetryFailedEditActionClicked,
            this::showEditorRequestDeniedMessage
        )
    }

    override fun onFragmentMovedOnTop() {
        super.onFragmentMovedOnTop()
        val toolbar: AdvancedToolbar = requireActivity().findViewById(R.id.toolbar)
        toolbar.setSubtitle(R.string.artists)
        toolbar.setupSearch(presenter::onSearchTextChanged, presenter.getSearchText())
        toolbar.setupOptionsMenu(R.menu.library_artists_menu, this::onOptionsItemClicked)
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
        binding.progressStateView.showMessage(R.string.no_artists_in_library)
    }

    override fun showEmptySearchResult() {
        binding.progressStateView.showMessage(R.string.compositions_for_search_not_found)
    }

    override fun showList() {
        binding.progressStateView.hideAll()
    }

    override fun showLoading() {
        binding.progressStateView.showProgress()
    }

    override fun showLoadingError(errorCommand: ErrorCommand) {
        binding.progressStateView.showMessage(errorCommand.message, true)
    }

    override fun submitList(artists: List<Artist>) {
        adapter.submitList(artists)
    }

    override fun restoreListPosition(listPosition: ListPosition) {
        ViewUtils.scrollToPosition(layoutManager, listPosition)
    }

    override fun showRenameProgress() {
        val fragment = ProgressDialogFragment.newInstance(R.string.rename_progress)
        progressDialogRunner.show(fragment)
    }

    override fun hideRenameProgress() {
        progressDialogRunner.cancel()
    }

    override fun showSelectOrderScreen(order: Order) {
        val fragment = SelectOrderDialogFragment.newInstance(
            order,
            OrderType.NAME,
            OrderType.COMPOSITION_COUNT
        )
        selectOrderDialogRunner.show(fragment)
    }

    override fun showErrorMessage(errorCommand: ErrorCommand) {
        editorErrorHandler.handleError(errorCommand) {
            MessagesUtils.makeSnackbar(
                binding.listContainer, errorCommand.message, Snackbar.LENGTH_LONG
            ).show()
        }
    }

    private fun showEditArtistNameDialog(artist: Artist) {
        val bundle = Bundle()
        bundle.putLong(Constants.Arguments.ID_ARG, artist.id)
        val fragment = InputTextDialogFragment.Builder(
            R.string.change_name,
            R.string.change,
            R.string.cancel,
            R.string.name,
            artist.name
        ).canBeEmpty(false)
            .extra(bundle)
            .build()
        editArtistNameDialogRunner.show(fragment)
    }

    private fun goToArtistScreen(artist: Artist) {
        FragmentNavigation.from(parentFragmentManager).addNewFragment(newInstance(artist.id))
    }

    private fun onArtistMenuClicked(view: View, artist: Artist) {
        PopupMenuWindow.showPopup(view, R.menu.artist_menu) { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_rename -> showEditArtistNameDialog(artist)
            }
        }
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
        MessagesUtils.makeSnackbar(
            binding.listContainer,
            R.string.android_r_edit_file_permission_denied,
            Snackbar.LENGTH_LONG
        ).show()
    }
}