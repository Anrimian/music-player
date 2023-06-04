package com.github.anrimian.musicplayer.ui.library.artists.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.anrimian.musicplayer.Constants.Arguments
import com.github.anrimian.musicplayer.Constants.Tags
import com.github.anrimian.musicplayer.R
import com.github.anrimian.musicplayer.databinding.FragmentLibraryArtistsBinding
import com.github.anrimian.musicplayer.di.Components
import com.github.anrimian.musicplayer.domain.models.artist.Artist
import com.github.anrimian.musicplayer.domain.models.composition.Composition
import com.github.anrimian.musicplayer.domain.models.order.Order
import com.github.anrimian.musicplayer.domain.models.order.OrderType
import com.github.anrimian.musicplayer.domain.models.playlist.PlayList
import com.github.anrimian.musicplayer.domain.models.utils.ListPosition
import com.github.anrimian.musicplayer.domain.utils.toLongArray
import com.github.anrimian.musicplayer.ui.common.dialogs.shareCompositions
import com.github.anrimian.musicplayer.ui.common.error.ErrorCommand
import com.github.anrimian.musicplayer.ui.common.format.MessagesUtils
import com.github.anrimian.musicplayer.ui.common.menu.PopupMenuWindow
import com.github.anrimian.musicplayer.ui.common.toolbar.AdvancedToolbar
import com.github.anrimian.musicplayer.ui.common.view.ViewUtils
import com.github.anrimian.musicplayer.ui.editor.artist.newRenameArtistDialog
import com.github.anrimian.musicplayer.ui.equalizer.EqualizerDialogFragment
import com.github.anrimian.musicplayer.ui.library.LibraryFragment
import com.github.anrimian.musicplayer.ui.library.artists.items.newInstance
import com.github.anrimian.musicplayer.ui.library.artists.list.adapter.ArtistsAdapter
import com.github.anrimian.musicplayer.ui.library.common.order.SelectOrderDialogFragment
import com.github.anrimian.musicplayer.ui.playlist_screens.choose.ChoosePlayListDialogFragment
import com.github.anrimian.musicplayer.ui.playlist_screens.choose.newChoosePlayListDialogFragment
import com.github.anrimian.musicplayer.ui.sleep_timer.SleepTimerDialogFragment
import com.github.anrimian.musicplayer.ui.utils.fragments.BackButtonListener
import com.github.anrimian.musicplayer.ui.utils.fragments.DialogFragmentRunner
import com.github.anrimian.musicplayer.ui.utils.fragments.navigation.FragmentNavigation
import com.github.anrimian.musicplayer.ui.utils.fragments.navigation.FragmentNavigationListener
import com.github.anrimian.musicplayer.ui.utils.fragments.safeShow
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.RecyclerViewUtils
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.touch_helper.short_swipe.ShortSwipeCallback
import com.google.android.material.snackbar.Snackbar
import moxy.ktx.moxyPresenter

