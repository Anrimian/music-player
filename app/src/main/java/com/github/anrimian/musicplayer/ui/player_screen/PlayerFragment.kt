package com.github.anrimian.musicplayer.ui.player_screen

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.annotation.DrawableRes
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.graphics.drawable.DrawerArrowDrawable
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.core.view.GravityCompat
import androidx.core.view.updateLayoutParams
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.github.anrimian.filesync.models.state.file.FileSyncState
import com.github.anrimian.musicplayer.Constants
import com.github.anrimian.musicplayer.Constants.Tags
import com.github.anrimian.musicplayer.R
import com.github.anrimian.musicplayer.data.utils.Permissions
import com.github.anrimian.musicplayer.databinding.FragmentDrawerBinding
import com.github.anrimian.musicplayer.databinding.PartialDetailedMusicBinding
import com.github.anrimian.musicplayer.databinding.PartialDrawerHeaderBinding
import com.github.anrimian.musicplayer.databinding.PartialQueueToolbarBinding
import com.github.anrimian.musicplayer.di.Components
import com.github.anrimian.musicplayer.domain.interactors.sleep_timer.NO_TIMER
import com.github.anrimian.musicplayer.domain.models.Screens
import com.github.anrimian.musicplayer.domain.models.composition.Composition
import com.github.anrimian.musicplayer.domain.models.composition.DeletedComposition
import com.github.anrimian.musicplayer.domain.models.play_queue.PlayQueueItem
import com.github.anrimian.musicplayer.domain.models.player.modes.RepeatMode
import com.github.anrimian.musicplayer.domain.models.scanner.FileScannerState
import com.github.anrimian.musicplayer.domain.models.scanner.Running
import com.github.anrimian.musicplayer.domain.models.utils.CompositionHelper
import com.github.anrimian.musicplayer.domain.models.volume.VolumeState
import com.github.anrimian.musicplayer.ui.about.AboutAppFragment
import com.github.anrimian.musicplayer.ui.common.compat.CompatUtils
import com.github.anrimian.musicplayer.ui.common.dialogs.shareComposition
import com.github.anrimian.musicplayer.ui.common.dialogs.showConfirmDeleteDialog
import com.github.anrimian.musicplayer.ui.common.dialogs.speed.SpeedSelectorDialogFragment
import com.github.anrimian.musicplayer.ui.common.error.ErrorCommand
import com.github.anrimian.musicplayer.ui.common.format.FormatUtils
import com.github.anrimian.musicplayer.ui.common.format.MessagesUtils
import com.github.anrimian.musicplayer.ui.common.format.getVolumeIcon
import com.github.anrimian.musicplayer.ui.common.format.showFileSyncState
import com.github.anrimian.musicplayer.ui.common.getNavigationViewPrimaryColorLight
import com.github.anrimian.musicplayer.ui.common.menu.PopupMenuWindow
import com.github.anrimian.musicplayer.ui.common.menu.showVolumePopup
import com.github.anrimian.musicplayer.ui.common.navigation.ScreensMap
import com.github.anrimian.musicplayer.ui.common.toolbar.AdvancedToolbar
import com.github.anrimian.musicplayer.ui.common.view.onRewindHold
import com.github.anrimian.musicplayer.ui.common.view.setSmallDrawableStart
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
import com.github.anrimian.musicplayer.ui.library.folders.root.newLibraryFoldersRootFragment
import com.github.anrimian.musicplayer.ui.library.genres.items.GenreItemsFragment
import com.github.anrimian.musicplayer.ui.library.genres.list.GenresListFragment
import com.github.anrimian.musicplayer.ui.main.setup.SetupFragment
import com.github.anrimian.musicplayer.ui.player_screen.lyrics.LyricsFragment
import com.github.anrimian.musicplayer.ui.player_screen.queue.PlayQueueFragment
import com.github.anrimian.musicplayer.ui.player_screen.view.drawer.DrawerLockStateProcessor
import com.github.anrimian.musicplayer.ui.player_screen.view.wrappers.PlayerPanelWrapper
import com.github.anrimian.musicplayer.ui.player_screen.view.wrappers.PlayerPanelWrapperImpl
import com.github.anrimian.musicplayer.ui.player_screen.view.wrappers.TabletPlayerPanelWrapper
import com.github.anrimian.musicplayer.ui.player_screen.view.wrappers.attachPlayerPagerWrapper
import com.github.anrimian.musicplayer.ui.playlist_screens.choose.ChoosePlayListDialogFragment
import com.github.anrimian.musicplayer.ui.playlist_screens.playlist.PlayListFragment
import com.github.anrimian.musicplayer.ui.playlist_screens.playlists.PlayListsFragment
import com.github.anrimian.musicplayer.ui.settings.SettingsFragment
import com.github.anrimian.musicplayer.ui.sleep_timer.SleepTimerDialogFragment
import com.github.anrimian.musicplayer.ui.utils.AndroidUtils
import com.github.anrimian.musicplayer.ui.utils.ViewUtils.animateVisibility
import com.github.anrimian.musicplayer.ui.utils.fragments.BackButtonListener
import com.github.anrimian.musicplayer.ui.utils.fragments.DialogFragmentRunner
import com.github.anrimian.musicplayer.ui.utils.fragments.navigation.FragmentNavigation
import com.github.anrimian.musicplayer.ui.utils.fragments.safeShow
import com.github.anrimian.musicplayer.ui.utils.moveToParent
import com.github.anrimian.musicplayer.ui.utils.reduceDragSensitivityBy
import com.github.anrimian.musicplayer.ui.utils.views.drawer.SimpleDrawerListener
import com.github.anrimian.musicplayer.ui.utils.views.seek_bar.SeekBarViewWrapper
import com.github.anrimian.musicplayer.ui.utils.views.view_pager.FragmentPagerAdapter
import com.google.android.material.snackbar.Snackbar
import io.reactivex.rxjava3.disposables.CompositeDisposable
import moxy.ktx.moxyPresenter

