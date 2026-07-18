package com.github.anrimian.musicplayer.ui.player_screen

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.github.anrimian.fsync.models.state.file.FileSyncState
import com.github.anrimian.musicplayer.AppConstants
import com.github.anrimian.musicplayer.AppConstants.Tags
import com.github.anrimian.musicplayer.R
import com.github.anrimian.musicplayer.data.utils.Permissions
import com.github.anrimian.musicplayer.databinding.FragmentPlayerBinding
import com.github.anrimian.musicplayer.databinding.PartialDrawerHeaderBinding
import com.github.anrimian.musicplayer.di.Components
import com.github.anrimian.musicplayer.domain.interactors.player.screen.ActionState
import com.github.anrimian.musicplayer.domain.interactors.player.screen.CurrentAction
import com.github.anrimian.musicplayer.domain.interactors.player.screen.MissingCompositions
import com.github.anrimian.musicplayer.domain.interactors.player.screen.NoAction
import com.github.anrimian.musicplayer.domain.interactors.player.screen.ScannerRunning
import com.github.anrimian.musicplayer.domain.models.Screens
import com.github.anrimian.musicplayer.domain.models.composition.Composition
import com.github.anrimian.musicplayer.domain.models.composition.DeletedComposition
import com.github.anrimian.musicplayer.domain.models.play_queue.PlayQueueItem
import com.github.anrimian.musicplayer.domain.models.player.modes.RepeatMode
import com.github.anrimian.musicplayer.domain.models.volume.VolumeState
import com.github.anrimian.musicplayer.ui.about.AboutAppFragment
import com.github.anrimian.musicplayer.ui.common.applyDrawerHeaderInsets
import com.github.anrimian.musicplayer.ui.common.dialogs.missing.MissingFilesDialogFragment
import com.github.anrimian.musicplayer.ui.common.dialogs.shareComposition
import com.github.anrimian.musicplayer.ui.common.dialogs.showConfirmDeleteDialog
import com.github.anrimian.musicplayer.ui.common.dialogs.speed.SpeedSelectorDialogFragment
import com.github.anrimian.musicplayer.ui.common.error.ErrorCommand
import com.github.anrimian.musicplayer.ui.common.format.MessagesUtils
import com.github.anrimian.musicplayer.ui.common.format.getMissingFilesMessage
import com.github.anrimian.musicplayer.ui.common.format.showSnackbar
import com.github.anrimian.musicplayer.ui.common.menu.PopupMenuWindow
import com.github.anrimian.musicplayer.ui.common.menu.composition.showCompositionPopupMenu
import com.github.anrimian.musicplayer.ui.common.menu.showVolumePopup
import com.github.anrimian.musicplayer.ui.common.navigation.ScreensMap
import com.github.anrimian.musicplayer.ui.common.toolbar.ToolbarBackgroundDrawable
import com.github.anrimian.musicplayer.ui.common.view.attachSystemBarsColor
import com.github.anrimian.musicplayer.ui.editor.common.DeleteErrorHandler
import com.github.anrimian.musicplayer.ui.editor.common.ErrorHandler
import com.github.anrimian.musicplayer.ui.editor.composition.CompositionEditorActivity
import com.github.anrimian.musicplayer.ui.library.albums.items.AlbumItemsFragment
import com.github.anrimian.musicplayer.ui.library.albums.list.AlbumsListFragment
import com.github.anrimian.musicplayer.ui.library.artists.items.ArtistItemsFragment
import com.github.anrimian.musicplayer.ui.library.artists.list.ArtistsListFragment
import com.github.anrimian.musicplayer.ui.library.common.library.BaseLibraryFragment
import com.github.anrimian.musicplayer.ui.library.common.library.BaseLibraryPresenter
import com.github.anrimian.musicplayer.ui.library.compositions.LibraryCompositionsFragment
import com.github.anrimian.musicplayer.ui.library.folders.root.LibraryFoldersRootFragment
import com.github.anrimian.musicplayer.ui.library.genres.items.GenreItemsFragment
import com.github.anrimian.musicplayer.ui.library.genres.list.GenresListFragment
import com.github.anrimian.musicplayer.ui.main.setup.SetupFragment
import com.github.anrimian.musicplayer.ui.player_screen.lyrics.LyricsFragment
import com.github.anrimian.musicplayer.ui.player_screen.queue.PlayQueueFragment
import com.github.anrimian.musicplayer.ui.player_screen.view.PlayerScreenBinder
import com.github.anrimian.musicplayer.ui.player_screen.view.wrappers.NavigationDrawerWrapper
import com.github.anrimian.musicplayer.ui.player_screen.view.wrappers.PlayerPanelWrapper
import com.github.anrimian.musicplayer.ui.player_screen.view.wrappers.PlayerPanelWrapperImpl
import com.github.anrimian.musicplayer.ui.player_screen.view.wrappers.TabletPlayerPanelWrapper
import com.github.anrimian.musicplayer.ui.player_screen.view.wrappers.ToolbarNavigationWrapper
import com.github.anrimian.musicplayer.ui.playlists.choose.ChoosePlayListDialogFragment
import com.github.anrimian.musicplayer.ui.playlists.details.PlaylistDetailsFragment
import com.github.anrimian.musicplayer.ui.playlists.list.PlaylistsFragment
import com.github.anrimian.musicplayer.ui.settings.main.SettingsFragment
import com.github.anrimian.musicplayer.ui.sleep_timer.SleepTimerDialogFragment
import com.github.anrimian.musicplayer.ui.utils.AndroidUtils
import com.github.anrimian.musicplayer.ui.utils.addDrawerStateListener
import com.github.anrimian.musicplayer.ui.utils.applyTopInsets
import com.github.anrimian.musicplayer.ui.utils.attachBackPressedCallback
import com.github.anrimian.musicplayer.ui.utils.attrColor
import com.github.anrimian.musicplayer.ui.utils.fragments.DialogFragmentRunner
import com.github.anrimian.musicplayer.ui.utils.fragments.navigation.FragmentNavigation
import com.github.anrimian.musicplayer.ui.utils.fragments.navigation.attachBackPressedCallback
import com.github.anrimian.musicplayer.ui.utils.fragments.safeShow
import com.github.anrimian.musicplayer.ui.utils.ignoreBottomInsets
import com.github.anrimian.musicplayer.ui.utils.isLandscape
import com.github.anrimian.musicplayer.ui.utils.isTablet
import com.github.anrimian.musicplayer.ui.utils.moveToParent
import com.github.anrimian.musicplayer.ui.utils.onPageSelected
import com.github.anrimian.musicplayer.ui.utils.reduceDragSensitivityBy
import com.github.anrimian.musicplayer.ui.utils.views.view_pager.FragmentBuilder
import com.github.anrimian.musicplayer.ui.utils.views.view_pager.FragmentPagerAdapter
import com.google.android.material.snackbar.Snackbar
import io.reactivex.rxjava3.disposables.CompositeDisposable
import moxy.ktx.moxyPresenter

