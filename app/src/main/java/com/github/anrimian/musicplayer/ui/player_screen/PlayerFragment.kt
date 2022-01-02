package com.github.anrimian.musicplayer.ui.player_screen

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.graphics.drawable.DrawerArrowDrawable
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.anrimian.musicplayer.Constants
import com.github.anrimian.musicplayer.Constants.Tags
import com.github.anrimian.musicplayer.R
import com.github.anrimian.musicplayer.databinding.FragmentDrawerBinding
import com.github.anrimian.musicplayer.databinding.PartialDetailedMusicBinding
import com.github.anrimian.musicplayer.databinding.PartialDrawerHeaderBinding
import com.github.anrimian.musicplayer.databinding.PartialQueueToolbarBinding
import com.github.anrimian.musicplayer.di.Components
import com.github.anrimian.musicplayer.domain.interactors.sleep_timer.NO_TIMER
import com.github.anrimian.musicplayer.domain.models.Screens
import com.github.anrimian.musicplayer.domain.models.composition.Composition
import com.github.anrimian.musicplayer.domain.models.play_queue.PlayQueueItem
import com.github.anrimian.musicplayer.domain.models.player.PlayerState
import com.github.anrimian.musicplayer.domain.models.player.modes.RepeatMode
import com.github.anrimian.musicplayer.domain.models.playlist.PlayList
import com.github.anrimian.musicplayer.domain.models.scanner.FileScannerState
import com.github.anrimian.musicplayer.domain.models.scanner.Running
import com.github.anrimian.musicplayer.domain.models.utils.CompositionHelper
import com.github.anrimian.musicplayer.ui.about.AboutAppFragment
import com.github.anrimian.musicplayer.ui.common.compat.CompatUtils
import com.github.anrimian.musicplayer.ui.common.dialogs.DialogUtils
import com.github.anrimian.musicplayer.ui.common.error.ErrorCommand
import com.github.anrimian.musicplayer.ui.common.format.FormatUtils
import com.github.anrimian.musicplayer.ui.common.format.MessagesUtils
import com.github.anrimian.musicplayer.ui.common.menu.PopupMenuWindow
import com.github.anrimian.musicplayer.ui.common.navigation.ScreensMap
import com.github.anrimian.musicplayer.ui.common.toolbar.AdvancedToolbar
import com.github.anrimian.musicplayer.ui.common.view.ViewUtils
import com.github.anrimian.musicplayer.ui.editor.common.DeleteErrorHandler
import com.github.anrimian.musicplayer.ui.editor.common.ErrorHandler
import com.github.anrimian.musicplayer.ui.editor.composition.newCompositionEditorIntent
import com.github.anrimian.musicplayer.ui.equalizer.EqualizerDialogFragment
import com.github.anrimian.musicplayer.ui.library.albums.list.AlbumsListFragment
import com.github.anrimian.musicplayer.ui.library.artists.list.ArtistsListFragment
import com.github.anrimian.musicplayer.ui.library.compositions.LibraryCompositionsFragment
import com.github.anrimian.musicplayer.ui.library.folders.root.LibraryFoldersRootFragment
import com.github.anrimian.musicplayer.ui.library.genres.list.GenresListFragment
import com.github.anrimian.musicplayer.ui.player_screen.view.adapter.PlayQueueAdapter
import com.github.anrimian.musicplayer.ui.player_screen.view.drawer.DrawerLockStateProcessor
import com.github.anrimian.musicplayer.ui.player_screen.view.wrappers.PlayerPanelWrapper
import com.github.anrimian.musicplayer.ui.player_screen.view.wrappers.PlayerPanelWrapperImpl
import com.github.anrimian.musicplayer.ui.player_screen.view.wrappers.TabletPlayerPanelWrapper
import com.github.anrimian.musicplayer.ui.playlist_screens.choose.ChoosePlayListDialogFragment
import com.github.anrimian.musicplayer.ui.playlist_screens.create.CreatePlayListDialogFragment
import com.github.anrimian.musicplayer.ui.playlist_screens.playlist.newPlayListFragment
import com.github.anrimian.musicplayer.ui.playlist_screens.playlists.PlayListsFragment
import com.github.anrimian.musicplayer.ui.settings.SettingsFragment
import com.github.anrimian.musicplayer.ui.sleep_timer.SleepTimerDialogFragment
import com.github.anrimian.musicplayer.ui.start.StartFragment
import com.github.anrimian.musicplayer.ui.utils.AndroidUtils
import com.github.anrimian.musicplayer.ui.utils.ViewUtils.animateVisibility
import com.github.anrimian.musicplayer.ui.utils.fragments.BackButtonListener
import com.github.anrimian.musicplayer.ui.utils.fragments.navigation.FragmentNavigation
import com.github.anrimian.musicplayer.ui.utils.views.drawer.SimpleDrawerListener
import com.github.anrimian.musicplayer.ui.utils.views.menu.ActionMenuUtil
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.RecyclerViewUtils
import com.github.anrimian.musicplayer.ui.utils.views.seek_bar.SeekBarViewWrapper
import com.github.anrimian.musicplayer.utils.Permissions
import com.google.android.material.snackbar.Snackbar
import io.reactivex.rxjava3.disposables.CompositeDisposable
import moxy.MvpAppCompatFragment
import moxy.ktx.moxyPresenter
import java.util.*
import kotlin.math.abs

