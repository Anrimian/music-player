package com.github.anrimian.musicplayer.ui.library.folders.volumes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.anrimian.musicplayer.AppConstants.Arguments.ID_ARG
import com.github.anrimian.musicplayer.AppConstants.Tags
import com.github.anrimian.musicplayer.R
import com.github.anrimian.musicplayer.databinding.FragmentBaseFabListBinding
import com.github.anrimian.musicplayer.di.Components
import com.github.anrimian.musicplayer.domain.models.composition.Composition
import com.github.anrimian.musicplayer.domain.models.folders.IgnoredFolder
import com.github.anrimian.musicplayer.domain.models.folders.Volume
import com.github.anrimian.musicplayer.ui.common.applyFabBottomInsets
import com.github.anrimian.musicplayer.ui.common.dialogs.shareCompositions
import com.github.anrimian.musicplayer.ui.common.error.ErrorCommand
import com.github.anrimian.musicplayer.ui.common.format.FormatUtils
import com.github.anrimian.musicplayer.ui.common.format.showSnackbar
import com.github.anrimian.musicplayer.ui.common.menu.PopupMenuWindow
import com.github.anrimian.musicplayer.ui.common.toolbar.AdvancedToolbar
import com.github.anrimian.musicplayer.ui.common.view.ViewUtils
import com.github.anrimian.musicplayer.ui.equalizer.EqualizerDialogFragment
import com.github.anrimian.musicplayer.ui.library.common.library.BaseLibraryFragment
import com.github.anrimian.musicplayer.ui.library.common.library.BaseLibraryPresenter
import com.github.anrimian.musicplayer.ui.library.folders.LibraryFoldersFragment
import com.github.anrimian.musicplayer.ui.library.folders.volumes.adapter.VolumesAdapter
import com.github.anrimian.musicplayer.ui.playlists.choose.ChoosePlayListDialogFragment
import com.github.anrimian.musicplayer.ui.settings.folders.ExcludedFoldersFragment
import com.github.anrimian.musicplayer.ui.sleep_timer.SleepTimerDialogFragment
import com.github.anrimian.musicplayer.ui.utils.applyBottomInsets
import com.github.anrimian.musicplayer.ui.utils.fragments.DialogFragmentRunner
import com.github.anrimian.musicplayer.ui.utils.fragments.navigation.FragmentNavigation
import com.github.anrimian.musicplayer.ui.utils.fragments.navigation.FragmentNavigationListener
import com.github.anrimian.musicplayer.ui.utils.fragments.safeShow
import com.github.anrimian.musicplayer.ui.utils.isTabletLand
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.RecyclerViewUtils
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.touch_helper.short_swipe.ShortSwipeCallback
import com.google.android.material.snackbar.Snackbar
import moxy.ktx.moxyPresenter

class LibraryVolumesFragment : BaseLibraryFragment(), LibraryVolumesView, FragmentNavigationListener {

    private val presenter by moxyPresenter {
        Components.getLibraryRootFolderComponent().libraryVolumesPresenter()
    }

    private lateinit var binding: FragmentBaseFabListBinding

    private lateinit var toolbar: AdvancedToolbar

    private lateinit var adapter: VolumesAdapter

    private lateinit var choosePlayListDialogRunner: DialogFragmentRunner<ChoosePlayListDialogFragment>
    private lateinit var choosePlaylistForFolderDialogRunner: DialogFragmentRunner<ChoosePlayListDialogFragment>

    override fun getLibraryPresenter(): BaseLibraryPresenter<*> = presenter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentBaseFabListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.fab.applyFabBottomInsets()
        binding.progressStateView.applyBottomInsets()
        if (isTabletLand()) {
            binding.recyclerView.applyBottomInsets()
        }

        toolbar = requireActivity().findViewById(R.id.toolbar)

        binding.progressStateView.onTryAgainClick { presenter.onRetryClicked() }

        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        RecyclerViewUtils.attachFastScroller(binding.recyclerView, true)
        adapter = VolumesAdapter(
            this,
            binding.recyclerView,
            presenter.getSelectedVolumes(),
            presenter::onVolumeClicked,
            presenter::onVolumeLongClicked,
            this::onVolumeMenuClicked
        )
        binding.recyclerView.adapter = adapter

        val callback = ShortSwipeCallback(requireContext(),
            R.drawable.ic_play_next,
            R.string.play_next,
            swipeCallback = presenter::onPlayNextVolumeClicked
        )
        val itemTouchHelper = ItemTouchHelper(callback)
        itemTouchHelper.attachToRecyclerView(binding.recyclerView)

        binding.fab.setOnClickListener { presenter.onPlayAllButtonClicked() }
        ViewUtils.onLongVibrationClick(binding.fab, presenter::onChangeRandomModePressed)