/**
 * Created on 19.10.2017.
 */
class PlayerFragment : BaseLibraryFragment(), PlayerView {

    companion object {
        private const val NO_ITEM = -1
        private const val SELECTED_DRAWER_ITEM = "selected_drawer_item"

        fun newInstance(
            openPlayQueue: Boolean = false,
            playlistUriStr: String? = null
        ) = PlayerFragment().apply {
            arguments = Bundle().apply {
                putBoolean(AppConstants.Arguments.OPEN_PLAYER_PANEL_ARG, openPlayQueue)
                putString(AppConstants.Arguments.PLAYLIST_IMPORT_ARG, playlistUriStr)
            }
        }
    }

    private val presenter by moxyPresenter { Components.getLibraryComponent().playerPresenter() }

    private lateinit var binding: FragmentPlayerBinding
    private lateinit var drawerHeaderBinding: PartialDrawerHeaderBinding

    private var selectedDrawerItemId = NO_ITEM
    private var itemIdToStart = NO_ITEM


    private val viewDisposable = CompositeDisposable()

    private lateinit var playerScreenBinder: PlayerScreenBinder

    private lateinit var navigation: FragmentNavigation

    private lateinit var navigationDrawerWrapper: NavigationDrawerWrapper
    private lateinit var toolbarNavigationWrapper: ToolbarNavigationWrapper
    private lateinit var playerPanelWrapper: PlayerPanelWrapper

    private val pagerFragments = ArrayList<FragmentBuilder>()
    private lateinit var playerPagerAdapter: FragmentPagerAdapter

