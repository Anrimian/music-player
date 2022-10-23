package com.github.anrimian.musicplayer.ui.playlist_screens.playlists

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.anrimian.musicplayer.R
import com.github.anrimian.musicplayer.databinding.FragmentPlayListsBinding
import com.github.anrimian.musicplayer.di.Components
import com.github.anrimian.musicplayer.domain.models.playlist.PlayList
import com.github.anrimian.musicplayer.domain.models.utils.ListPosition
import com.github.anrimian.musicplayer.ui.common.dialogs.showConfirmDeleteDialog
import com.github.anrimian.musicplayer.ui.common.error.ErrorCommand
import com.github.anrimian.musicplayer.ui.common.format.MessagesUtils
import com.github.anrimian.musicplayer.ui.common.menu.PopupMenuWindow
import com.github.anrimian.musicplayer.ui.common.toolbar.AdvancedToolbar
import com.github.anrimian.musicplayer.ui.common.view.ViewUtils
import com.github.anrimian.musicplayer.ui.playlist_screens.create.CreatePlayListDialogFragment
import com.github.anrimian.musicplayer.ui.playlist_screens.playlist.newPlayListFragment
import com.github.anrimian.musicplayer.ui.playlist_screens.playlists.adapter.PlayListsAdapter
import com.github.anrimian.musicplayer.ui.playlist_screens.rename.RenamePlayListDialogFragment
import com.github.anrimian.musicplayer.ui.utils.fragments.navigation.FragmentLayerListener
import com.github.anrimian.musicplayer.ui.utils.fragments.navigation.FragmentNavigation
import com.github.anrimian.musicplayer.ui.utils.fragments.safeShow
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.RecyclerViewUtils
import com.google.android.material.snackbar.Snackbar
import moxy.MvpAppCompatFragment
import moxy.ktx.moxyPresenter

class PlayListsFragment : MvpAppCompatFragment(), PlayListsView, FragmentLayerListener {

    private val presenter by moxyPresenter { Components.getAppComponent().playListsPresenter() }

    private lateinit var viewBinding: FragmentPlayListsBinding

    private lateinit var adapter: PlayListsAdapter
    private lateinit var layoutManager: LinearLayoutManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewBinding = FragmentPlayListsBinding.inflate(inflater, container, false)
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        layoutManager = LinearLayoutManager(context)
        viewBinding.recyclerView.layoutManager = layoutManager

        RecyclerViewUtils.attachFastScroller(viewBinding.recyclerView)

        adapter = PlayListsAdapter(
            viewBinding.recyclerView,
            this::goToPlayListScreen,
            this::onPlaylistMenuClicked
        )

        viewBinding.recyclerView.adapter = adapter
        viewBinding.fab.setOnClickListener { onCreatePlayListButtonClicked() }
    }

    override fun onFragmentMovedOnTop() {
        requireActivity().findViewById<AdvancedToolbar>(R.id.toolbar).setup { config ->
            config.setTitle(R.string.play_lists)
            config.setSubtitle(null)
        }
        presenter.onFragmentMovedToTop()
    }

    override fun onStop() {
        super.onStop()
        presenter.onStop(ViewUtils.getListPosition(layoutManager))
    }

    override fun showEmptyList() {
        viewBinding.progressStateView.showMessage(R.string.play_lists_on_device_not_found, false)
    }

    override fun showList() {
        viewBinding.progressStateView.hideAll()
    }

    override fun showLoading() {
        viewBinding.progressStateView.showProgress()
    }

    override fun updateList(lists: List<PlayList>) {
        adapter.submitList(lists)
    }

    override fun restoreListPosition(listPosition: ListPosition) {
        ViewUtils.scrollToPosition(layoutManager, listPosition)
    }

    override fun showConfirmDeletePlayListDialog(playList: PlayList) {
        showConfirmDeleteDialog(requireContext(), playList) {
            presenter.onDeletePlayListDialogConfirmed(playList)
        }
    }

    override fun showPlayListDeleteSuccess(playList: PlayList) {
        MessagesUtils.makeSnackbar(
            viewBinding.listContainer,
            getString(R.string.play_list_deleted, playList.name),
            Snackbar.LENGTH_SHORT
        ).show()
    }

    override fun showDeletePlayListError(errorCommand: ErrorCommand) {
        MessagesUtils.makeSnackbar(
            viewBinding.listContainer,
            getString(R.string.play_list_delete_error, errorCommand.message),
            Snackbar.LENGTH_SHORT
        ).show()
    }

    override fun showEditPlayListNameDialog(playList: PlayList) {
        RenamePlayListDialogFragment.newInstance(playList.id).safeShow(childFragmentManager)
    }

    private fun onPlaylistMenuClicked(playList: PlayList, view: View) {
        PopupMenuWindow.showPopup(view, R.menu.play_list_menu) { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_change_play_list_name -> {
                    presenter.onChangePlayListNameButtonClicked(playList)
                }
                R.id.menu_delete_play_list -> {
                    presenter.onDeletePlayListButtonClicked(playList)
                }
            }
        }
    }

    private fun onCreatePlayListButtonClicked() {
        val fragment = CreatePlayListDialogFragment()
        fragment.safeShow(childFragmentManager)
    }

    private fun goToPlayListScreen(playList: PlayList) {
        FragmentNavigation.from(parentFragmentManager)
            .addNewFragment(newPlayListFragment(playList.id))
    }
}