/**
 * Created on 19.10.2017.
 */

private const val NO_ITEM = -1
private const val SELECTED_DRAWER_ITEM = "selected_drawer_item"

fun newPlayerFragment(openPlayQueue: Boolean = false): PlayerFragment {
    val args = Bundle()
    args.putBoolean(Constants.Arguments.OPEN_PLAY_QUEUE_ARG, openPlayQueue)
    val fragment = PlayerFragment()
    fragment.arguments = args
    return fragment
}

class PlayerFragment : MvpAppCompatFragment(), BackButtonListener, PlayerView {

    private val presenter by moxyPresenter {
        Components.getLibraryComponent().playerPresenter()
    }
    private lateinit var viewBinding: FragmentDrawerBinding
    private lateinit var panelBinding: PartialDetailedMusicBinding
    private lateinit var drawerHeaderBinding: PartialDrawerHeaderBinding
    private lateinit var toolbarPlayQueueBinding: PartialQueueToolbarBinding

    private lateinit var toolbar: AdvancedToolbar
    private lateinit var playQueueAdapter: PlayQueueAdapter

    private var selectedDrawerItemId = NO_ITEM
    private var itemIdToStart = NO_ITEM

    private val secondScrollHandler = Handler(Looper.getMainLooper())
    private var currentPosition = -2 //for immediate first scroll

    private lateinit var playQueueLayoutManager: LinearLayoutManager
    private lateinit var seekBarViewWrapper: SeekBarViewWrapper
    private lateinit var drawerLockStateProcessor: DrawerLockStateProcessor

    private val viewDisposable = CompositeDisposable()

    private lateinit var navigation: FragmentNavigation
    private lateinit var playerPanelWrapper: PlayerPanelWrapper

    private lateinit var deletingErrorHandler: ErrorHandler

    private var previousCoverComposition: Composition? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
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
            TabletPlayerPanelWrapper(view, drawerLockStateProcessor::onBottomSheetOpened)
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
        
        ActionMenuUtil.setupMenu(
            toolbarPlayQueueBinding.acvPlayQueue,
            R.menu.play_queue_menu,
            this::onPlayQueueMenuItemClicked
        )
        
        toolbar.setupWithNavigation(navigation, drawerArrowDrawable) { 
            playerPanelWrapper.isBottomPanelExpanded 
        }
        
        panelBinding.ivSkipToPrevious.setOnClickListener { presenter.onSkipToPreviousButtonClicked() }
        ViewUtils.setOnHoldListener(panelBinding.ivSkipToPrevious, presenter::onFastSeekBackwardCalled)
        panelBinding.ivSkipToNext.setOnClickListener { presenter.onSkipToNextButtonClicked() }
        ViewUtils.setOnHoldListener(panelBinding.ivSkipToNext, presenter::onFastSeekForwardCalled)

        playQueueLayoutManager = LinearLayoutManager(requireContext())
        viewBinding.rvPlaylist!!.layoutManager = playQueueLayoutManager
        playQueueAdapter = PlayQueueAdapter(viewBinding.rvPlaylist)
        playQueueAdapter.setOnCompositionClickListener(presenter::onQueueItemClicked)
        playQueueAdapter.setMenuClickListener(this::onPlayItemMenuClicked)
        playQueueAdapter.setIconClickListener(presenter::onQueueItemIconClicked)
        
        viewBinding.rvPlaylist!!.adapter = playQueueAdapter
        val callback = FormatUtils.withSwipeToDelete(
            viewBinding.rvPlaylist!!,
            AndroidUtils.getColorFromAttr(requireContext(), R.attr.listItemBottomBackground),
            presenter::onItemSwipedToDelete,
            ItemTouchHelper.START,
            R.drawable.ic_remove_from_queue,
            R.string.delete_from_queue
        )
        callback.setOnMovedListener(presenter::onItemMoved)
        callback.setOnStartDragListener { presenter.onDragStarted() }
        callback.setOnEndDragListener { presenter.onDragEnded() }
        val itemTouchHelper = ItemTouchHelper(callback)
        itemTouchHelper.attachToRecyclerView(viewBinding.rvPlaylist!!)
        
