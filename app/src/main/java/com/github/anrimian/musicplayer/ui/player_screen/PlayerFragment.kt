package com.github.anrimian.musicplayer.ui.player_screen

import android.os.Bundle
import android.view.*
import androidx.annotation.DrawableRes
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.graphics.drawable.DrawerArrowDrawable
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.github.anrimian.filesync.models.state.file.FormattedFileSyncState
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
import com.github.anrimian.musicplayer.domain.models.play_queue.PlayQueueItem
import com.github.anrimian.musicplayer.domain.models.player.modes.RepeatMode
import com.github.anrimian.musicplayer.domain.models.playlist.PlayList
import com.github.anrimian.musicplayer.domain.models.scanner.FileScannerState
import com.github.anrimian.musicplayer.domain.models.scanner.Running
import com.github.anrimian.musicplayer.domain.models.utils.CompositionHelper
import com.github.anrimian.musicplayer.ui.about.AboutAppFragment
import com.github.anrimian.musicplayer.ui.common.compat.CompatUtils
import com.github.anrimian.musicplayer.ui.common.dialogs.DialogUtils
import com.github.anrimian.musicplayer.ui.common.dialogs.shareComposition
import com.github.anrimian.musicplayer.ui.common.dialogs.showConfirmDeleteDialog
import com.github.anrimian.musicplayer.ui.common.error.ErrorCommand
import com.github.anrimian.musicplayer.ui.common.format.FormatUtils
import com.github.anrimian.musicplayer.ui.common.format.MessagesUtils
import com.github.anrimian.musicplayer.ui.common.format.showFileSyncState
import com.github.anrimian.musicplayer.ui.common.getNavigationViewPrimaryColorLight
import com.github.anrimian.musicplayer.ui.common.menu.PopupMenuWindow
import com.github.anrimian.musicplayer.ui.common.navigation.ScreensMap
import com.github.anrimian.musicplayer.ui.common.toolbar.AdvancedToolbar
import com.github.anrimian.musicplayer.ui.common.view.ViewUtils
import com.github.anrimian.musicplayer.ui.editor.common.DeleteErrorHandler
import com.github.anrimian.musicplayer.ui.editor.common.ErrorHandler
import com.github.anrimian.musicplayer.ui.editor.composition.newCompositionEditorIntent
import com.github.anrimian.musicplayer.ui.library.albums.list.AlbumsListFragment
import com.github.anrimian.musicplayer.ui.library.artists.list.ArtistsListFragment
import com.github.anrimian.musicplayer.ui.library.compositions.LibraryCompositionsFragment
import com.github.anrimian.musicplayer.ui.library.folders.root.LibraryFoldersRootFragment
import com.github.anrimian.musicplayer.ui.library.folders.root.newLibraryFoldersRootFragment
import com.github.anrimian.musicplayer.ui.library.genres.list.GenresListFragment
import com.github.anrimian.musicplayer.ui.player_screen.lyrics.LyricsFragment
import com.github.anrimian.musicplayer.ui.player_screen.queue.PlayQueueFragment
import com.github.anrimian.musicplayer.ui.player_screen.view.drawer.DrawerLockStateProcessor
import com.github.anrimian.musicplayer.ui.player_screen.view.wrappers.PlayerPanelWrapper
import com.github.anrimian.musicplayer.ui.player_screen.view.wrappers.PlayerPanelWrapperImpl
import com.github.anrimian.musicplayer.ui.player_screen.view.wrappers.TabletPlayerPanelWrapper
import com.github.anrimian.musicplayer.ui.player_screen.view.wrappers.attachPlayerPagerWrapper
import com.github.anrimian.musicplayer.ui.playlist_screens.choose.ChoosePlayListDialogFragment
import com.github.anrimian.musicplayer.ui.playlist_screens.playlist.newPlayListFragment
import com.github.anrimian.musicplayer.ui.playlist_screens.playlists.PlayListsFragment
import com.github.anrimian.musicplayer.ui.settings.SettingsFragment
import com.github.anrimian.musicplayer.ui.sleep_timer.SleepTimerDialogFragment
import com.github.anrimian.musicplayer.ui.start.StartFragment
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
import moxy.MvpAppCompatFragment
import moxy.ktx.moxyPresenter