        val fm = childFragmentManager
        choosePlayListDialogRunner = DialogFragmentRunner(fm, Tags.SELECT_PLAYLIST_TAG) { fragment ->
            fragment.setOnCompleteListener(presenter::onPlayListToAddingSelected)
        }
        choosePlaylistForFolderDialogRunner = DialogFragmentRunner(
            fm,
            Tags.SELECT_PLAYLIST_FOR_FOLDER_TAG
        ) { fragment ->
            fragment.setComplexCompleteListener { playlist, bundle ->
                val folderId = bundle.getLong(ID_ARG)
                presenter.onPlayListSelected(folderId, playlist)
            }
        }
    }

    override fun onFragmentResumed() {
        presenter.onFragmentDisplayed()
        val act = requireActivity()
        act.findViewById<AdvancedToolbar>(R.id.toolbar).setup {
            subtitle = toolbar.getSubtitle()
            titleClickListener = toolbar.getTitleClickListener()

            setupSelectionModeMenu(
                R.menu.library_volumes_selection_menu,
                ::onActionModeItemClicked,
                presenter::onExitSelectionModeClicked
            )
            setupOptionsMenu(R.menu.library_volumes_menu, ::onOptionsItemClicked)
        }
    }

    override fun getCoordinatorLayout() = binding.listContainer

    override fun getFloatingActionButton() = binding.fab

    override fun showEmptyList() {
        binding.progressStateView.showMessage(R.string.compositions_on_device_not_found, false)
    }

    override fun showList() {
        binding.progressStateView.hideAll()
    }

    override fun showLoading() {
        binding.progressStateView.showProgress()
    }

    override fun showError(errorCommand: ErrorCommand) {
        binding.progressStateView.showMessage(errorCommand.message, true)
    }

    override fun updateList(list: List<Volume>) {
        adapter.submitList(list)
    }

    override fun showSelectPlayListDialog() {
        choosePlayListDialogRunner.show(ChoosePlayListDialogFragment())
    }

    override fun showSelectPlayListFromVolumeDialog(volume: Volume) {
        val bundle = Bundle()
        bundle.putLong(ID_ARG, volume.rootFolderId)
        val dialog = ChoosePlayListDialogFragment.newInstance(bundle)
        choosePlaylistForFolderDialogRunner.show(dialog)
    }

    override fun sendCompositions(compositions: List<Composition>) {
        shareCompositions(this, compositions)
    }

    override fun goToFolderScreen(folderId: Long) {
        FragmentNavigation.from(parentFragmentManager)
            .addNewFragment(LibraryFoldersFragment.newInstance(folderId))
    }

    override fun showRandomMode(isRandomModeEnabled: Boolean) {
        FormatUtils.formatPlayAllButton(binding.fab, isRandomModeEnabled)
    }

    override fun showSelectionMode(count: Int) {
        toolbar.showSelectionMode(count)
    }

    override fun onItemSelected(item: Volume, position: Int) {
        adapter.setItemSelected(position)
    }

    override fun onItemUnselected(item: Volume, position: Int) {
        adapter.setItemUnselected(position)
    }

    override fun setItemsSelected(selected: Boolean) {
        adapter.setItemsSelected(selected)
    }

    override fun showAddedIgnoredFolderMessage(folder: IgnoredFolder) {
        val message = getString(R.string.ignored_folder_added, folder.path)
        binding.listContainer.showSnackbar(
            message,
            duration = Snackbar.LENGTH_LONG,
            anchorView = binding.fab,
            actionText = getString(R.string.cancel),
            action = presenter::onRemoveIgnoredFolderClicked
        )
    }

    private fun onActionModeItemClicked(menuItem: MenuItem) {
        when (menuItem.itemId) {
            R.id.menu_play -> presenter.onPlayAllSelectedClicked()
            R.id.menu_select_all -> presenter.onSelectAllButtonClicked()
            R.id.menu_play_next -> presenter.onPlayNextSelectedSourcesClicked()
            R.id.menu_add_to_queue -> presenter.onAddToQueueSelectedSourcesClicked()
            R.id.menu_add_to_playlist -> presenter.onAddSelectedSourcesToPlayListClicked()
            R.id.menu_share -> presenter.onShareSelectedSourcesClicked()
        }
    }

    private fun onVolumeMenuClicked(view: View, volume: Volume) {
        PopupMenuWindow.showPopup(view, R.menu.volume_item_menu) { item ->
            when (item.itemId) {
                R.id.menu_play -> presenter.onPlayVolumeClicked(volume)
                R.id.menu_play_next -> presenter.onPlayNextVolumeClicked(volume)
                R.id.menu_add_to_queue -> presenter.onAddToQueueVolumeClicked(volume)
                R.id.menu_add_to_playlist -> presenter.onAddVolumeToPlayListButtonClicked(volume)
                R.id.menu_share -> presenter.onShareVolumeClicked(volume)
                R.id.menu_hide -> presenter.onExcludeFolderClicked(volume)
            }
        }
    }

    private fun onOptionsItemClicked(item: MenuItem) {
        when (item.itemId) {
            R.id.menu_excluded_folders -> {
                val parentFragment = parentFragment
                if (parentFragment != null) {
                    FragmentNavigation.from(parentFragment.parentFragmentManager)
                        .addNewFragment(ExcludedFoldersFragment())
                }
            }
            R.id.menu_sleep_timer -> SleepTimerDialogFragment().safeShow(childFragmentManager)
            R.id.menu_equalizer -> EqualizerDialogFragment().safeShow(childFragmentManager)
        }
    }

}