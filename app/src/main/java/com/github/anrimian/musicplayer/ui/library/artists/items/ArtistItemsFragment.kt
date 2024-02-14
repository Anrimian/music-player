package com.github.anrimian.musicplayer.ui.library.artists.items

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.anrimian.filesync.models.state.file.FileSyncState
import com.github.anrimian.musicplayer.Constants
import com.github.anrimian.musicplayer.Constants.Tags
import com.github.anrimian.musicplayer.R
import com.github.anrimian.musicplayer.databinding.FragmentBaseFabListBinding
import com.github.anrimian.musicplayer.databinding.ItemAlbumsHorizontalBinding
import com.github.anrimian.musicplayer.di.Components
import com.github.anrimian.musicplayer.domain.models.albums.Album
import com.github.anrimian.musicplayer.domain.models.artist.Artist
import com.github.anrimian.musicplayer.domain.models.composition.Composition
import com.github.anrimian.musicplayer.domain.models.composition.CurrentComposition
import com.github.anrimian.musicplayer.domain.models.composition.DeletedComposition
import com.github.anrimian.musicplayer.domain.models.utils.ListPosition
import com.github.anrimian.musicplayer.ui.common.dialogs.shareCompositions
import com.github.anrimian.musicplayer.ui.common.dialogs.showConfirmDeleteDialog
import com.github.anrimian.musicplayer.ui.common.error.ErrorCommand
import com.github.anrimian.musicplayer.ui.common.format.FormatUtils
import com.github.anrimian.musicplayer.ui.common.format.MessagesUtils
import com.github.anrimian.musicplayer.ui.common.toolbar.AdvancedToolbar
import com.github.anrimian.musicplayer.ui.common.view.ViewUtils
import com.github.anrimian.musicplayer.ui.editor.artist.RenameArtistDialogFragment
import com.github.anrimian.musicplayer.ui.editor.common.DeleteErrorHandler
import com.github.anrimian.musicplayer.ui.editor.common.ErrorHandler
import com.github.anrimian.musicplayer.ui.equalizer.EqualizerDialogFragment
import com.github.anrimian.musicplayer.ui.library.albums.items.AlbumItemsFragment
import com.github.anrimian.musicplayer.ui.library.artists.items.adapter.AlbumsAdapter
import com.github.anrimian.musicplayer.ui.library.common.compositions.BaseLibraryCompositionsFragment
import com.github.anrimian.musicplayer.ui.library.compositions.adapter.CompositionsAdapter
import com.github.anrimian.musicplayer.ui.playlist_screens.choose.ChoosePlayListDialogFragment
import com.github.anrimian.musicplayer.ui.playlist_screens.choose.newChoosePlayListDialogFragment
import com.github.anrimian.musicplayer.ui.sleep_timer.SleepTimerDialogFragment
import com.github.anrimian.musicplayer.ui.utils.fragments.BackButtonListener
import com.github.anrimian.musicplayer.ui.utils.fragments.DialogFragmentRunner
import com.github.anrimian.musicplayer.ui.utils.fragments.navigation.FragmentNavigation
import com.github.anrimian.musicplayer.ui.utils.fragments.navigation.FragmentNavigationListener
import com.github.anrimian.musicplayer.ui.utils.fragments.safeShow
import com.github.anrimian.musicplayer.ui.utils.slidr.SlidrPanel
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.RecyclerViewUtils
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.SingleItemAdapter
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.touch_helper.short_swipe.ShortSwipeCallback
import com.google.android.material.snackbar.Snackbar
import com.r0adkll.slidr.model.SlidrInterface
import moxy.ktx.moxyPresenter