/**
 * Created on 19.10.2017.
 */
class PlayerFragment : BaseLibraryFragment(), BackButtonListener, PlayerView {

    companion object {
        private const val NO_ITEM = -1
        private const val SELECTED_DRAWER_ITEM = "selected_drawer_item"

        fun newInstance(
            openPlayQueue: Boolean = false,
            playlistUriStr: String? = null
        ) = PlayerFragment().apply {
            arguments = Bundle().apply {
                putBoolean(Constants.Arguments.OPEN_PLAYER_PANEL_ARG, openPlayQueue)
                putString(Constants.Arguments.PLAYLIST_IMPORT_ARG, playlistUriStr)
            }
        }
    }

    private val presenter by moxyPresenter { Components.getLibraryComponent().playerPresenter() }

    private lateinit var binding: FragmentDrawerBinding
    private lateinit var panelBinding: PartialDetailedMusicBinding
    private lateinit var drawerHeaderBinding: PartialDrawerHeaderBinding
    private lateinit var toolbarPlayQueueBinding: PartialQueueToolbarBinding

    private lateinit var toolbar: AdvancedToolbar

    private var selectedDrawerItemId = NO_ITEM
    private var itemIdToStart = NO_ITEM

    private lateinit var seekBarViewWrapper: SeekBarViewWrapper
    private lateinit var drawerLockStateProcessor: DrawerLockStateProcessor

    private val viewDisposable = CompositeDisposable()

    private lateinit var navigation: FragmentNavigation
    private lateinit var playerPanelWrapper: PlayerPanelWrapper

    private lateinit var deletingErrorHandler: ErrorHandler

    private lateinit var choosePlayListFragmentRunner: DialogFragmentRunner<ChoosePlayListDialogFragment>
    private lateinit var speedDialogFragmentRunner: DialogFragmentRunner<SpeedSelectorDialogFragment>

    private var previousCoverComposition: Composition? = null

    override fun getLibraryPresenter(): BaseLibraryPresenter<*> = presenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentDrawerBinding.inflate(inflater, container, false)
        toolbar = binding.toolbar.root
        toolbarPlayQueueBinding = binding.toolbarPlayQueue
        panelBinding = binding.clMusicPanel!!
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        AndroidUtils.setNavigationBarColorAttr(requireActivity(), R.attr.playerPanelBackground)

        toolbar.initializeViews(requireActivity().window)
        toolbar.setupWithActivity(requireActivity() as AppCompatActivity)

