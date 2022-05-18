package com.github.anrimian.musicplayer.ui.library.albums.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.anrimian.musicplayer.Constants.Tags
import com.github.anrimian.musicplayer.R
import com.github.anrimian.musicplayer.databinding.FragmentLibraryAlbumsBinding
import com.github.anrimian.musicplayer.di.Components
import com.github.anrimian.musicplayer.domain.models.albums.Album
import com.github.anrimian.musicplayer.domain.models.order.Order
import com.github.anrimian.musicplayer.domain.models.order.OrderType
import com.github.anrimian.musicplayer.domain.models.utils.ListPosition
import com.github.anrimian.musicplayer.ui.common.error.ErrorCommand
import com.github.anrimian.musicplayer.ui.common.format.MessagesUtils
import com.github.anrimian.musicplayer.ui.common.menu.PopupMenuWindow
import com.github.anrimian.musicplayer.ui.common.toolbar.AdvancedToolbar
import com.github.anrimian.musicplayer.ui.common.view.ViewUtils
import com.github.anrimian.musicplayer.ui.editor.album.newAlbumEditorIntent
import com.github.anrimian.musicplayer.ui.equalizer.EqualizerDialogFragment
import com.github.anrimian.musicplayer.ui.library.LibraryFragment
import com.github.anrimian.musicplayer.ui.library.albums.items.newAlbumItemsFragment
import com.github.anrimian.musicplayer.ui.library.albums.list.adapter.AlbumsAdapter
import com.github.anrimian.musicplayer.ui.library.common.order.SelectOrderDialogFragment
import com.github.anrimian.musicplayer.ui.sleep_timer.SleepTimerDialogFragment
import com.github.anrimian.musicplayer.ui.utils.fragments.BackButtonListener
import com.github.anrimian.musicplayer.ui.utils.fragments.DialogFragmentRunner
import com.github.anrimian.musicplayer.ui.utils.fragments.navigation.FragmentLayerListener
import com.github.anrimian.musicplayer.ui.utils.fragments.navigation.FragmentNavigation
import com.github.anrimian.musicplayer.ui.utils.fragments.safeShow
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.RecyclerViewUtils
import com.google.android.material.snackbar.Snackbar
import moxy.ktx.moxyPresenter

class AlbumsListFragment : LibraryFragment(), AlbumsListView, FragmentLayerListener,
    BackButtonListener {

    private val presenter by moxyPresenter {
        Components.albumsComponent().albumsListPresenter()
    }

    private lateinit var viewBinding: FragmentLibraryAlbumsBinding

    private lateinit var toolbar: AdvancedToolbar

    private lateinit var adapter: AlbumsAdapter
    private lateinit var layoutManager: LinearLayoutManager

    private lateinit var selectOrderDialogRunner: DialogFragmentRunner<SelectOrderDialogFragment>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewBinding = FragmentLibraryAlbumsBinding.inflate(inflater, container, false)
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        toolbar = requireActivity().findViewById(R.id.toolbar)
        
        viewBinding.progressStateView.onTryAgainClick(presenter::onTryAgainLoadCompositionsClicked)
        
        adapter = AlbumsAdapter(
            viewBinding.recyclerView,
            this::goToAlbumScreen,
            this::onAlbumMenuClicked
        )
        viewBinding.recyclerView.adapter = adapter
        layoutManager = LinearLayoutManager(context)
        viewBinding.recyclerView.layoutManager = layoutManager
        RecyclerViewUtils.attachFastScroller(viewBinding.recyclerView)

        val fm = childFragmentManager
        selectOrderDialogRunner = DialogFragmentRunner(fm, Tags.ORDER_TAG) { f ->
            f.setOnCompleteListener(presenter::onOrderSelected)
        }
    }

    override fun onFragmentMovedOnTop() {
        super.onFragmentMovedOnTop()
        val toolbar: AdvancedToolbar = requireActivity().findViewById(R.id.toolbar)
        toolbar.setSubtitle(R.string.albums)
        toolbar.setupSearch(
            presenter::onSearchTextChanged,
            presenter.getSearchText()
        )
        toolbar.setupOptionsMenu(R.menu.library_albums_menu, this::onOptionsItemClicked)
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
        viewBinding.progressStateView.showMessage(R.string.no_albums_in_library)
    }

    override fun showEmptySearchResult() {
        viewBinding.progressStateView.showMessage(R.string.compositions_for_search_not_found)
    }

    override fun showList() {
        viewBinding.progressStateView.hideAll()
    }

    override fun showLoading() {
        viewBinding.progressStateView.showProgress()
    }

    override fun showLoadingError(errorCommand: ErrorCommand?) {
        viewBinding.progressStateView.showMessage(errorCommand!!.message, true)
    }

    override fun submitList(albums: List<Album>) {
        adapter.submitList(albums)
    }

    override fun showErrorMessage(errorCommand: ErrorCommand) {
        MessagesUtils.makeSnackbar(
            viewBinding.listContainer,
            errorCommand.message,
            Snackbar.LENGTH_SHORT
        ).show()
    }

    override fun showSelectOrderScreen(order: Order) {
        val fragment = SelectOrderDialogFragment.newInstance(
            order,
            OrderType.NAME,
            OrderType.COMPOSITION_COUNT
        )
        selectOrderDialogRunner.show(fragment)
    }

    override fun restoreListPosition(listPosition: ListPosition) {
        ViewUtils.scrollToPosition(layoutManager, listPosition)
    }

    private fun goToAlbumScreen(album: Album) {
        FragmentNavigation.from(parentFragmentManager)
            .addNewFragment(newAlbumItemsFragment(album.id))
    }

    private fun onAlbumMenuClicked(view: View, album: Album) {
        PopupMenuWindow.showPopup(view, R.menu.album_menu) { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_edit -> startActivity(newAlbumEditorIntent(requireContext(), album.id))
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
}