package com.github.anrimian.musicplayer.ui.library.artists.items

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.annotation.AttrRes
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.anrimian.musicplayer.Constants
import com.github.anrimian.musicplayer.Constants.Tags
import com.github.anrimian.musicplayer.R
import com.github.anrimian.musicplayer.databinding.FragmentBaseFabListBinding
import com.github.anrimian.musicplayer.di.Components
import com.github.anrimian.musicplayer.domain.models.albums.Album
import com.github.anrimian.musicplayer.domain.models.artist.Artist
import com.github.anrimian.musicplayer.domain.models.composition.Composition
import com.github.anrimian.musicplayer.domain.models.composition.CurrentComposition
import com.github.anrimian.musicplayer.domain.models.playlist.PlayList
import com.github.anrimian.musicplayer.domain.models.utils.ListPosition
import com.github.anrimian.musicplayer.domain.utils.functions.BooleanConditionRunner
import com.github.anrimian.musicplayer.ui.common.dialogs.DialogUtils
import com.github.anrimian.musicplayer.ui.common.dialogs.composition.CompositionActionDialogFragment
import com.github.anrimian.musicplayer.ui.common.dialogs.input.InputTextDialogFragment
import com.github.anrimian.musicplayer.ui.common.error.ErrorCommand
import com.github.anrimian.musicplayer.ui.common.format.FormatUtils
import com.github.anrimian.musicplayer.ui.common.format.MessagesUtils
import com.github.anrimian.musicplayer.ui.common.toolbar.AdvancedToolbar
import com.github.anrimian.musicplayer.ui.common.view.ViewUtils
import com.github.anrimian.musicplayer.ui.editor.common.DeleteErrorHandler
import com.github.anrimian.musicplayer.ui.editor.common.ErrorHandler
import com.github.anrimian.musicplayer.ui.library.albums.items.newAlbumItemsFragment
import com.github.anrimian.musicplayer.ui.library.artists.items.adapter.AlbumsViewHolder
import com.github.anrimian.musicplayer.ui.library.artists.items.adapter.ArtistAlbumsPresenter
import com.github.anrimian.musicplayer.ui.library.artists.items.adapter.ArtistItemsAdapter
import com.github.anrimian.musicplayer.ui.library.common.compositions.BaseLibraryCompositionsFragment
import com.github.anrimian.musicplayer.ui.library.common.compositions.BaseLibraryCompositionsPresenter
import com.github.anrimian.musicplayer.ui.playlist_screens.choose.ChoosePlayListDialogFragment
import com.github.anrimian.musicplayer.ui.playlist_screens.choose.newChoosePlayListDialogFragment
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
import com.r0adkll.slidr.model.SlidrInterface
import moxy.ktx.moxyPresenter

fun newInstance(artistId: Long): ArtistItemsFragment {
    val args = Bundle()
    args.putLong(Constants.Arguments.ID_ARG, artistId)
    val fragment = ArtistItemsFragment()
    fragment.arguments = args
    return fragment
}