/**
 * Created on 19.10.2017.
 */

private const val NO_ITEM = -1
private const val SELECTED_DRAWER_ITEM = "selected_drawer_item"

fun newPlayerFragment(openPlayQueue: Boolean = false): PlayerFragment {
    val args = Bundle()
    args.putBoolean(Constants.Arguments.OPEN_PLAYER_PANEL_ARG, openPlayQueue)
    val fragment = PlayerFragment()
    fragment.arguments = args
    return fragment
}

class PlayerFragment : MvpAppCompatFragment(), BackButtonListener, PlayerView {

    private val presenter by moxyPresenter { Components.getLibraryComponent().playerPresenter() }

    private lateinit var viewBinding: FragmentDrawerBinding
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

    private var previousCoverComposition: Composition? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        viewBinding = FragmentDrawerBinding.inflate(inflater, container, false)
        toolbar = viewBinding.toolbar.root
        toolbarPlayQueueBinding = viewBinding.toolbarPlayQueue
        panelBinding = viewBinding.clMusicPanel!!
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        AndroidUtils.setNavigationBarColorAttr(requireActivity(), R.attr.playerPanelBackground)

        toolbar.initializeViews(requireActivity().window)
        toolbar.setupWithActivity(requireActivity() as AppCompatActivity)

        navigation = FragmentNavigation.from(childFragmentManager)
        navigation.initialize(viewBinding.drawerFragmentContainer!!, savedInstanceState)
        navigation.checkForEqualityOnReplace(true)
        navigation.setExitAnimation(R.anim.anim_slide_out_right)
        navigation.setEnterAnimation(R.anim.anim_slide_in_right)
        navigation.setRootExitAnimation(R.anim.anim_alpha_disappear)

        drawerLockStateProcessor = DrawerLockStateProcessor(viewBinding.drawer)
        drawerLockStateProcessor.setupWithNavigation(navigation)
        viewDisposable.add(
            toolbar.searchModeObservable.subscribe(drawerLockStateProcessor::onSearchModeChanged)
        )
        viewDisposable.add(
            toolbar.selectionModeObservable.subscribe(drawerLockStateProcessor::onSelectionModeChanged)
        )

        val mlBottomSheet = viewBinding.root.findViewById<MotionLayout>(R.id.ml_bottom_sheet)
        playerPanelWrapper = if (mlBottomSheet == null) {
            TabletPlayerPanelWrapper(
                view,
                toolbar,
                drawerLockStateProcessor::onBottomSheetOpened
            )
        } else {
            PlayerPanelWrapperImpl(
                view,
                viewBinding,
                panelBinding,
                mlBottomSheet,
                requireActivity(),
                savedInstanceState,
                presenter::onBottomPanelCollapsed,
                presenter::onBottomPanelExpanded,
                drawerLockStateProcessor::onBottomSheetOpened
            )
        }
        viewBinding.navigationView.setNavigationItemSelectedListener(this::onNavigationItemSelected)
        val headerView = viewBinding.navigationView.inflateHeaderView(R.layout.partial_drawer_header)
        headerView.setBackgroundColor(requireContext().getNavigationViewPrimaryColorLight())
        drawerHeaderBinding = PartialDrawerHeaderBinding.bind(headerView)
        val drawerToggle = ActionBarDrawerToggle(
            requireActivity(),
            viewBinding.drawer,
            R.string.open_drawer,
            R.string.close_drawer
        )
        val drawerArrowDrawable = createDrawerArrowDrawable()
        drawerToggle.drawerArrowDrawable = drawerArrowDrawable
        viewBinding.drawer.addDrawerListener(SimpleDrawerListener(this::onDrawerClosed))

        toolbar.setupWithNavigation(navigation, drawerArrowDrawable) {
            playerPanelWrapper.isBottomPanelExpanded
        }

        panelBinding.ivSkipToPrevious.setOnClickListener { presenter.onSkipToPreviousButtonClicked() }
        ViewUtils.setOnHoldListener(panelBinding.ivSkipToPrevious, presenter::onFastSeekBackwardCalled)
        panelBinding.ivSkipToNext.setOnClickListener { presenter.onSkipToNextButtonClicked() }
        ViewUtils.setOnHoldListener(panelBinding.ivSkipToNext, presenter::onFastSeekForwardCalled)