class ArtistsListFragment : LibraryFragment(), ArtistsListView,
    FragmentNavigationListener,
    BackButtonListener {

    private val presenter by moxyPresenter { Components.artistsComponent().artistsListPresenter() }

    private lateinit var binding: FragmentLibraryArtistsBinding

    private lateinit var toolbar: AdvancedToolbar
    private lateinit var adapter: ArtistsAdapter
    private lateinit var layoutManager: LinearLayoutManager

    private lateinit var selectOrderDialogRunner: DialogFragmentRunner<SelectOrderDialogFragment>
    private lateinit var choosePlayListDialogRunner: DialogFragmentRunner<ChoosePlayListDialogFragment>

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
            this,
            binding.rvArtists,
            presenter.getSelectedArtists(),
            presenter::onArtistClicked,
            presenter::onArtistLongClicked,
            this::onArtistMenuClicked
        )
        binding.rvArtists.adapter = adapter
        layoutManager = LinearLayoutManager(context)
        binding.rvArtists.layoutManager = layoutManager
        RecyclerViewUtils.attachFastScroller(binding.rvArtists)
        val callback = ShortSwipeCallback(
            requireContext(),
            R.drawable.ic_play_next,
            R.string.play_next,
            swipeCallback = presenter::onPlayNextArtistClicked
        )
        val itemTouchHelper = ItemTouchHelper(callback)
        itemTouchHelper.attachToRecyclerView(binding.rvArtists)

        val fm = childFragmentManager
        selectOrderDialogRunner = DialogFragmentRunner(fm, Tags.ORDER_TAG) { fragment ->
            fragment.setOnCompleteListener(presenter::onOrderSelected)
        }
        choosePlayListDialogRunner = DialogFragmentRunner(fm, Tags.SELECT_PLAYLIST_TAG) { fragment ->
            fragment.setComplexCompleteListener { playlist, extra ->
                presenter.onPlayListToAddingSelected(
                    playlist,
                    extra.getLongArray(Arguments.IDS_ARG)!!,
                    extra.getBoolean(Arguments.CLOSE_MULTISELECT_ARG)
                )
            }
        }
    }

    override fun onFragmentResumed() {
        super.onFragmentResumed()
        val toolbar: AdvancedToolbar = requireActivity().findViewById(R.id.toolbar)
        toolbar.setSubtitle(R.string.artists)
        toolbar.setupSearch(presenter::onSearchTextChanged, presenter.getSearchText())
        toolbar.setupSelectionModeMenu(R.menu.library_artists_selection_menu, this::onActionModeItemClicked)
        toolbar.setupOptionsMenu(R.menu.library_artists_menu, this::onOptionsItemClicked)
    }

    override fun onStop() {
        super.onStop()
        presenter.onStop(ViewUtils.getListPosition(layoutManager))
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

    override fun showSelectOrderScreen(order: Order) {
        val fragment = SelectOrderDialogFragment.newInstance(
            order,
            OrderType.NAME,
            OrderType.COMPOSITION_COUNT
        )
        selectOrderDialogRunner.show(fragment)
    }

    override fun goToArtistScreen(artist: Artist) {
        FragmentNavigation.from(parentFragmentManager).addNewFragment(newInstance(artist.id))
    }

    override fun onArtistSelected(artist: Artist, position: Int) {
        adapter.setItemSelected(position)
    }

    override fun onArtistUnselected(artist: Artist, position: Int) {
        adapter.setItemUnselected(position)
    }

    override fun setItemsSelected(selected: Boolean) {
        adapter.setItemsSelected(selected)
    }

    override fun showSelectionMode(count: Int) {
        toolbar.showSelectionMode(count)
    }

    override fun onCompositionsAddedToPlayNext(compositions: List<Composition>) {
        val message = MessagesUtils.getPlayNextMessage(requireContext(), compositions)
        MessagesUtils.makeSnackbar(binding.listContainer, message, Snackbar.LENGTH_SHORT).show()
    }

    override fun onCompositionsAddedToQueue(compositions: List<Composition>) {
        val message = MessagesUtils.getAddedToQueueMessage(requireContext(), compositions)
        MessagesUtils.makeSnackbar(binding.listContainer, message, Snackbar.LENGTH_SHORT).show()
    }

    override fun showSelectPlayListDialog(artists: Collection<Artist>, closeMultiselect: Boolean) {
        val args = Bundle().apply {
            putLongArray(Arguments.IDS_ARG, artists.toLongArray(Artist::getId))
            putBoolean(Arguments.CLOSE_MULTISELECT_ARG, closeMultiselect)
        }
        choosePlayListDialogRunner.show(newChoosePlayListDialogFragment(args))
    }

    override fun showAddingToPlayListComplete(playList: PlayList, compositions: List<Composition>) {
        val text = MessagesUtils.getAddToPlayListCompleteMessage(requireActivity(), playList, compositions)
        MessagesUtils.makeSnackbar(binding.listContainer, text, Snackbar.LENGTH_SHORT).show()
    }

    override fun showAddingToPlayListError(errorCommand: ErrorCommand) {
        MessagesUtils.makeSnackbar(
            binding.listContainer,
            getString(R.string.add_to_playlist_error_template, errorCommand.message),
            Snackbar.LENGTH_SHORT
        ).show()
    }

    override fun showErrorMessage(errorCommand: ErrorCommand) {
        MessagesUtils.makeSnackbar(
            binding.listContainer, errorCommand.message, Snackbar.LENGTH_LONG
        ).show()
    }

    override fun sendCompositions(compositions: List<Composition>) {
        shareCompositions(this, compositions)
    }

    override fun showReceiveCompositionsForSendError(errorCommand: ErrorCommand) {
        MessagesUtils.makeSnackbar(
            binding.listContainer,
            getString(R.string.can_not_receive_file_for_send, errorCommand.message),
            Snackbar.LENGTH_SHORT
        ).show()
    }

    private fun showEditArtistNameDialog(artist: Artist) {
        newRenameArtistDialog(artist.id, artist.name).safeShow(childFragmentManager)
    }

    private fun onArtistMenuClicked(view: View, artist: Artist) {
        PopupMenuWindow.showPopup(view, R.menu.artist_menu) { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_play -> presenter.onPlayArtistClicked(artist)
                R.id.menu_play_next -> presenter.onPlayNextArtistClicked(artist)
                R.id.menu_add_to_queue -> presenter.onAddToQueueArtistClicked(artist)
                R.id.menu_add_to_playlist -> presenter.onAddArtistToPlayListClicked(artist)
                R.id.menu_rename -> showEditArtistNameDialog(artist)
                R.id.menu_share -> presenter.onShareArtistClicked(artist)
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

    private fun onActionModeItemClicked(menuItem: MenuItem) {
        when (menuItem.itemId) {
            R.id.menu_play -> presenter.onPlayAllSelectedClicked()
            R.id.menu_select_all -> presenter.onSelectAllButtonClicked()
            R.id.menu_play_next -> presenter.onPlayNextSelectedArtistsClicked()
            R.id.menu_add_to_queue -> presenter.onAddToQueueSelectedArtistsClicked()
            R.id.menu_add_to_playlist -> presenter.onAddSelectedArtistsToPlayListClicked()
            R.id.menu_share -> presenter.onShareSelectedArtistsClicked()
        }
    }
}