    private lateinit var deletingErrorHandler: ErrorHandler

    private lateinit var choosePlayListFragmentRunner: DialogFragmentRunner<ChoosePlayListDialogFragment>
    private lateinit var speedDialogFragmentRunner: DialogFragmentRunner<SpeedSelectorDialogFragment>

    override fun getLibraryPresenter(): BaseLibraryPresenter<*> = presenter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentPlayerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        playerScreenBinder = Components.getAppComponent().playerScreenBinder()
        playerScreenBinder.setupMenu(binding.navigationView)

        binding.toolbar.applyTopInsets()
        binding.toolbarPlayQueue.applyTopInsets()
        if (!requireContext().isLandscape() || requireContext().isTablet()) {
            binding.clPlayerPagerContainer.ignoreBottomInsets()//for snackbars
        }
        requireActivity().attachSystemBarsColor()

        navigation = FragmentNavigation.from(childFragmentManager)
        navigation.initialize(binding.drawerFragmentContainer, savedInstanceState)
        navigation.checkForEqualityOnReplace(true)
        navigation.attachBackPressedCallback(requireActivity())
        navigation.setExitAnimation(R.anim.anim_slide_out_right)
        navigation.setEnterAnimation(R.anim.anim_slide_in_right)
        navigation.setRootExitAnimation(R.anim.anim_alpha_disappear)

        navigationDrawerWrapper = NavigationDrawerWrapper(binding.drawer)
        navigationDrawerWrapper.setupWithNavigation(navigation)
        viewDisposable.add(
            binding.toolbar.getSearchModeObservable()
                .subscribe(navigationDrawerWrapper::onSearchModeChanged)
        )
        viewDisposable.add(
            binding.toolbar.getSelectionModeObservable()
                .subscribe(navigationDrawerWrapper::onSelectionModeChanged)
        )
        binding.toolbar.attachBackPressedCallback(requireActivity())

        val isLargeLand = binding.clBottomSheetContainer == null
        var isBottomSheetExpanded = !isLargeLand && presenter.isPlayerPanelOpened()
        if (requireArguments().getBoolean(AppConstants.Arguments.OPEN_PLAYER_PANEL_ARG)) {
            requireArguments().remove(AppConstants.Arguments.OPEN_PLAYER_PANEL_ARG)
            presenter.onOpenPlayerPanelClicked()
            isBottomSheetExpanded = true
        }
        toolbarNavigationWrapper = ToolbarNavigationWrapper(
            binding.toolbar,
            navigation,
            isBottomSheetExpanded,
            this::onNavigationClick
        )

        playerPanelWrapper = if (binding.clBottomSheetContainer == null) {
            TabletPlayerPanelWrapper(
                binding.controlPanelView,
                toolbarNavigationWrapper,
                navigationDrawerWrapper::onBottomSheetOpened
            )
        } else {
            PlayerPanelWrapperImpl(
                binding,
                requireActivity(),
                toolbarNavigationWrapper,
                savedInstanceState,
                presenter::onBottomPanelCollapsed,
                presenter::onBottomPanelExpanded,
                navigationDrawerWrapper::onBottomSheetOpened
            )
        }

        binding.navigationView.setNavigationItemSelectedListener(this::onNavigationItemSelected)
        val headerView = binding.navigationView.inflateHeaderView(R.layout.partial_drawer_header)
        headerView.background = ToolbarBackgroundDrawable(
            attrColor(R.attr.colorPrimary),
            attrColor(android.R.attr.statusBarColor)
        )
        headerView.applyDrawerHeaderInsets()
        drawerHeaderBinding = PartialDrawerHeaderBinding.bind(headerView)
        binding.drawer.attachBackPressedCallback(requireActivity())
        binding.drawer.addDrawerStateListener(onDrawerClosed = { onDrawerClosed() })

        binding.controlPanelView.onSkipToPreviousClick { presenter.onSkipToPreviousButtonClicked() }
        binding.controlPanelView.onSkipToPreviousHold(presenter::onFastSeekBackwardCalled)
        binding.controlPanelView.onSkipToNextClick { presenter.onSkipToNextButtonClicked() }
        binding.controlPanelView.onSkipToNextHold(presenter::onFastSeekForwardCalled)

        binding.controlPanelView.onPanelClick { openPlayerPanel() }