        panelBinding.btnActionsMenu.setOnClickListener(this::onCompositionMenuClicked)
        panelBinding.topPanel.setOnClickListener { openPlayerPanel() }

        seekBarViewWrapper = SeekBarViewWrapper(panelBinding.sbTrackState)
        seekBarViewWrapper.setProgressChangeListener(presenter::onTrackRewoundTo)
        seekBarViewWrapper.setOnSeekStartListener(presenter::onSeekStart)
        seekBarViewWrapper.setOnSeekStopListener(presenter::onSeekStop)

        CompatUtils.setOutlineTextButtonStyle(panelBinding.tvPlaybackSpeed)
        CompatUtils.setOutlineTextButtonStyle(panelBinding.tvSleepTime)

        deletingErrorHandler = DeleteErrorHandler(
            this,
            presenter::onRetryFailedDeleteActionClicked,
            this::showEditorRequestDeniedMessage
        )

        choosePlayListFragmentRunner = DialogFragmentRunner(
            childFragmentManager,
            Tags.SELECT_PLAYLIST_TAG
        ) { fragment -> fragment.setOnCompleteListener(presenter::onPlayListForAddingSelected) }

        if (requireArguments().getBoolean(Constants.Arguments.OPEN_PLAYER_PANEL_ARG)) {
            requireArguments().remove(Constants.Arguments.OPEN_PLAYER_PANEL_ARG)
            openPlayerPanel()
        }

        if (!Permissions.hasFilePermission(requireContext())) {
            parentFragmentManager
                .beginTransaction()
                .replace(R.id.main_activity_container, StartFragment())
                .commit()
        }

        viewBinding.vpPlayContent!!.adapter = FragmentPagerAdapter(
            this,
            listOf(::LyricsFragment, ::PlayQueueFragment)
        )
        //necessary, otherwise motion layout will be buggy:
        // tvSubtitle shouldn't be updated when motion layout is in progress state
        viewBinding.vpPlayContent!!.offscreenPageLimit = 1
        viewBinding.vpPlayContent!!.reduceDragSensitivityBy(4)
        attachPlayerPagerWrapper(
            viewBinding.vpPlayContent!!,
            toolbarPlayQueueBinding,
            presenter::onPlayerContentPageChanged
        )
        toolbarPlayQueueBinding.flTitleArea.setOnClickListener(this::onPlayerTitleClicked)