class ArtistItemsFragment : BaseLibraryCompositionsFragment(),
    ArtistItemsView, FragmentLayerListener, BackButtonListener {

    private val presenter by moxyPresenter { Components.artistItemsComponent(getAlbumId()).artistItemsPresenter() }

    private lateinit var viewBinding: FragmentBaseFabListBinding

    private lateinit var toolbar: AdvancedToolbar
    private lateinit var adapter: ArtistItemsAdapter
    private lateinit var layoutManager: LinearLayoutManager
    private val showNoCompositionsRunner = BooleanConditionRunner(2
    ) { viewBinding.progressStateView.showMessage(R.string.no_compositions) }

    private val artistAlbumsPresenter = ArtistAlbumsPresenter()

    private lateinit var compositionActionDialogRunner: DialogFragmentRunner<CompositionActionDialogFragment>
    private lateinit var choosePlayListDialogRunner: DialogFragmentRunner<ChoosePlayListDialogFragment>
    private lateinit var editArtistNameDialogRunner: DialogFragmentRunner<InputTextDialogFragment>
    private lateinit var progressDialogRunner: DialogFragmentDelayRunner
    private lateinit var slidrInterface: SlidrInterface

    private lateinit var deletingErrorHandler: ErrorHandler
    private lateinit var editorErrorHandler: ErrorHandler


    override fun getLibraryPresenter(): BaseLibraryCompositionsPresenter<ArtistItemsView> {
        return presenter
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        viewBinding = FragmentBaseFabListBinding.inflate(inflater, container, false)
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        toolbar = requireActivity().findViewById(R.id.toolbar)
        viewBinding.progressStateView.onTryAgainClick { presenter.onTryAgainLoadCompositionsClicked() }

        RecyclerViewUtils.attachFastScroller(viewBinding.recyclerView, true)

        adapter = ArtistItemsAdapter(
            viewBinding.recyclerView,
            presenter.getSelectedCompositions(),
            presenter::onCompositionClicked,
            presenter::onCompositionLongClick,
            presenter::onCompositionIconClicked,
            presenter::onCompositionMenuClicked,
            this::onAlbumClicked,
            this::onAlbumsScrolled
        )
        viewBinding.recyclerView.adapter = adapter
        layoutManager = LinearLayoutManager(context)
        viewBinding.recyclerView.layoutManager = layoutManager
        val callback = ShortSwipeCallback(
            requireContext(),
            R.drawable.ic_play_next,
            R.string.play_next,
            shouldNotSwipeViewHolder = { viewHolder -> viewHolder is AlbumsViewHolder },
            swipeCallback = { position ->
                presenter.onPlayNextCompositionClicked(position - 1)
            })
        val itemTouchHelper = ItemTouchHelper(callback)
        itemTouchHelper.attachToRecyclerView(viewBinding.recyclerView)

        viewBinding.fab.setOnClickListener { presenter.onPlayAllButtonClicked() }
        ViewUtils.onLongVibrationClick(viewBinding.fab, presenter::onChangeRandomModePressed)

        slidrInterface = SlidrPanel.simpleSwipeBack(
            viewBinding.listContainer,
            this,
            toolbar::onStackFragmentSlided
        )
        val fm = childFragmentManager
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
        choosePlayListDialogRunner = DialogFragmentRunner(
            fm,
            Tags.SELECT_PLAYLIST_TAG
        ) { f -> f.setOnCompleteListener(presenter::onPlayListToAddingSelected) }
        compositionActionDialogRunner = DialogFragmentRunner(
            fm,
            Tags.COMPOSITION_ACTION_TAG
        ) { f -> f.setOnTripleCompleteListener(this::onCompositionActionSelected) }
        editArtistNameDialogRunner = DialogFragmentRunner(
            fm,
            Tags.ARTIST_NAME_TAG
        ) { fragment -> fragment.setComplexCompleteListener { name, extra ->
            presenter.onNewArtistNameEntered(name, extra.getLong(Constants.Arguments.ID_ARG))
        } }
        progressDialogRunner = DialogFragmentDelayRunner(fm, Tags.PROGRESS_DIALOG_TAG)
    }

    override fun onFragmentMovedOnTop() {
//        super.onFragmentMovedOnTop();
        presenter.onFragmentMovedToTop()
        val toolbar: AdvancedToolbar = requireActivity().findViewById(R.id.toolbar)
        toolbar.setupSearch(null, null)
        toolbar.setTitleClickListener(null)
        toolbar.setupSelectionModeMenu(R.menu.library_compositions_selection_menu, this::onActionModeItemClicked)
        toolbar.setupOptionsMenu(R.menu.artist_menu, this::onOptionsItemClicked)
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

    override fun showArtistInfo(artist: Artist) {
        toolbar.title = artist.name
        toolbar.subtitle = FormatUtils.formatArtistAdditionalInfo(
            requireContext(),
            artist,
            R.drawable.ic_description_text_circle_inverse
        )
    }

    override fun showEmptyList() {
        viewBinding.fab.visibility = View.GONE
        viewBinding.progressStateView.hideAll()
    }

    override fun showEmptySearchResult() {
        viewBinding.fab.visibility = View.GONE
        viewBinding.progressStateView.showMessage(R.string.compositions_for_search_not_found)
    }

    override fun showList() {
        viewBinding.fab.visibility = View.VISIBLE
        viewBinding.progressStateView.hideAll()
    }

    override fun showLoading() {
        viewBinding.progressStateView.showProgress()
    }

    override fun showLoadingError(errorCommand: ErrorCommand) {
        viewBinding.progressStateView.showMessage(errorCommand.message, true)
    }

    override fun updateList(compositions: List<Composition>) {
        val list: MutableList<Any> = ArrayList()
        list.add(artistAlbumsPresenter)
        list.addAll(compositions)
        adapter.submitList(list)
        showNoCompositionsRunner.setCondition(compositions.isEmpty())
        artistAlbumsPresenter.setCompositionsTitleVisible(compositions.isNotEmpty())
    }

    override fun restoreListPosition(listPosition: ListPosition) {
        ViewUtils.scrollToPosition(layoutManager, listPosition)
    }

    override fun showArtistAlbums(albums: List<Album>) {
        artistAlbumsPresenter.submitAlbums(albums)
        showNoCompositionsRunner.setCondition(albums.isEmpty())
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

    override fun showSelectPlayListDialog() {
        val dialog = if (toolbar.isInActionMode) {
            newChoosePlayListDialogFragment(R.attr.actionModeStatusBarColor)
        } else {
            ChoosePlayListDialogFragment()
        }
        choosePlayListDialogRunner.show(dialog)
    }

    override fun showConfirmDeleteDialog(compositionsToDelete: List<Composition>) {
        DialogUtils.showConfirmDeleteDialog(
            requireContext(),
            compositionsToDelete,
            presenter::onDeleteCompositionsDialogConfirmed
        )
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

    override fun shareCompositions(selectedCompositions: Collection<Composition>) {
        DialogUtils.shareCompositions(requireContext(), selectedCompositions)
    }

    override fun showCurrentComposition(currentComposition: CurrentComposition) {
        adapter.showCurrentComposition(currentComposition)
    }

    override fun setDisplayCoversEnabled(isCoversEnabled: Boolean) {
        adapter.setCoversEnabled(isCoversEnabled)
    }

    override fun showRandomMode(isRandomModeEnabled: Boolean) {
        FormatUtils.formatPlayAllButton(viewBinding.fab, isRandomModeEnabled)
    }

    override fun showCompositionActionDialog(composition: Composition, position: Int) {
        val extra = Bundle()
        extra.putInt(Constants.Arguments.POSITION_ARG, position)
        @AttrRes val statusBarColor = if (toolbar.isInActionMode) R.attr.actionModeStatusBarColor else android.R.attr.statusBarColor
        val fragment = CompositionActionDialogFragment.newInstance(
            composition,
            R.menu.composition_actions_menu,
            statusBarColor,
            extra)
        compositionActionDialogRunner.show(fragment)
    }

    override fun showErrorMessage(errorCommand: ErrorCommand) {
        editorErrorHandler.handleError(errorCommand) {
            MessagesUtils.makeSnackbar(viewBinding.listContainer, errorCommand.message, Snackbar.LENGTH_LONG).show()
        }
    }

    override fun onCompositionsAddedToPlayNext(compositions: List<Composition>) {
        val message = MessagesUtils.getPlayNextMessage(requireContext(), compositions)
        MessagesUtils.makeSnackbar(viewBinding.listContainer, message, Snackbar.LENGTH_SHORT).show()
    }

    override fun onCompositionsAddedToQueue(compositions: List<Composition>) {
        val message = MessagesUtils.getAddedToQueueMessage(requireContext(), compositions)
        MessagesUtils.makeSnackbar(viewBinding.listContainer, message, Snackbar.LENGTH_SHORT).show()
    }

    override fun closeScreen() {
        FragmentNavigation.from(parentFragmentManager()).goBack()
    }

    override fun showRenameArtistDialog(artist: Artist) {
        val bundle = Bundle()
        bundle.putLong(Constants.Arguments.ID_ARG, artist.id)
        val fragment = InputTextDialogFragment.Builder(R.string.change_name,
            R.string.change,
            R.string.cancel,
            R.string.name,
            artist.name)
            .canBeEmpty(false)
            .extra(bundle)
            .build()
        editArtistNameDialogRunner.show(fragment)
    }

    override fun showRenameProgress() {
        val fragment = ProgressDialogFragment.newInstance(R.string.rename_progress)
        progressDialogRunner.show(fragment)
    }

    override fun hideRenameProgress() {
        progressDialogRunner.cancel()
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
        FragmentNavigation.from(parentFragmentManager())
            .addNewFragment(newAlbumItemsFragment(album.id))
    }

    private fun onOptionsItemClicked(item: MenuItem) {
        when (item.itemId) {
            R.id.menu_rename -> {
                presenter.onRenameArtistClicked()
            }
        }
    }

    private fun showEditorRequestDeniedMessage() {
        MessagesUtils.makeSnackbar(viewBinding.listContainer, R.string.android_r_edit_file_permission_denied, Snackbar.LENGTH_LONG).show()
    }

}