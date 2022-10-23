package com.github.anrimian.musicplayer.ui.library.genres.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.anrimian.musicplayer.Constants
import com.github.anrimian.musicplayer.Constants.Tags
import com.github.anrimian.musicplayer.R
import com.github.anrimian.musicplayer.databinding.FragmentBaseListBinding
import com.github.anrimian.musicplayer.di.Components
import com.github.anrimian.musicplayer.domain.models.genres.Genre
import com.github.anrimian.musicplayer.domain.models.order.Order
import com.github.anrimian.musicplayer.domain.models.order.OrderType
import com.github.anrimian.musicplayer.ui.common.dialogs.input.InputTextDialogFragment
import com.github.anrimian.musicplayer.ui.common.error.ErrorCommand
import com.github.anrimian.musicplayer.ui.common.format.MessagesUtils
import com.github.anrimian.musicplayer.ui.common.serialization.GenreSerializer
import com.github.anrimian.musicplayer.ui.common.toolbar.AdvancedToolbar
import com.github.anrimian.musicplayer.ui.equalizer.EqualizerDialogFragment
import com.github.anrimian.musicplayer.ui.library.LibraryFragment
import com.github.anrimian.musicplayer.ui.library.common.order.SelectOrderDialogFragment
import com.github.anrimian.musicplayer.ui.library.genres.items.newGenreItemsFragment
import com.github.anrimian.musicplayer.ui.library.genres.list.adapter.GenresAdapter
import com.github.anrimian.musicplayer.ui.sleep_timer.SleepTimerDialogFragment
import com.github.anrimian.musicplayer.ui.utils.dialogs.ProgressDialogFragment
import com.github.anrimian.musicplayer.ui.utils.dialogs.menu.MenuDialogFragment
import com.github.anrimian.musicplayer.ui.utils.dialogs.newProgressDialogFragment
import com.github.anrimian.musicplayer.ui.utils.fragments.BackButtonListener
import com.github.anrimian.musicplayer.ui.utils.fragments.DialogFragmentDelayRunner
import com.github.anrimian.musicplayer.ui.utils.fragments.DialogFragmentRunner
import com.github.anrimian.musicplayer.ui.utils.fragments.navigation.FragmentLayerListener
import com.github.anrimian.musicplayer.ui.utils.fragments.navigation.FragmentNavigation
import com.github.anrimian.musicplayer.ui.utils.fragments.safeShow
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.RecyclerViewUtils
import com.google.android.material.snackbar.Snackbar
import moxy.ktx.moxyPresenter

class GenresListFragment : LibraryFragment(), GenresListView, FragmentLayerListener,
    BackButtonListener {

    private val presenter by moxyPresenter {
        Components.genresComponent().genresListPresenter()
    }

    private lateinit var viewBinding: FragmentBaseListBinding

    private lateinit var toolbar: AdvancedToolbar
    private lateinit var adapter: GenresAdapter

    private lateinit var genreMenuDialogRunner: DialogFragmentRunner<MenuDialogFragment>
    private lateinit var editGenreNameDialogRunner: DialogFragmentRunner<InputTextDialogFragment>
    private lateinit var selectOrderDialogRunner: DialogFragmentRunner<SelectOrderDialogFragment>

    private lateinit var progressDialogRunner: DialogFragmentDelayRunner<ProgressDialogFragment>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewBinding = FragmentBaseListBinding.inflate(inflater, container, false)
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        toolbar = requireActivity().findViewById(R.id.toolbar)

        viewBinding.progressStateView.onTryAgainClick(presenter::onTryAgainLoadCompositionsClicked)

        RecyclerViewUtils.attachFastScroller(viewBinding.recyclerView)
        adapter = GenresAdapter(
            viewBinding.recyclerView,
            this::goToGenreScreen,
            this::onGenreLongClick
        )
        viewBinding.recyclerView.adapter = adapter
        val layoutManager = LinearLayoutManager(context)
        viewBinding.recyclerView.layoutManager = layoutManager

        val fm = childFragmentManager
        selectOrderDialogRunner = DialogFragmentRunner(fm, Tags.ORDER_TAG) { f ->
            f.setOnCompleteListener(presenter::onOrderSelected)
        }
        genreMenuDialogRunner = DialogFragmentRunner(fm, Tags.GENRE_MENU_TAG) { f ->
            f.setComplexCompleteListener(this::onGenreMenuClicked)
        }
        editGenreNameDialogRunner = DialogFragmentRunner(fm, Tags.GENRE_NAME_TAG) { f ->
            f.setComplexCompleteListener { name, extra ->
                presenter.onNewGenreNameEntered(name, extra.getLong(Constants.Arguments.ID_ARG))
            }
        }
        progressDialogRunner = DialogFragmentDelayRunner(fm, Tags.PROGRESS_DIALOG_TAG)
    }

    override fun onFragmentMovedOnTop() {
        super.onFragmentMovedOnTop()
        val toolbar: AdvancedToolbar = requireActivity().findViewById(R.id.toolbar)
        toolbar.setSubtitle(R.string.genres)
        toolbar.setupSearch(presenter::onSearchTextChanged, presenter.getSearchText())
        toolbar.setupOptionsMenu(R.menu.library_genres_menu, this::onOptionsItemClicked)
    }

    override fun onBackPressed(): Boolean {
        if (toolbar.isInSearchMode) {
            toolbar.setSearchModeEnabled(false)
            return true
        }
        return false
    }

    override fun showEmptyList() {
        viewBinding.progressStateView.showMessage(R.string.no_genres_in_library)
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

    override fun showLoadingError(errorCommand: ErrorCommand) {
        viewBinding.progressStateView.showMessage(errorCommand.message, true)
    }

    override fun submitList(genres: List<Genre>) {
        adapter.submitList(genres)
    }

    override fun showRenameProgress() {
        val fragment = newProgressDialogFragment(R.string.rename_progress)
        progressDialogRunner.show(fragment)
    }

    override fun hideRenameProgress() {
        progressDialogRunner.cancel()
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

    private fun onGenreLongClick(genre: Genre) {
        val extra = GenreSerializer.serialize(genre)
        val fragment = MenuDialogFragment.newInstance(
            R.menu.genre_menu,
            genre.name,
            extra
        )
        genreMenuDialogRunner.show(fragment)
    }

    private fun onGenreMenuClicked(menuItem: MenuItem, extra: Bundle) {
        val genre = GenreSerializer.deserialize(extra)
        when (menuItem.itemId) {
            R.id.menu_rename -> showEditGenreNameDialog(genre)
        }
    }

    private fun showEditGenreNameDialog(genre: Genre) {
        val bundle = Bundle()
        bundle.putLong(Constants.Arguments.ID_ARG, genre.id)
        val fragment = InputTextDialogFragment.Builder(
            R.string.change_name,
            R.string.change,
            R.string.cancel,
            R.string.name,
            genre.name
        ).canBeEmpty(false)
            .extra(bundle)
            .build()
        editGenreNameDialogRunner.show(fragment)
    }

    private fun goToGenreScreen(genre: Genre) {
        FragmentNavigation.from(parentFragmentManager)
            .addNewFragment(newGenreItemsFragment(genre.id))
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