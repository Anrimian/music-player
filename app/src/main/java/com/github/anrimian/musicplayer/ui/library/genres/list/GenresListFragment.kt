package com.github.anrimian.musicplayer.ui.library.genres.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.anrimian.musicplayer.Constants
import com.github.anrimian.musicplayer.Constants.Tags
import com.github.anrimian.musicplayer.R
import com.github.anrimian.musicplayer.databinding.FragmentBaseListBinding
import com.github.anrimian.musicplayer.di.Components
import com.github.anrimian.musicplayer.domain.models.composition.Composition
import com.github.anrimian.musicplayer.domain.models.genres.Genre
import com.github.anrimian.musicplayer.domain.models.order.Order
import com.github.anrimian.musicplayer.domain.models.order.OrderType
import com.github.anrimian.musicplayer.domain.models.utils.ListPosition
import com.github.anrimian.musicplayer.domain.utils.toLongArray
import com.github.anrimian.musicplayer.ui.common.dialogs.shareCompositions
import com.github.anrimian.musicplayer.ui.common.error.ErrorCommand
import com.github.anrimian.musicplayer.ui.common.menu.PopupMenuWindow
import com.github.anrimian.musicplayer.ui.common.toolbar.AdvancedToolbar
import com.github.anrimian.musicplayer.ui.common.view.ViewUtils
import com.github.anrimian.musicplayer.ui.editor.genre.RenameGenreDialogFragment
import com.github.anrimian.musicplayer.ui.equalizer.EqualizerDialogFragment
import com.github.anrimian.musicplayer.ui.library.common.library.BaseLibraryFragment
import com.github.anrimian.musicplayer.ui.library.common.library.BaseLibraryPresenter
import com.github.anrimian.musicplayer.ui.library.common.order.SelectOrderDialogFragment
import com.github.anrimian.musicplayer.ui.library.common.setupLibraryTitle
import com.github.anrimian.musicplayer.ui.library.genres.items.GenreItemsFragment
import com.github.anrimian.musicplayer.ui.library.genres.list.adapter.GenresAdapter
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
import moxy.ktx.moxyPresenter