        navigation = FragmentNavigation.from(childFragmentManager)
        navigation.initialize(binding.drawerFragmentContainer!!, savedInstanceState)
        navigation.checkForEqualityOnReplace(true)
        navigation.setExitAnimation(R.anim.anim_slide_out_right)
        navigation.setEnterAnimation(R.anim.anim_slide_in_right)
        navigation.setRootExitAnimation(R.anim.anim_alpha_disappear)

        drawerLockStateProcessor = DrawerLockStateProcessor(binding.drawer)
        drawerLockStateProcessor.setupWithNavigation(navigation)
        viewDisposable.add(
            toolbar.getSearchModeObservable().subscribe(drawerLockStateProcessor::onSearchModeChanged)
        )
        viewDisposable.add(
            toolbar.getSelectionModeObservable().subscribe(drawerLockStateProcessor::onSelectionModeChanged)
        )

        val mlBottomSheet = binding.root.findViewById<MotionLayout>(R.id.ml_bottom_sheet)
        playerPanelWrapper = if (mlBottomSheet == null) {
            TabletPlayerPanelWrapper(
                view,
                toolbar,
                drawerLockStateProcessor::onBottomSheetOpened
            )
        } else {
            PlayerPanelWrapperImpl(
                view,
                binding,
                panelBinding,
                mlBottomSheet,
                requireActivity(),
                savedInstanceState,
                presenter::onBottomPanelCollapsed,
                presenter::onBottomPanelExpanded,
                drawerLockStateProcessor::onBottomSheetOpened
            )
        }

        binding.navigationView.setNavigationItemSelectedListener(this::onNavigationItemSelected)
        val headerView = binding.navigationView.inflateHeaderView(R.layout.partial_drawer_header)
        headerView.setBackgroundColor(requireContext().getNavigationViewPrimaryColorLight())
        headerView.updateLayoutParams<LinearLayout.LayoutParams> {
            height += AndroidUtils.getStatusBarHeight(requireContext())
        }
        drawerHeaderBinding = PartialDrawerHeaderBinding.bind(headerView)

        val drawerToggle = ActionBarDrawerToggle(
            requireActivity(),
            binding.drawer,
            R.string.open_drawer,
            R.string.close_drawer
        )
        val drawerArrowDrawable = createDrawerArrowDrawable()
        drawerToggle.drawerArrowDrawable = drawerArrowDrawable
        binding.drawer.addDrawerListener(SimpleDrawerListener(this::onDrawerClosed))

        toolbar.setupWithNavigation(navigation, drawerArrowDrawable) {
            playerPanelWrapper.isBottomPanelExpanded
        }

        panelBinding.ivSkipToPrevious.setOnClickListener { presenter.onSkipToPreviousButtonClicked() }
        panelBinding.ivSkipToPrevious.onRewindHold(presenter::onFastSeekBackwardCalled)
        panelBinding.ivSkipToNext.setOnClickListener { presenter.onSkipToNextButtonClicked() }
        panelBinding.ivSkipToNext.onRewindHold(presenter::onFastSeekForwardCalled)

        panelBinding.btnActionsMenu.setOnClickListener(this::onCompositionMenuClicked)
        panelBinding.topPanel.setOnClickListener { openPlayerPanel() }

        panelBinding.tvVolume.setOnClickListener { v ->
            val gravity = resources.getInteger(R.integer.volume_popup_panel_gravity)
            showVolumePopup(v, gravity)
        }

        seekBarViewWrapper = SeekBarViewWrapper(panelBinding.sbTrackState)
        seekBarViewWrapper.setProgressChangeListener(presenter::onTrackRewoundTo)
        seekBarViewWrapper.setOnSeekStartListener(presenter::onSeekStart)
        seekBarViewWrapper.setOnSeekStopListener(presenter::onSeekStop)

        CompatUtils.setOutlineTextButtonStyle(panelBinding.tvPlaybackSpeed)
        CompatUtils.setOutlineTextButtonStyle(panelBinding.tvSleepTime)
        CompatUtils.setOutlineTextButtonStyle(panelBinding.tvVolume)

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