        if (savedInstanceState != null) {
            selectedDrawerItemId = savedInstanceState.getInt(SELECTED_DRAWER_ITEM, NO_ITEM)
        } else {
            presenter.onCurrentScreenRequested()
        }
        
        panelBinding.btnActionsMenu.setOnClickListener(this::onCompositionMenuClicked)
        panelBinding.topPanel.setOnClickListener { openPlayQueue() }
        
        seekBarViewWrapper = SeekBarViewWrapper(panelBinding.sbTrackState)
        seekBarViewWrapper.setProgressChangeListener(presenter::onTrackRewoundTo)
        seekBarViewWrapper.setOnSeekStartListener(presenter::onSeekStart)
        seekBarViewWrapper.setOnSeekStopListener(presenter::onSeekStop)
        
        CompatUtils.setOutlineTextButtonStyle(panelBinding.tvPlaybackSpeed)
        CompatUtils.setOutlineTextButtonStyle(panelBinding.tvSleepTime)
        
        deletingErrorHandler = DeleteErrorHandler(
            childFragmentManager,
            presenter::onRetryFailedDeleteActionClicked, 
            this::showEditorRequestDeniedMessage
        )
        
        val fragment = childFragmentManager
            .findFragmentByTag(Tags.SELECT_PLAYLIST_TAG) as ChoosePlayListDialogFragment?
        fragment?.setOnCompleteListener(presenter::onPlayListForAddingSelected)
        
        val createPlayListFragment = childFragmentManager
            .findFragmentByTag(Tags.CREATE_PLAYLIST_TAG) as CreatePlayListDialogFragment?
        createPlayListFragment?.setOnCompleteListener(presenter::onPlayListForAddingCreated)
        
        if (requireArguments().getBoolean(Constants.Arguments.OPEN_PLAY_QUEUE_ARG)) {
            requireArguments().remove(Constants.Arguments.OPEN_PLAY_QUEUE_ARG)
            openPlayQueue()
        }
        
        if (!Permissions.hasFilePermission(requireContext())) {
            parentFragmentManager
                .beginTransaction()
                .replace(R.id.main_activity_container, StartFragment())
                .commit()
        }