class GenresListFragment : BaseLibraryFragment(), GenresListView, FragmentNavigationListener,
    BackButtonListener {

    private val presenter by moxyPresenter {
        Components.genresComponent().genresListPresenter()
    }

    private lateinit var binding: FragmentBaseListBinding

    private lateinit var toolbar: AdvancedToolbar
    private lateinit var adapter: GenresAdapter
    private lateinit var layoutManager: LinearLayoutManager

    private lateinit var selectOrderDialogRunner: DialogFragmentRunner<SelectOrderDialogFragment>
    private lateinit var choosePlayListDialogRunner: DialogFragmentRunner<ChoosePlayListDialogFragment>

    override fun getLibraryPresenter(): BaseLibraryPresenter<*> = presenter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentBaseListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        toolbar = requireActivity().findViewById(R.id.toolbar)

        binding.progressStateView.onTryAgainClick(presenter::onTryAgainLoadCompositionsClicked)

        RecyclerViewUtils.attachFastScroller(binding.recyclerView)
        adapter = GenresAdapter(
            this,
            binding.recyclerView,
            presenter.getSelectedGenres(),
            presenter::onGenreClicked,
            presenter::onGenreLongClicked,
            this::onGenreMenuClicked
        )
        binding.recyclerView.adapter = adapter
        layoutManager = LinearLayoutManager(context)
        binding.recyclerView.layoutManager = layoutManager
        val callback = ShortSwipeCallback(
            requireContext(),
            R.drawable.ic_play_next,
            R.string.play_next,
            swipeCallback = presenter::onPlayNextGenreClicked
        )
        val itemTouchHelper = ItemTouchHelper(callback)
        itemTouchHelper.attachToRecyclerView(binding.recyclerView)

        val fm = childFragmentManager
        selectOrderDialogRunner = DialogFragmentRunner(fm, Tags.ORDER_TAG) { f ->
            f.setOnCompleteListener(presenter::onOrderSelected)
        }
        choosePlayListDialogRunner = DialogFragmentRunner(fm, Tags.SELECT_PLAYLIST_TAG) { fragment ->
            fragment.setComplexCompleteListener { playlist, extra ->
                presenter.onPlayListToAddingSelected(
                    playlist,
                    extra.getLongArray(Constants.Arguments.IDS_ARG)!!,
                    extra.getBoolean(Constants.Arguments.CLOSE_MULTISELECT_ARG)
                )
            }
        }
    }

    override fun onFragmentResumed() {
        presenter.onFragmentResumed()
        val toolbar: AdvancedToolbar = requireActivity().findViewById(R.id.toolbar)
        toolbar.setupLibraryTitle(this)
        toolbar.setSubtitle(R.string.genres)
        toolbar.setupSearch(presenter::onSearchTextChanged, presenter.getSearchText())
        toolbar.setupSelectionModeMenu(R.menu.library_genres_selection_menu, this::onActionModeItemClicked)
        toolbar.setupOptionsMenu(R.menu.library_genres_menu, this::onOptionsItemClicked)
    }

    override fun onStop() {
        super.onStop()
        presenter.onStop(ViewUtils.getListPosition(layoutManager))
    }

    override fun onBackPressed(): Boolean {
        if (toolbar.isInActionMode()) {
            presenter.onSelectionModeBackPressed()
            return true
        }
        if (toolbar.isInSearchMode()) {
            toolbar.setSearchModeEnabled(false)
            return true
        }
        return false
    }

    override fun getCoordinatorLayout() = binding.root

    override fun showEmptyList() {
        binding.progressStateView.showMessage(R.string.no_genres_in_library)
    }

    override fun showEmptySearchResult() {
        binding.progressStateView.showMessage(R.string.no_matching_search_results_found)
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

    override fun submitList(genres: List<Genre>) {
        adapter.submitList(genres)
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

    override fun goToGenreScreen(genre: Genre) {
        FragmentNavigation.from(parentFragmentManager)
            .addNewFragment(GenreItemsFragment.newInstance(genre.id))
    }

    override fun onGenreSelected(genre: Genre, position: Int) {
        adapter.setItemSelected(position)
    }

    override fun onGenreUnselected(genre: Genre, position: Int) {
        adapter.setItemUnselected(position)
    }

    override fun setItemsSelected(selected: Boolean) {
        adapter.setItemsSelected(selected)
    }

    override fun showSelectionMode(count: Int) {
        toolbar.showSelectionMode(count)
    }

    override fun showSelectPlayListDialog(albums: Collection<Genre>, closeMultiselect: Boolean) {
        val args = Bundle().apply {
            putLongArray(Constants.Arguments.IDS_ARG, albums.toLongArray(Genre::id))
            putBoolean(Constants.Arguments.CLOSE_MULTISELECT_ARG, closeMultiselect)
        }
        choosePlayListDialogRunner.show(newChoosePlayListDialogFragment(args))
    }

    override fun sendCompositions(compositions: List<Composition>) {
        shareCompositions(this, compositions)
    }

    private fun onGenreMenuClicked(view: View, genre: Genre) {
        PopupMenuWindow.showPopup(view, R.menu.genre_menu) { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_play -> presenter.onPlayGenreClicked(genre)
                R.id.menu_play_next -> presenter.onPlayNextGenreClicked(genre)
                R.id.menu_add_to_queue -> presenter.onAddToQueueGenreClicked(genre)
                R.id.menu_add_to_playlist -> presenter.onAddGenreToPlayListClicked(genre)
                R.id.menu_rename -> showEditGenreNameDialog(genre)
                R.id.menu_share -> presenter.onShareGenreClicked(genre)
            }
        }
    }

    private fun showEditGenreNameDialog(genre: Genre) {
        RenameGenreDialogFragment.newInstance(genre.id, genre.name).safeShow(childFragmentManager)
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
            R.id.menu_play_next -> presenter.onPlayNextSelectedGenresClicked()
            R.id.menu_add_to_queue -> presenter.onAddToQueueSelectedGenresClicked()
            R.id.menu_add_to_playlist -> presenter.onAddSelectedGenresToPlayListClicked()
            R.id.menu_share -> presenter.onShareSelectedGenresClicked()
        }
    }
}