        binding.controlPanelView.onVolumeButtonClick { v ->
            val gravity = resources.getInteger(R.integer.volume_popup_panel_gravity)
            showVolumePopup(v, gravity)
        }
        binding.controlPanelView.onRandomModeClick { presenter.onChangeRandomModeClicked() }

        binding.controlPanelView.setSeekbarListeners(
            presenter::onTrackRewoundTo,
            presenter::onSeekStart,
            presenter::onSeekStop
        )

        deletingErrorHandler = DeleteErrorHandler(
            this,
            presenter::onRetryFailedDeleteActionClicked,
            this::showEditorRequestDeniedMessage
        )

        val fm = childFragmentManager
        choosePlayListFragmentRunner = DialogFragmentRunner(fm,
            Tags.SELECT_PLAYLIST_TAG
        ) { fragment -> fragment.setOnCompleteListener(presenter::onPlayListForAddingSelected) }

        speedDialogFragmentRunner = DialogFragmentRunner(fm, Tags.SPEED_SELECTOR_TAG) { fragment ->
            fragment.setSpeedChangeListener(presenter::onPlaybackSpeedSelected)
        }

        if (!Permissions.hasFilePermission(requireContext())) {
            parentFragmentManager
                .beginTransaction()
                .replace(R.id.main_activity_container,
                    SetupFragment())
                .commit()
        }

        pagerFragments.add(FragmentBuilder(0, ::LyricsFragment))
        pagerFragments.add(FragmentBuilder(1, ::PlayQueueFragment))
        playerPagerAdapter = FragmentPagerAdapter(this, pagerFragments)
        binding.vpPlayContent.adapter = playerPagerAdapter
        binding.vpPlayContent.offscreenPageLimit = 1
        binding.vpPlayContent.reduceDragSensitivityBy(4)
        binding.vpPlayContent.onPageSelected(presenter::onPlayerContentPageChanged)
        binding.toolbarPlayQueue.initWithViewPager(binding.vpPlayContent, presenter.getPlayerContentPage())
        binding.controlPanelView.initWithViewPager(binding.vpPlayContent)

        binding.toolbarPlayQueue.setTitleClickListener(this::onPlayerTitleClicked)