        if (requireArguments().getBoolean(Constants.Arguments.OPEN_PLAYER_PANEL_ARG)) {
            requireArguments().remove(Constants.Arguments.OPEN_PLAYER_PANEL_ARG)
            openPlayerPanel()
        }

        if (!Permissions.hasFilePermission(requireContext())) {
            parentFragmentManager
                .beginTransaction()
                .replace(R.id.main_activity_container,
                    SetupFragment())
                .commit()
        }

        binding.vpPlayContent!!.adapter = FragmentPagerAdapter(
            this,
            listOf(::LyricsFragment, ::PlayQueueFragment)
        )
        //necessary, otherwise motion layout will be buggy:
        // tvSubtitle shouldn't be updated when motion layout is in progress state
        binding.vpPlayContent!!.offscreenPageLimit = 1
        binding.vpPlayContent!!.reduceDragSensitivityBy(4)
        attachPlayerPagerWrapper(
            binding.vpPlayContent!!,
            toolbarPlayQueueBinding,
            presenter::onPlayerContentPageChanged
        )
        toolbarPlayQueueBinding.flTitleArea.setOnClickListener(this::onPlayerTitleClicked)

        if (savedInstanceState == null) {
            val playlistImportUri = requireArguments().getString(Constants.Arguments.PLAYLIST_IMPORT_ARG)
            if (playlistImportUri == null) {
                presenter.onSetupScreenStateRequested()
            } else {
                requireArguments().remove(Constants.Arguments.PLAYLIST_IMPORT_ARG)
                selectedDrawerItemId = R.id.menu_play_lists
                binding.navigationView.setCheckedItem(R.id.menu_play_lists)
                startFragment(PlayListsFragment.newInstance(playlistImportUri))
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            if (binding.drawer.getDrawerLockMode(GravityCompat.START) != DrawerLayout.LOCK_MODE_LOCKED_CLOSED) {
                binding.drawer.openDrawer(GravityCompat.START)
            } else {
                onBackPressed()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        navigation.onSaveInstanceState(outState)
        outState.putInt(SELECTED_DRAWER_ITEM, selectedDrawerItemId)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        toolbar.release()
        drawerLockStateProcessor.release()
        viewDisposable.clear()
    }

    override fun onBackPressed(): Boolean {
        if (playerPanelWrapper.isBottomPanelExpanded) {
            playerPanelWrapper.collapseBottomPanelSmoothly()
            return true
        }
        if (binding.drawer.isDrawerOpen(GravityCompat.START)) {
            binding.drawer.closeDrawer(GravityCompat.START)
            return true
        }
        val fragment = navigation.fragmentOnTop
        var processed = (fragment is BackButtonListener && fragment.onBackPressed())
        if (!processed) {
            processed = FragmentNavigation.from(childFragmentManager).goBack()
        }
        return processed
    }

    override fun onStop() {
        super.onStop()
        AndroidUtils.clearVectorAnimationInfo(panelBinding.ivPlayPause)
    }

    override fun getCoordinatorLayout() = binding.clPlayQueueContainer!!

    override fun setButtonPanelState(expanded: Boolean) {
        if (expanded) {
            playerPanelWrapper.expandBottomPanel()
        } else {
            playerPanelWrapper.collapseBottomPanel()
        }
    }

    override fun showPlayerContentPage(position: Int) {
        binding.vpPlayContent!!.setCurrentItem(position, false)
    }

    override fun showDrawerScreen(selectedDrawerScreenId: Int, selectedPlayListScreenId: Long) {
        val itemId = ScreensMap.getMenuId(selectedDrawerScreenId)
        selectedDrawerItemId = itemId
        binding.navigationView.setCheckedItem(itemId)
        when (selectedDrawerScreenId) {
            Screens.LIBRARY -> presenter.onLibraryScreenSelected()
            Screens.PLAY_LISTS -> {
                val fragments: MutableList<Fragment> = ArrayList()
                fragments.add(PlayListsFragment.newInstance())
                if (selectedPlayListScreenId != 0L) {
                    fragments.add(PlayListFragment.newInstance(selectedPlayListScreenId))
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
            Screens.LIBRARY_FOLDERS -> fragments.add(newLibraryFoldersRootFragment())
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

    override fun showPlayerState(isPlaying: Boolean) {
        if (isPlaying) {
            AndroidUtils.setAnimatedVectorDrawable(panelBinding.ivPlayPause, R.drawable.anim_play_to_pause)
            panelBinding.ivPlayPause.contentDescription = getString(R.string.pause)
            panelBinding.ivPlayPause.setOnClickListener { presenter.onStopButtonClicked() }
        } else {
            AndroidUtils.setAnimatedVectorDrawable(panelBinding.ivPlayPause, R.drawable.anim_pause_to_play)
            panelBinding.ivPlayPause.contentDescription = getString(R.string.play)
            panelBinding.ivPlayPause.setOnClickListener { presenter.onPlayButtonClicked() }
        }
    }

    override fun showPlayErrorState(errorCommand: ErrorCommand?) {
        panelBinding.tvError.text = errorCommand?.message
    }

    override fun showCurrentQueueItem(item: PlayQueueItem?) {
        animateVisibility(binding.bottomSheetTopShadow, View.VISIBLE)
        setMusicControlsEnabled(item != null)
        if (item == null) {
            panelBinding.tvPlayedTime.text = FormatUtils.formatMilliseconds(0)
            panelBinding.tvTotalTime.text = FormatUtils.formatMilliseconds(0)
            panelBinding.sbTrackState.progress = 0
            panelBinding.tvCurrentComposition.setText(R.string.no_current_composition)
            panelBinding.tvCurrentCompositionAuthor.setText(R.string.unknown_author)
            val noCompositionMessage = getString(R.string.no_current_composition)
            panelBinding.topPanel.contentDescription =
                getString(R.string.now_playing_template, noCompositionMessage)
            panelBinding.sbTrackState.contentDescription = noCompositionMessage
            previousCoverComposition = null
        } else {
            val composition = item.composition
            val compositionName = CompositionHelper.formatCompositionName(composition)
            panelBinding.tvCurrentComposition.text = compositionName
            panelBinding.tvTotalTime.text = FormatUtils.formatMilliseconds(composition.duration)
            panelBinding.tvCurrentCompositionAuthor.text =
                FormatUtils.formatCompositionAuthor(composition, requireContext())
            seekBarViewWrapper.setMax(composition.duration)
            panelBinding.topPanel.contentDescription =
                getString(R.string.now_playing_template, compositionName)
            panelBinding.sbTrackState.contentDescription = null
        }
    }

    override fun showCurrentItemCover(item: PlayQueueItem?) {
        if (item == null) {
            previousCoverComposition = null
            panelBinding.ivMusicIcon.setImageResource(R.drawable.ic_music_placeholder)
            return
        }
        val composition = item.composition
        Components.getAppComponent()
            .imageLoader()
            .displayImageInReusableTarget(
                panelBinding.ivMusicIcon,
                composition,
                previousCoverComposition,
                R.drawable.ic_music_placeholder
            )
        previousCoverComposition = composition
    }

    override fun showRepeatMode(mode: Int) {
        @DrawableRes val iconRes = FormatUtils.getRepeatModeIcon(mode)
        panelBinding.btnInfinitePlay.setImageResource(iconRes)
        val description = getString(FormatUtils.getRepeatModeText(mode))
        panelBinding.btnInfinitePlay.contentDescription = description

        panelBinding.btnInfinitePlay.setOnClickListener { view -> onRepeatModeButtonClicked(view, mode) }
    }

    override fun showRandomPlayingButton(active: Boolean) {
        if (active) {
            AndroidUtils.setAnimatedVectorDrawable(panelBinding.btnRandomPlay, R.drawable.anim_shuffle_off_to_on)
            panelBinding.btnRandomPlay.setOnClickListener {
                presenter.onRandomPlayingButtonClicked(false)
            }
        } else {
            AndroidUtils.setAnimatedVectorDrawable(panelBinding.btnRandomPlay, R.drawable.anim_shuffle_on_to_off)
            panelBinding.btnRandomPlay.setOnClickListener {
                presenter.onRandomPlayingButtonClicked(true)
            }
        }
    }

    override fun showTrackState(currentPosition: Long, duration: Long) {
        seekBarViewWrapper.setProgress(currentPosition)
        val formattedTime = FormatUtils.formatMilliseconds(currentPosition)
        panelBinding.sbTrackState.contentDescription = getString(R.string.position_template, formattedTime)
        panelBinding.tvPlayedTime.text = formattedTime
    }

    override fun showShareCompositionDialog(composition: Composition) {
        shareComposition(this, composition)
    }

    override fun startEditCompositionScreen(id: Long) {
        startActivity(CompositionEditorActivity.newIntent(requireContext(), id))
    }

    override fun showDeletedItemMessage() {
        MessagesUtils.makeSnackbar(
            binding.clPlayQueueContainer!!,
            R.string.queue_item_removed,
            Snackbar.LENGTH_LONG
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
            MessagesUtils.makeSnackbar(
                binding.clPlayQueueContainer!!,
                getString(R.string.delete_composition_error_template, errorCommand.message),
                Snackbar.LENGTH_SHORT
            ).show()
        }
    }

    override fun showDeleteCompositionMessage(compositionsToDelete: List<DeletedComposition>) {
        val text = MessagesUtils.getDeleteCompleteMessage(requireActivity(), compositionsToDelete)
        MessagesUtils.makeSnackbar(binding.clPlayQueueContainer!!, text, Snackbar.LENGTH_SHORT).show()
    }

    override fun displayPlaybackSpeed(speed: Float) {
        panelBinding.tvPlaybackSpeed.text = getString(R.string.playback_speed_template, speed)
        panelBinding.tvPlaybackSpeed.setOnClickListener {
            speedDialogFragmentRunner.show(SpeedSelectorDialogFragment.newInstance(speed))
        }
    }

    override fun showSpeedChangeFeatureVisible(visible: Boolean) {
        panelBinding.tvPlaybackSpeed.visibility = if (visible) View.VISIBLE else View.GONE
        if (!visible) {
            speedDialogFragmentRunner.close()
        }
    }

    override fun showSleepTimerRemainingTime(remainingMillis: Long) {
        //setVisibility() doesn't work in motion layout
        if (remainingMillis == NO_TIMER) {
            panelBinding.tvSleepTime.text = ""
            panelBinding.tvSleepTime.background = null
            panelBinding.tvSleepTime.setCompoundDrawables(null, null, null, null)
            panelBinding.tvSleepTime.setOnClickListener(null)
            return
        }
        if (!panelBinding.tvSleepTime.hasOnClickListeners()) {
            //initialize, set visible

            panelBinding.tvSleepTime.setSmallDrawableStart(R.drawable.ic_timer)
            panelBinding.tvSleepTime.setBackgroundResource(R.drawable.bg_outline_text_button)
            panelBinding.tvSleepTime.setOnClickListener {
                SleepTimerDialogFragment().safeShow(childFragmentManager)
            }
        }
        panelBinding.tvSleepTime.text = FormatUtils.formatMilliseconds(remainingMillis)
    }

    override fun showFileScannerState(state: FileScannerState) {
        if (state is Running) {
            val fileName = state.composition.fileName
            drawerHeaderBinding.tvFileScannerState.text = getString(R.string.file_scanner_state, fileName)
            drawerHeaderBinding.tvFileScannerState.visibility = View.VISIBLE
        } else {
            drawerHeaderBinding.tvFileScannerState.visibility = View.INVISIBLE
        }
    }

    override fun showCurrentCompositionSyncState(syncState: FileSyncState, item: PlayQueueItem?) {
        val isFileRemote: Boolean
        val formattedState: FileSyncState
        if (item == null) {
            formattedState = FileSyncState.NotActive
            isFileRemote = false
        } else {
            formattedState = syncState
            isFileRemote = CompositionHelper.isCompositionFileRemote(item.composition)
        }
        showFileSyncState(
            formattedState,
            isFileRemote,
            panelBinding.pvFileState
        )
    }

    override fun locateCompositionInFolders(composition: Composition) {
        playerPanelWrapper.collapseBottomPanelSmoothly {
            val id = composition.id
            val currentFragment = navigation.fragmentOnTop
            if (currentFragment is LibraryFoldersRootFragment) {
                currentFragment.revealComposition(id)
            } else {
                if (selectedDrawerItemId != R.id.menu_library) {
                    selectedDrawerItemId = R.id.menu_library
                    binding.navigationView.setCheckedItem(R.id.menu_library)
                }
                presenter.onLibraryScreenSelected(Screens.LIBRARY_FOLDERS)
                startFragment(newLibraryFoldersRootFragment(id))
            }
        }
    }

    override fun showScreensSwipeEnabled(enabled: Boolean) {
        binding.vpPlayContent!!.isUserInputEnabled = enabled
    }

    override fun onVolumeChanged(volume: VolumeState) {
        val volumePercent = 100 * volume.getVolume() / volume.max
        panelBinding.tvVolume.text = getString(R.string.percentage_template, volumePercent)
        panelBinding.tvVolume.setSmallDrawableStart(getVolumeIcon(volumePercent))
    }

    fun openPlayerPanel() {
        presenter.onOpenPlayerPanelClicked()
        playerPanelWrapper.openPlayerPanel()
    }

    fun openImportPlaylistScreen(uriStr: String) {
        playerPanelWrapper.collapseBottomPanelSmoothly {
            val currentFragment = navigation.fragmentOnTop
            if (currentFragment is PlayListsFragment) {
                currentFragment.importPlaylist(uriStr)
            } else {
                if (selectedDrawerItemId != R.id.menu_play_lists) {
                    selectedDrawerItemId = R.id.menu_play_lists
                    binding.navigationView.setCheckedItem(R.id.menu_play_lists)
                    presenter.onDrawerScreenSelected(Screens.PLAY_LISTS)
                }
                navigation.newRootFragment(PlayListsFragment.newInstance(uriStr), 0, 0)
            }
        }
    }

    private fun setMusicControlsEnabled(show: Boolean) {
        panelBinding.btnActionsMenu.isEnabled = show
        panelBinding.ivSkipToNext.isEnabled = show
        panelBinding.ivSkipToPrevious.isEnabled = show
        panelBinding.ivPlayPause.isEnabled = show
        panelBinding.btnInfinitePlay.isEnabled = show
        panelBinding.btnRandomPlay.isEnabled = show
        panelBinding.sbTrackState.isEnabled = show
        panelBinding.tvPlaybackSpeed.isEnabled = show
    }

    @Suppress("unused")
    private fun onNavigationIconClicked() {
        if (binding.drawer.getDrawerLockMode(GravityCompat.START) != DrawerLayout.LOCK_MODE_LOCKED_CLOSED) {
            binding.drawer.openDrawer(GravityCompat.START)
        } else {
            onBackPressed()
        }
    }

    private fun startFragment(fragment: Fragment) {
        navigation.newRootFragment(fragment, 0, R.anim.anim_alpha_appear)
    }

    private fun clearFragment() {
        navigation.clearFragmentStack(R.anim.anim_alpha_disappear)
    }

    private fun onCompositionMenuClicked(view: View) {
        PopupMenuWindow.showPopup(view, R.menu.composition_short_actions_menu) { item ->
            when (item.itemId) {
                R.id.menu_add_to_playlist -> presenter.onAddCurrentCompositionToPlayListButtonClicked()
                R.id.menu_edit -> presenter.onEditCompositionButtonClicked()
                R.id.menu_show_in_folders -> presenter.onShowCurrentCompositionInFoldersClicked()
                R.id.menu_share -> presenter.onShareCompositionButtonClicked()
                R.id.menu_delete -> presenter.onDeleteCurrentCompositionButtonClicked()
            }
        }
    }

    private fun createDrawerArrowDrawable() = DrawerArrowDrawable(requireActivity()).apply {
        color = AndroidUtils.getColorFromAttr(requireActivity(), R.attr.toolbarTextColorPrimary)
    }

    private fun onNavigationItemSelected(item: MenuItem): Boolean {
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
        PopupMenuWindow.showPopup(view, R.menu.player_pager_menu, Gravity.BOTTOM) { item ->
            val position = when (item.itemId) {
                R.id.menu_lyrics -> 0
                else -> 1
            }
            binding.vpPlayContent!!.currentItem = position
        }
    }

    private fun showEditorRequestDeniedMessage() {
        MessagesUtils.makeSnackbar(
            binding.clPlayQueueContainer!!,
            R.string.android_r_edit_file_permission_denied,
            Snackbar.LENGTH_LONG
        ).show()
    }

}