        if (savedInstanceState == null) {
            presenter.onSetupScreenStateRequested()
        } else {
            selectedDrawerItemId = savedInstanceState.getInt(SELECTED_DRAWER_ITEM, NO_ITEM)
        }
    }

    @Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        //drawer header is RecyclerView item, unable to add fragment in a normal way.
        //so we add header fragment into stable container view and then move it into header
        val headerContainer = viewBinding.flDrawerHeaderStableContainer
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
            if (viewBinding.drawer.getDrawerLockMode(GravityCompat.START) != DrawerLayout.LOCK_MODE_LOCKED_CLOSED) {
                viewBinding.drawer.openDrawer(GravityCompat.START)
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
        if (viewBinding.drawer.isDrawerOpen(GravityCompat.START)) {
            viewBinding.drawer.closeDrawer(GravityCompat.START)
            return true
        }
        val fragment = navigation.fragmentOnTop
        var processed = (fragment is BackButtonListener && fragment.onBackPressed())
        if (!processed) {
            processed = FragmentNavigation.from(childFragmentManager).goBack()
        }
        return processed
    }

    override fun onStart() {
        super.onStart()
        presenter.onStart()
    }

    override fun onStop() {
        super.onStop()
        //battery saving
        presenter.onStop()

        AndroidUtils.clearVectorAnimationInfo(panelBinding.ivPlayPause)
    }

    override fun setButtonPanelState(expanded: Boolean) {
        if (expanded) {
            playerPanelWrapper.expandBottomPanel()
        } else {
            playerPanelWrapper.collapseBottomPanel()
        }
    }

    override fun showPlayerContentPage(position: Int) {
        viewBinding.vpPlayContent!!.setCurrentItem(position, false)
    }

    override fun showDrawerScreen(selectedDrawerScreenId: Int, selectedPlayListScreenId: Long) {
        val itemId = ScreensMap.getMenuId(selectedDrawerScreenId)
        selectedDrawerItemId = itemId
        viewBinding.navigationView.setCheckedItem(itemId)
        when (selectedDrawerScreenId) {
            Screens.LIBRARY -> presenter.onLibraryScreenSelected()
            Screens.PLAY_LISTS -> {
                val fragments: MutableList<Fragment> = ArrayList()
                fragments.add(PlayListsFragment())
                if (selectedPlayListScreenId != 0L) {
                    fragments.add(newPlayListFragment(selectedPlayListScreenId))
                }
                navigation.newRootFragmentStack(fragments, 0, R.anim.anim_alpha_appear)
            }
        }
    }

    override fun showLibraryScreen(selectedLibraryScreen: Int) {
        val fragment = when (selectedLibraryScreen) {
            Screens.LIBRARY_COMPOSITIONS -> LibraryCompositionsFragment()
            Screens.LIBRARY_FOLDERS -> newLibraryFoldersRootFragment()
            Screens.LIBRARY_ARTISTS -> ArtistsListFragment()
            Screens.LIBRARY_ALBUMS -> AlbumsListFragment()
            Screens.LIBRARY_GENRES -> GenresListFragment()
            else -> LibraryCompositionsFragment()
        }
        startFragment(fragment)
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

    override fun showCurrentQueueItem(item: PlayQueueItem?, showCover: Boolean) {
        animateVisibility(viewBinding.bottomSheetTopShadow, View.VISIBLE)
        setMusicControlsEnabled(item != null)
        if (item == null) {
            panelBinding.tvPlayedTime.text = FormatUtils.formatMilliseconds(0)
            panelBinding.tvTotalTime.text = FormatUtils.formatMilliseconds(0)
            panelBinding.sbTrackState.progress = 0
            panelBinding.tvCurrentComposition.setText(R.string.no_current_composition)
            panelBinding.tvCurrentCompositionAuthor.setText(R.string.unknown_author)
            panelBinding.ivMusicIcon.setImageResource(R.drawable.ic_music_placeholder)
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
            if (showCover) {
                Components.getAppComponent()
                    .imageLoader()
                    .displayImageInReusableTarget(
                        panelBinding.ivMusicIcon,
                        composition,
                        previousCoverComposition,
                        R.drawable.ic_music_placeholder
                    )
                previousCoverComposition = composition
            } else {
                previousCoverComposition = null
                panelBinding.ivMusicIcon.setImageResource(R.drawable.ic_music_placeholder)
            }
        }
    }

    override fun showRepeatMode(mode: Int) {
        @DrawableRes val iconRes = FormatUtils.getRepeatModeIcon(mode)
        panelBinding.btnInfinitePlay.setImageResource(iconRes)
        val description = getString(FormatUtils.getRepeatModeText(mode))
        panelBinding.btnInfinitePlay.contentDescription = description

        panelBinding.btnInfinitePlay.setOnClickListener { view -> onRepeatModeButtonClicked(view, mode) }
    }

    override fun showRandomPlayingButton(active: Boolean) {
        panelBinding.btnRandomPlay.isSelected = active
        if (active) {
            panelBinding.btnRandomPlay.setOnClickListener {
                presenter.onRandomPlayingButtonClicked(false)
            }
        } else {
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
        startActivity(newCompositionEditorIntent(requireContext(), id))
    }

    override fun showErrorMessage(errorCommand: ErrorCommand) {
        MessagesUtils.makeSnackbar(viewBinding.clPlayQueueContainer!!, errorCommand.message).show()
    }

    override fun showDeletedItemMessage() {
        MessagesUtils.makeSnackbar(
            viewBinding.clPlayQueueContainer!!,
            R.string.queue_item_removed,
            Snackbar.LENGTH_LONG
        ).setAction(R.string.cancel, presenter::onRestoreDeletedItemClicked)
            .show()
    }

    override fun showAddingToPlayListError(errorCommand: ErrorCommand) {
        MessagesUtils.makeSnackbar(
            viewBinding.clPlayQueueContainer!!,
            getString(R.string.add_to_playlist_error_template, errorCommand.message),
            Snackbar.LENGTH_SHORT
        ).show()
    }

    override fun showAddingToPlayListComplete(playList: PlayList?, compositions: List<Composition>) {
        val text = MessagesUtils.getAddToPlayListCompleteMessage(requireActivity(), playList, compositions)
        MessagesUtils.makeSnackbar(viewBinding.clPlayQueueContainer!!, text, Snackbar.LENGTH_SHORT).show()
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
                viewBinding.clPlayQueueContainer!!,
                getString(R.string.delete_composition_error_template, errorCommand.message),
                Snackbar.LENGTH_SHORT
            ).show()
        }
    }

    override fun showDeleteCompositionMessage(compositionsToDelete: List<Composition>) {
        val text = MessagesUtils.getDeleteCompleteMessage(requireActivity(), compositionsToDelete)
        MessagesUtils.makeSnackbar(viewBinding.clPlayQueueContainer!!, text, Snackbar.LENGTH_SHORT).show()
    }

    override fun displayPlaybackSpeed(speed: Float) {
        panelBinding.tvPlaybackSpeed.text = getString(R.string.playback_speed_template, speed)
        panelBinding.tvPlaybackSpeed.setOnClickListener {
            DialogUtils.showSpeedSelectorDialog(
                requireContext(),
                speed,
                presenter::onPlaybackSpeedSelected
            )
        }
    }

    override fun showSpeedChangeFeatureVisible(visible: Boolean) {
        panelBinding.tvPlaybackSpeed.visibility = if (visible) View.VISIBLE else View.GONE
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
            val icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_timer)!!
            val resources = requireContext().resources
            val iconSize = resources.getDimensionPixelSize(R.dimen.sleep_timer_icon_size)
            icon.setBounds(0, 0, iconSize, iconSize)
            icon.setTint(
                AndroidUtils.getColorFromAttr(
                    requireContext(),
                    android.R.attr.textColorSecondary
                )
            )
            panelBinding.tvSleepTime.setCompoundDrawables(icon, null, null, null)
            val iconPadding = resources.getDimensionPixelSize(R.dimen.sleep_timer_icon_padding)
            panelBinding.tvSleepTime.compoundDrawablePadding = iconPadding
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

    override fun showCurrentCompositionSyncState(fileSyncState: FormattedFileSyncState) {
        showFileSyncState(
            fileSyncState.fileSyncState,
            fileSyncState.isFileRemote,
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
                viewBinding.navigationView.setCheckedItem(R.id.menu_library)
                presenter.onLibraryScreenSelected(Screens.LIBRARY_FOLDERS)
                startFragment(newLibraryFoldersRootFragment(id))
            }
        }
    }

    fun openPlayerPanel() {
        presenter.onOpenPlayerPanelClicked()
        playerPanelWrapper.openPlayerPanel()
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
        if (viewBinding.drawer.getDrawerLockMode(GravityCompat.START) != DrawerLayout.LOCK_MODE_LOCKED_CLOSED) {
            viewBinding.drawer.openDrawer(GravityCompat.START)
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
        viewBinding.drawer.closeDrawer(GravityCompat.START)
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
            RepeatMode.REPEAT_PLAY_LIST -> R.id.menu_repeat_playlist
            RepeatMode.REPEAT_COMPOSITION -> R.id.menu_repeat_composition
            else -> R.id.menu_do_not_repeat
        }
        val menu = AndroidUtils.createMenu(requireContext(), R.menu.repeat_mode_menu)
        menu.findItem(selectedItemId).isChecked = true

        PopupMenuWindow.showPopup(view, menu) { item ->
            val repeatMode = when (item.itemId) {
                R.id.menu_repeat_playlist -> RepeatMode.REPEAT_PLAY_LIST
                R.id.menu_repeat_composition -> RepeatMode.REPEAT_COMPOSITION
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
            viewBinding.vpPlayContent!!.currentItem = position
        }
    }

    private fun showEditorRequestDeniedMessage() {
        MessagesUtils.makeSnackbar(
            viewBinding.clPlayQueueContainer!!,
            R.string.android_r_edit_file_permission_denied,
            Snackbar.LENGTH_LONG
        ).show()
    }

}