        if (savedInstanceState == null) {
            val playlistImportUri = requireArguments().getString(AppConstants.Arguments.PLAYLIST_IMPORT_ARG)
            if (playlistImportUri == null) {
                presenter.onSetupScreenStateRequested()
            } else {
                requireArguments().remove(AppConstants.Arguments.PLAYLIST_IMPORT_ARG)
                selectedDrawerItemId = R.id.menu_play_lists
                binding.navigationView.setCheckedItem(R.id.menu_play_lists)
                startFragment(PlaylistsFragment.newInstance(playlistImportUri))
            }
        } else {
            selectedDrawerItemId = savedInstanceState.getInt(SELECTED_DRAWER_ITEM, NO_ITEM)
        }
    }

    @Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        //drawer header is RecyclerView item, unable to add fragment in a normal way.
        //so we add header fragment into stable container view and then move it into header
        val headerContainer = binding.flDrawerHeaderStableContainer
        if (savedInstanceState == null) {
            val headerFragment = Components.getAppComponent().specificNavigation().getDrawerHeaderFragment()
            if (headerFragment != null) {
                childFragmentManager.beginTransaction()
                    .add(headerContainer.id, headerFragment)
                    .runOnCommit {
                        headerContainer.moveToParent(drawerHeaderBinding.flDrawerHeaderContainer)
                    }
                    .commitAllowingStateLoss()
            }
        } else {
            headerContainer.moveToParent(drawerHeaderBinding.flDrawerHeaderContainer)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        navigation.onSaveInstanceState(outState)
        outState.putInt(SELECTED_DRAWER_ITEM, selectedDrawerItemId)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        toolbarNavigationWrapper.release()
        navigationDrawerWrapper.release()
        viewDisposable.clear()
    }

    override fun onStop() {
        super.onStop()
        binding.controlPanelView.clearVectorAnimationInfo()
    }

    override fun getCoordinatorLayout() = binding.clPlayerPagerContainer

    override fun setButtonPanelState(expanded: Boolean) {
        if (expanded) {
            playerPanelWrapper.expandBottomPanel(true)
        } else {
            playerPanelWrapper.collapseBottomPanel()
        }
    }

    override fun showPlayerContentPage(position: Int) {
        binding.vpPlayContent.setCurrentItem(position, false)
    }

    override fun showDrawerScreen(selectedDrawerScreenId: Int, selectedPlayListScreenId: Long) {
        val itemId = ScreensMap.getMenuId(selectedDrawerScreenId)
        selectedDrawerItemId = itemId
        binding.navigationView.setCheckedItem(itemId)
        when (selectedDrawerScreenId) {
            Screens.LIBRARY -> presenter.onLibraryScreenSelected()
            Screens.PLAY_LISTS -> {
                val fragments: MutableList<Fragment> = ArrayList()
                fragments.add(PlaylistsFragment.newInstance())
                if (selectedPlayListScreenId != 0L) {
                    fragments.add(PlaylistDetailsFragment.newInstance(selectedPlayListScreenId))
                }
                navigation.newRootFragmentStack(fragments, 0, R.anim.anim_alpha_appear)
            }
        }
    }

    override fun showLibraryScreen(
        selectedLibraryScreen: Int,
        selectedArtistScreenId: Long,
        selectedAlbumScreenId: Long,
        selectedGenreScreenId: Long
    ) {
        val fragments: MutableList<Fragment> = ArrayList()
        when (selectedLibraryScreen) {
            Screens.LIBRARY_COMPOSITIONS -> fragments.add(LibraryCompositionsFragment())
            Screens.LIBRARY_FOLDERS -> fragments.add(LibraryFoldersRootFragment.newInstance())
            Screens.LIBRARY_ARTISTS -> {
                fragments.add(ArtistsListFragment())
                if (selectedArtistScreenId != 0L) {
                    fragments.add(ArtistItemsFragment.newInstance(selectedArtistScreenId))
                    //need to fix fragment navigation lifecycle issue before implementing this
                    // (after restore title is from artist instead of album)
//                    if (selectedAlbumScreenId != 0L) {
//                        fragments.add(AlbumItemsFragment.newInstance(selectedAlbumScreenId))
//                    }
                }
            }
            Screens.LIBRARY_ALBUMS -> {
                fragments.add(AlbumsListFragment())
                if (selectedAlbumScreenId != 0L) {
                    fragments.add(AlbumItemsFragment.newInstance(selectedAlbumScreenId))
                }
            }
            Screens.LIBRARY_GENRES -> {
                fragments.add(GenresListFragment())
                if (selectedGenreScreenId != 0L) {
                    fragments.add(GenreItemsFragment.newInstance(selectedGenreScreenId))
                }
            }
        }
        navigation.newRootFragmentStack(fragments, 0, R.anim.anim_alpha_appear)
    }

    override fun showPlayingState(isPlaying: Boolean) {
        binding.controlPanelView.showPlayingState(
            isPlaying,
            { presenter.onStopButtonClicked() },
            { presenter.onPlayButtonClicked() }
        )
    }

    override fun showPlayErrorState(errorCommand: ErrorCommand?) {
        binding.controlPanelView.showPlayErrorState(errorCommand)
    }

    override fun showCurrentQueueItem(item: PlayQueueItem?) {
        binding.controlPanelView.showCurrentQueueItem(item, this::onCompositionMenuClicked)
    }

    override fun showCurrentItemCover(item: PlayQueueItem?) {
        binding.controlPanelView.showCurrentItemCover(item)
    }

    override fun showRepeatMode(mode: Int) {
        binding.controlPanelView.showRepeatMode(mode) { view ->
            onRepeatModeButtonClicked(view, mode)
        }
    }

    override fun showRandomMode(isActive: Boolean) {
        binding.controlPanelView.showRandomPlayingButton(isActive)
    }

    override fun showTrackState(currentPosition: Long, duration: Long) {
        binding.controlPanelView.showTrackState(currentPosition, duration)
    }

    override fun showDeletedItemMessage() {
        MessagesUtils.makeSnackbar(
            binding.clPlayerPagerContainer,
            R.string.queue_item_removed,
            Snackbar.LENGTH_LONG,
        ).setAction(R.string.cancel, presenter::onRestoreDeletedItemClicked)
            .show()
    }

    override fun showSelectPlayListDialog() {
        choosePlayListFragmentRunner.show(ChoosePlayListDialogFragment())
    }

    override fun showConfirmDeleteDialog(compositionsToDelete: List<Composition>) {
        showConfirmDeleteDialog(requireContext(), compositionsToDelete) {
            presenter.onDeleteCompositionsDialogConfirmed(compositionsToDelete)
        }
    }

    override fun showDeleteCompositionError(errorCommand: ErrorCommand) {
        deletingErrorHandler.handleError(errorCommand) {
            binding.clPlayerPagerContainer.showSnackbar(
                getString(R.string.delete_composition_error_template, errorCommand.message)
            )
        }
    }

    override fun showDeleteCompositionMessage(compositionsToDelete: List<DeletedComposition>) {
        val text = MessagesUtils.getDeleteCompleteMessage(requireActivity(), compositionsToDelete)
        binding.clPlayerPagerContainer.showSnackbar(text)
    }

    override fun showPlaybackSpeed(speed: Float) {
        binding.controlPanelView.showPlaybackSpeed(speed) {
            speedDialogFragmentRunner.show(SpeedSelectorDialogFragment.newInstance(speed))
        }
    }

    override fun showSpeedChangeFeatureVisible(visible: Boolean) {
        binding.controlPanelView.showSpeedChangeFeatureVisible(visible)
        if (!visible) {
            speedDialogFragmentRunner.close()
        }
    }

    override fun showSleepTimerRemainingTime(remainingMillis: Long) {
        binding.controlPanelView.showSleepTimerRemainingTime(remainingMillis) {
            SleepTimerDialogFragment().safeShow(childFragmentManager)
        }
    }

    override fun showCurrentActions(action: CurrentAction) {
        if (action == NoAction) {
            drawerHeaderBinding.tvFileScannerState.visibility = View.INVISIBLE
            drawerHeaderBinding.tvFileScannerState.setOnClickListener(null)
            return
        }
        drawerHeaderBinding.tvFileScannerState.visibility = View.VISIBLE
        val text = when(action) {
            is ScannerRunning -> {
                drawerHeaderBinding.tvFileScannerState.setOnClickListener(null)
                getString(R.string.file_scanner_state, action.composition.fileName)
            }
            is MissingCompositions -> {
                drawerHeaderBinding.tvFileScannerState.setOnClickListener {
                    MissingFilesDialogFragment().safeShow(this)
                }
                getMissingFilesMessage(requireContext(), action.count)
            }
            else -> ""
        }
        drawerHeaderBinding.tvFileScannerState.text = text
    }

    override fun showCurrentCompositionSyncState(syncState: FileSyncState?, item: PlayQueueItem?) {
        binding.controlPanelView.showCurrentCompositionSyncState(syncState, item)
    }

    override fun showScreensSwipeEnabled(enabled: Boolean) {
        binding.vpPlayContent.isUserInputEnabled = enabled
    }

    override fun onVolumeChanged(volume: Long) {
        binding.controlPanelView.showVolume(VolumeState.from(volume))
    }

    override fun showActionState(actionState: ActionState) {
        playerScreenBinder.bindActionState(binding.toolbar, actionState)
    }


    fun openPlayerPanel() {
        presenter.onOpenPlayerPanelClicked()
        playerPanelWrapper.expandBottomPanel(false)
    }

    fun openImportPlaylistScreen(uriStr: String) {
        playerPanelWrapper.collapseBottomPanelSmoothly {
            val currentFragment = navigation.fragmentOnTop
            if (currentFragment is PlaylistsFragment) {
                currentFragment.importPlaylist(uriStr)
            } else {
                if (selectedDrawerItemId != R.id.menu_play_lists) {
                    selectedDrawerItemId = R.id.menu_play_lists
                    binding.navigationView.setCheckedItem(R.id.menu_play_lists)
                    presenter.onDrawerScreenSelected(Screens.PLAY_LISTS)
                }
                navigation.newRootFragment(PlaylistsFragment.newInstance(uriStr), 0, 0)
            }
        }
    }

    fun locateCompositionInFolders(compositionId: Long) {
        playerPanelWrapper.collapseBottomPanelSmoothly {
            val currentFragment = navigation.fragmentOnTop
            if (currentFragment is LibraryFoldersRootFragment) {
                currentFragment.revealComposition(compositionId)
            } else {
                if (selectedDrawerItemId != R.id.menu_library) {
                    selectedDrawerItemId = R.id.menu_library
                    binding.navigationView.setCheckedItem(R.id.menu_library)
                }
                presenter.onLibraryScreenSelected(Screens.LIBRARY_FOLDERS)
                startFragment(LibraryFoldersRootFragment.newInstance(compositionId))
            }
        }
    }

    private fun startFragment(fragment: Fragment) {
        navigation.newRootFragment(fragment, 0, R.anim.anim_alpha_appear)
    }

    private fun clearFragment() {
        navigation.clearFragmentStack(R.anim.anim_alpha_disappear)
    }

    private fun onCompositionMenuClicked(view: View, queueItem: PlayQueueItem) {
        showCompositionPopupMenu(view, R.menu.composition_short_actions_menu, queueItem) { item ->
            when (item.itemId) {
                R.id.menu_add_to_playlist -> presenter.onAddQueueItemToPlayListButtonClicked(queueItem)
                R.id.menu_edit -> startActivity(CompositionEditorActivity.newIntent(requireContext(), queueItem.id))
                R.id.menu_show_in_folders -> locateCompositionInFolders(queueItem.id)
                R.id.menu_share -> shareComposition(this, queueItem)
                R.id.menu_delete -> presenter.onDeleteCompositionButtonClicked(queueItem)
            }
        }
    }

    private fun onNavigationItemSelected(item: MenuItem): Boolean {
        if (playerScreenBinder.onNavigationItemSelected(item, binding.navigationView)) {
            binding.drawer.closeDrawer(GravityCompat.START)
            return false
        }
        val itemId = item.itemId
        var selected = false
        when {
            itemId == R.id.menu_settings -> navigation.addNewFragment(SettingsFragment())
            itemId == R.id.menu_about -> navigation.addNewFragment(AboutAppFragment())
            itemId != selectedDrawerItemId -> {
                selectedDrawerItemId = itemId
                itemIdToStart = itemId
                clearFragment()
                selected = true
            }
        }
        binding.drawer.closeDrawer(GravityCompat.START)
        return selected
    }

    private fun onDrawerClosed() {
        if (itemIdToStart != NO_ITEM) {
            val screenId = ScreensMap.getScreenId(itemIdToStart)
            presenter.onDrawerScreenSelected(screenId)
            itemIdToStart = NO_ITEM
        }
    }

    private fun onRepeatModeButtonClicked(view: View, currentRepeatMode: Int) {
        val selectedItemId = when(currentRepeatMode) {
            RepeatMode.REPEAT_PLAY_QUEUE -> R.id.menu_repeat_queue
            RepeatMode.REPEAT_COMPOSITION -> R.id.menu_repeat_composition
            RepeatMode.PLAY_COMPOSITION_ONCE -> R.id.menu_play_once
            else -> R.id.menu_do_not_repeat
        }
        val menu = AndroidUtils.createMenu(requireContext(), R.menu.repeat_mode_menu)
        menu.findItem(selectedItemId).isChecked = true

        PopupMenuWindow.showPopup(view, menu) { item ->
            val repeatMode = when (item.itemId) {
                R.id.menu_repeat_queue -> RepeatMode.REPEAT_PLAY_QUEUE
                R.id.menu_repeat_composition -> RepeatMode.REPEAT_COMPOSITION
                R.id.menu_play_once -> RepeatMode.PLAY_COMPOSITION_ONCE
                else -> RepeatMode.NONE
            }
            presenter.onRepeatModeChanged(repeatMode)
        }
    }

    private fun onPlayerTitleClicked(view: View) {
        val menu = AndroidUtils.createMenu(requireContext(), R.menu.player_pager_menu)

        PopupMenuWindow.showPopup(view, menu, Gravity.BOTTOM) { item ->
            val position = when (item.itemId) {
                R.id.menu_lyrics -> 0
                else -> 1
            }
            binding.vpPlayContent.currentItem = position
        }
    }

    private fun showEditorRequestDeniedMessage() {
        binding.clPlayerPagerContainer.showSnackbar(
            R.string.android_r_edit_file_permission_denied,
            Snackbar.LENGTH_LONG
        )
    }

    private fun onNavigationClick() {
        if (binding.drawer.getDrawerLockMode(GravityCompat.START) != DrawerLayout.LOCK_MODE_LOCKED_CLOSED) {
            binding.drawer.openDrawer(GravityCompat.START)
        } else {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

}