class ArtistItemsFragment : BaseLibraryCompositionsFragment(), ArtistItemsView,
    FragmentNavigationListener, BackButtonListener {

    companion object {
        fun newInstance(artistId: Long) = ArtistItemsFragment().apply {
            arguments = Bundle().apply {
                putLong(Constants.Arguments.ID_ARG, artistId)
            }
        }
    }

    private val presenter by moxyPresenter { Components.artistItemsComponent(getAlbumId()).artistItemsPresenter() }

    private lateinit var binding: FragmentBaseFabListBinding

    private lateinit var toolbar: AdvancedToolbar
    private lateinit var albumsAdapter: AlbumsAdapter
    private lateinit var albumsHeaderWrapper: SingleItemAdapter<ItemAlbumsHorizontalBinding>
    private lateinit var adapter: CompositionsAdapter<Composition>
    private lateinit var layoutManager: LinearLayoutManager

    private lateinit var choosePlayListDialogRunner: DialogFragmentRunner<ChoosePlayListDialogFragment>
    private lateinit var slidrInterface: SlidrInterface

    private lateinit var deletingErrorHandler: ErrorHandler

    private var isCompositionsEmpty: Boolean = true
    private var isAlbumsEmpty: Boolean = true

    override fun getLibraryPresenter(): ArtistItemsPresenter = presenter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentBaseFabListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        toolbar = requireActivity().findViewById(R.id.toolbar)
        binding.progressStateView.onTryAgainClick { presenter.onTryAgainLoadCompositionsClicked() }

        RecyclerViewUtils.attachFastScroller(binding.recyclerView, true)

        albumsHeaderWrapper = SingleItemAdapter { inflater, parent ->
            val binding = ItemAlbumsHorizontalBinding.inflate(inflater, parent, false)

            binding.rvAlbums.layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
            binding.rvAlbums.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    onAlbumsScrolled(binding.rvAlbums.computeHorizontalScrollOffset() == 0)
                }
            })

            albumsAdapter = AlbumsAdapter(binding.rvAlbums, this::onAlbumClicked)
            binding.rvAlbums.adapter = albumsAdapter

            return@SingleItemAdapter binding
        }
        adapter = CompositionsAdapter(
            this,
            binding.recyclerView,
            presenter.getSelectedCompositions(),
            presenter::onCompositionClicked,
            presenter::onCompositionLongClick,
            presenter::onCompositionIconClicked,
            this::onCompositionMenuClicked
        )
        binding.recyclerView.adapter = ConcatAdapter(albumsHeaderWrapper, adapter)
        layoutManager = LinearLayoutManager(context)
        binding.recyclerView.layoutManager = layoutManager
        val callback = ShortSwipeCallback(
            requireContext(),
            R.drawable.ic_play_next,
            R.string.play_next,
            shouldNotSwipeViewHolder = { viewHolder ->
                !isAlbumsEmpty && viewHolder.absoluteAdapterPosition == 0
            },
            swipeCallback = presenter::onPlayNextCompositionClicked
        )
        val itemTouchHelper = ItemTouchHelper(callback)
        itemTouchHelper.attachToRecyclerView(binding.recyclerView)

        binding.fab.setOnClickListener { presenter.onPlayAllButtonClicked() }
        ViewUtils.onLongVibrationClick(binding.fab, presenter::onChangeRandomModePressed)

        slidrInterface = SlidrPanel.simpleSwipeBack(
            binding.listContainer,
            this,
            toolbar::onStackFragmentSlided
        )

        val fm = childFragmentManager

        deletingErrorHandler = DeleteErrorHandler(
            this,
            presenter::onRetryFailedDeleteActionClicked,
            this::showEditorRequestDeniedMessage
        )

        choosePlayListDialogRunner = DialogFragmentRunner(
            fm,
            Tags.SELECT_PLAYLIST_TAG
        ) { f -> f.setOnCompleteListener(presenter::onPlayListToAddingSelected) }
    }

    override fun onFragmentResumed() {
        presenter.onFragmentResumed()
        val toolbar: AdvancedToolbar = requireActivity().findViewById(R.id.toolbar)
        toolbar.setupSearch(null, null)
        toolbar.setTitleClickListener(null)
        toolbar.setupSelectionModeMenu(R.menu.library_compositions_selection_menu, this::onActionModeItemClicked)
        toolbar.setupOptionsMenu(R.menu.library_artist_items_menu, this::onOptionsItemClicked)
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

    override fun getCoordinatorLayout() = binding.listContainer

    override fun showArtistInfo(artist: Artist) {
        toolbar.setTitle(artist.name)
        toolbar.setSubtitle(FormatUtils.formatArtistAdditionalInfo(
            requireContext(),
            artist,
            R.drawable.ic_description_text_circle_inverse
        ))
    }

    override fun showEmptyList() {
        binding.fab.visibility = View.GONE
        binding.progressStateView.hideAll()
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

    override fun updateList(compositions: List<Composition>) {
        adapter.submitList(compositions)

        isCompositionsEmpty = compositions.isEmpty()
        updateEmptyMessage()
        albumsHeaderWrapper.runAction { binding ->
            val titleVisibility = if (!isCompositionsEmpty) View.VISIBLE else View.GONE
            binding.tvSongsTitle.visibility = titleVisibility
        }
    }

    override fun restoreListPosition(listPosition: ListPosition) {
        ViewUtils.scrollToPosition(layoutManager, listPosition)
    }

    override fun showArtistAlbums(albums: List<Album>) {
        albumsHeaderWrapper.runAction { binding ->
            albumsAdapter.submitList(albums)

            val root = binding.root
            val params = root.layoutParams
            if (albums.isEmpty()) {
                root.visibility = View.GONE
                params.width = 0
                params.height = 0
            } else {
                root.visibility = View.VISIBLE
                params.width = ViewGroup.LayoutParams.MATCH_PARENT
                params.height = ViewGroup.LayoutParams.WRAP_CONTENT
            }
            root.layoutParams = params
        }
        isAlbumsEmpty = albums.isEmpty()
        updateEmptyMessage()
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
        val dialog = if (toolbar.isInActionMode()) {
            newChoosePlayListDialogFragment(R.attr.actionModeStatusBarColor)
        } else {
            ChoosePlayListDialogFragment()
        }
        choosePlayListDialogRunner.show(dialog)
    }

    override fun showConfirmDeleteDialog(compositionsToDelete: List<Composition>) {
        showConfirmDeleteDialog(
            requireContext(),
            compositionsToDelete,
            presenter::onDeleteCompositionsDialogConfirmed
        )
    }

    override fun showDeleteCompositionError(errorCommand: ErrorCommand) {
        deletingErrorHandler.handleError(errorCommand) {
            MessagesUtils.makeSnackbar(
                binding.listContainer,
                getString(R.string.delete_composition_error_template, errorCommand.message),
                Snackbar.LENGTH_SHORT
            ).show()
        }
    }

    override fun showDeleteCompositionMessage(compositionsToDelete: List<DeletedComposition>) {
        val text = MessagesUtils.getDeleteCompleteMessage(requireActivity(), compositionsToDelete)
        MessagesUtils.makeSnackbar(binding.listContainer, text, Snackbar.LENGTH_SHORT).show()
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

    override fun closeScreen() {
        FragmentNavigation.from(parentFragmentManager).goBack()
    }

    override fun showRenameArtistDialog(artist: Artist) {
        RenameArtistDialogFragment.newInstance(artist.id, artist.name).safeShow(childFragmentManager)
    }

    override fun showFilesSyncState(states: Map<Long, FileSyncState>) {
        adapter.showFileSyncStates(states)
    }

    //scroll horizontally then scroll to bottom issue
    private fun onAlbumsScrolled(onStart: Boolean) {
        if (onStart) {
            slidrInterface.unlock()
        } else {
            slidrInterface.lock()
        }
    }

    private fun getAlbumId() = requireArguments().getLong(Constants.Arguments.ID_ARG)

    private fun onAlbumClicked(album: Album) {
        FragmentNavigation.from(parentFragmentManager)
            .addNewFragment(AlbumItemsFragment.newInstance(album.id))
    }

    private fun onOptionsItemClicked(item: MenuItem) {
        when (item.itemId) {
            R.id.menu_rename -> presenter.onRenameArtistClicked()
            R.id.menu_sleep_timer -> SleepTimerDialogFragment().safeShow(childFragmentManager)
            R.id.menu_equalizer -> EqualizerDialogFragment().safeShow(childFragmentManager)
        }
    }

    private fun showEditorRequestDeniedMessage() {
        MessagesUtils.makeSnackbar(binding.listContainer, R.string.android_r_edit_file_permission_denied, Snackbar.LENGTH_LONG).show()
    }

    private fun updateEmptyMessage() {
        if (isCompositionsEmpty && isAlbumsEmpty) {
            binding.progressStateView.showMessage(R.string.no_compositions)
        }
    }
}