        if (savedInstanceState == null) {
            Components.getAppComponent()
                .specificNavigation()
                .attachShortSyncStateFragment(childFragmentManager, R.id.flShortSyncState)
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
            Screens.LIBRARY_FOLDERS -> LibraryFoldersRootFragment()
            Screens.LIBRARY_ARTISTS -> ArtistsListFragment()
            Screens.LIBRARY_ALBUMS -> AlbumsListFragment()
            Screens.LIBRARY_GENRES -> GenresListFragment()
            else -> LibraryCompositionsFragment()
        }
        startFragment(fragment)
    }

    override fun showPlayerState(state: PlayerState) {
        if (state === PlayerState.PLAY) {
            AndroidUtils.setAnimatedVectorDrawable(panelBinding.ivPlayPause, R.drawable.anim_play_to_pause)
            panelBinding.ivPlayPause.contentDescription = getString(R.string.pause)
            panelBinding.ivPlayPause.setOnClickListener { presenter.onStopButtonClicked() }
            playQueueAdapter.showPlaying(true)
        } else {
            AndroidUtils.setAnimatedVectorDrawable(panelBinding.ivPlayPause, R.drawable.anim_pause_to_play)
            panelBinding.ivPlayPause.contentDescription = getString(R.string.play)
            panelBinding.ivPlayPause.setOnClickListener { presenter.onPlayButtonClicked() }
            playQueueAdapter.showPlaying(false)
        }
    }

    override fun setMusicControlsEnabled(show: Boolean) {
        panelBinding.ivSkipToNext.isEnabled = show
        panelBinding.ivSkipToPrevious.isEnabled = show
        panelBinding.ivPlayPause.isEnabled = show
        panelBinding.btnInfinitePlay.isEnabled = show
        panelBinding.btnRandomPlay.isEnabled = show
        panelBinding.sbTrackState.isEnabled = show
        toolbarPlayQueueBinding.acvPlayQueue.menu.findItem(R.id.menu_save_as_playlist).isEnabled = show
        panelBinding.tvPlaybackSpeed.isEnabled = show
    }

    override fun showCurrentQueueItem(item: PlayQueueItem?, showCover: Boolean) {
        animateVisibility(viewBinding.bottomSheetTopShadow, View.VISIBLE)
        animateVisibility(viewBinding.rvPlaylist!!, View.VISIBLE)
        panelBinding.btnActionsMenu.isEnabled = item != null
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
            viewBinding.rvPlaylist!!.contentDescription = noCompositionMessage
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
            playQueueAdapter.onCurrentItemChanged(item)
        }
    }

    //check by:
    //switch order mode(working)
    //new queue(so-so, but pass)
    //remove queue item(working)
    override fun scrollQueueToPosition(position: Int) {
        secondScrollHandler.removeCallbacksAndMessages(null)
        val positionDiff = abs(position - currentPosition)
        currentPosition = position
        if (RecyclerViewUtils.isPositionVisible(playQueueLayoutManager, position)) {
            return
        }
        
        val smooth = positionDiff == 1 
                || position == playQueueLayoutManager.findFirstVisibleItemPosition() 
                || position == playQueueLayoutManager.findLastVisibleItemPosition()
        RecyclerViewUtils.scrollToPosition(
            viewBinding.rvPlaylist!!,
            playQueueLayoutManager,
            position,
            smooth
        )

        //sometimes can not scroll, check twice
        secondScrollHandler.postDelayed({
            if (!RecyclerViewUtils.isPositionVisible(playQueueLayoutManager, position)) {
                RecyclerViewUtils.scrollToPosition(
                    viewBinding.rvPlaylist!!,
                    playQueueLayoutManager,
                    position,
                    false
                )
            }
        }, 300)
    }

    override fun updatePlayQueue(items: List<PlayQueueItem>) {
        playQueueAdapter.submitList(items)
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

    override fun showShareMusicDialog(composition: Composition) {
        DialogUtils.shareComposition(requireContext(), composition)
    }

    override fun showPlayQueueSubtitle(size: Int) {
        toolbarPlayQueueBinding.tvQueueSubtitle.text = FormatUtils.formatCompositionsCount(requireContext(), size)
    }

    override fun notifyItemMoved(from: Int, to: Int) {
        playQueueAdapter.notifyItemMoved(from, to)
    }

    override fun setPlayQueueCoversEnabled(isCoversEnabled: Boolean) {
        playQueueAdapter.setCoversEnabled(isCoversEnabled)
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
        val dialog = ChoosePlayListDialogFragment()
        dialog.setOnCompleteListener(presenter::onPlayListForAddingSelected)
        dialog.show(childFragmentManager, Tags.SELECT_PLAYLIST_TAG)
    }

    override fun showConfirmDeleteDialog(compositionsToDelete: List<Composition>) {
        DialogUtils.showConfirmDeleteDialog(requireContext(), compositionsToDelete) { 
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
        //setVisibility() don't work in motion layout
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
                SleepTimerDialogFragment().show(childFragmentManager, null)
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
            drawerHeaderBinding.tvFileScannerState.visibility = View.GONE
        }
    }

    fun openPlayQueue() {
        presenter.onOpenPlayQueueClicked()
        playerPanelWrapper.openPlayQueue()
    }

    private fun onPlayQueueMenuItemClicked(menuItem: MenuItem) {
        when (menuItem.itemId) {
            R.id.menu_save_as_playlist -> {
                val fragment = CreatePlayListDialogFragment()
                fragment.setOnCompleteListener(presenter::onPlayListForAddingCreated)
                fragment.show(childFragmentManager, Tags.CREATE_PLAYLIST_TAG)
            }
            R.id.menu_sleep_timer -> SleepTimerDialogFragment().show(childFragmentManager, null)
            R.id.menu_equalizer -> EqualizerDialogFragment().show(childFragmentManager, null)
            R.id.menu_clear_play_queue -> presenter.onClearPlayQueueClicked()
        }
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
                R.id.menu_share -> presenter.onShareCompositionButtonClicked()
                R.id.menu_delete -> presenter.onDeleteCurrentCompositionButtonClicked()
                R.id.menu_edit -> presenter.onEditCompositionButtonClicked()
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

    private fun onPlayItemMenuClicked(view: View, playQueueItem: PlayQueueItem) {
        val composition = playQueueItem.composition
        PopupMenuWindow.showPopup(view, R.menu.play_queue_item_menu) { item ->
            when (item.itemId) {
                R.id.menu_add_to_playlist -> presenter.onAddQueueItemToPlayListButtonClicked(composition)
                R.id.menu_edit -> startActivity(newCompositionEditorIntent(requireContext(), composition.id))
                R.id.menu_share -> onShareCompositionClicked(composition)
                R.id.menu_delete_from_queue -> presenter.onDeleteQueueItemClicked(playQueueItem)
                R.id.menu_delete -> presenter.onDeleteCompositionButtonClicked(composition)
            }
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

    private fun onShareCompositionClicked(composition: Composition) {
        DialogUtils.shareComposition(requireContext(), composition)
    }

    private fun showEditorRequestDeniedMessage() {
        MessagesUtils.makeSnackbar(
            viewBinding.clPlayQueueContainer!!,
            R.string.android_r_edit_file_permission_denied,
            Snackbar.LENGTH_LONG
        ).show()
    }

}