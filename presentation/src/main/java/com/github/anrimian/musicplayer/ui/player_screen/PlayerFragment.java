package com.github.anrimian.musicplayer.ui.player_screen;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.motion.MotionLayout;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.FileProvider;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.drawable.DrawerArrowDrawable;
import android.support.v7.widget.ActionMenuView;
import android.support.v7.widget.AppCompatSeekBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.arellomobile.mvp.MvpAppCompatFragment;
import com.arellomobile.mvp.presenter.InjectPresenter;
import com.arellomobile.mvp.presenter.ProvidePresenter;
import com.github.anrimian.musicplayer.R;
import com.github.anrimian.musicplayer.data.preferences.UiStatePreferences;
import com.github.anrimian.musicplayer.di.Components;
import com.github.anrimian.musicplayer.domain.models.Screens;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.composition.PlayQueueItem;
import com.github.anrimian.musicplayer.domain.models.playlist.PlayList;
import com.github.anrimian.musicplayer.infrastructure.service.MusicServiceManager;
import com.github.anrimian.musicplayer.ui.common.DialogUtils;
import com.github.anrimian.musicplayer.ui.common.error.ErrorCommand;
import com.github.anrimian.musicplayer.ui.common.format.ImageFormatUtils;
import com.github.anrimian.musicplayer.ui.common.toolbar.AdvancedToolbar;
import com.github.anrimian.musicplayer.ui.library.compositions.LibraryCompositionsFragment;
import com.github.anrimian.musicplayer.ui.library.folders.LibraryFoldersRootFragment;
import com.github.anrimian.musicplayer.ui.player_screen.view.adapter.PlayQueueAdapter;
import com.github.anrimian.musicplayer.ui.player_screen.view.drawer.DrawerLockStateProcessor;
import com.github.anrimian.musicplayer.ui.playlist_screens.choose.ChoosePlayListDialogFragment;
import com.github.anrimian.musicplayer.ui.playlist_screens.create.CreatePlayListDialogFragment;
import com.github.anrimian.musicplayer.ui.playlist_screens.playlists.PlayListsFragment;
import com.github.anrimian.musicplayer.ui.start.StartFragment;
import com.github.anrimian.musicplayer.ui.utils.fragments.BackButtonListener;
import com.github.anrimian.musicplayer.ui.utils.fragments.FragmentUtils;
import com.github.anrimian.musicplayer.ui.utils.views.bottom_sheet.SimpleBottomSheetCallback;
import com.github.anrimian.musicplayer.ui.utils.views.delegate.BoundValuesDelegate;
import com.github.anrimian.musicplayer.ui.utils.views.delegate.ChangeWidthDelegate;
import com.github.anrimian.musicplayer.ui.utils.views.delegate.DelegateManager;
import com.github.anrimian.musicplayer.ui.utils.views.delegate.DrawerArrowDelegate;
import com.github.anrimian.musicplayer.ui.utils.views.delegate.ExpandViewDelegate;
import com.github.anrimian.musicplayer.ui.utils.views.delegate.LeftBottomShadowDelegate;
import com.github.anrimian.musicplayer.ui.utils.views.delegate.MotionLayoutDelegate;
import com.github.anrimian.musicplayer.ui.utils.views.delegate.ReverseDelegate;
import com.github.anrimian.musicplayer.ui.utils.views.delegate.SlideDelegate;
import com.github.anrimian.musicplayer.ui.utils.views.delegate.TextSizeDelegate;
import com.github.anrimian.musicplayer.ui.utils.views.delegate.ToolbarMenuVisibilityDelegate;
import com.github.anrimian.musicplayer.ui.utils.views.delegate.VisibilityDelegate;
import com.github.anrimian.musicplayer.ui.utils.views.drawer.SimpleDrawerListener;
import com.github.anrimian.musicplayer.ui.utils.views.seek_bar.SeekBarViewWrapper;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.io.File;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.support.design.widget.BottomSheetBehavior.STATE_COLLAPSED;
import static android.support.design.widget.BottomSheetBehavior.STATE_EXPANDED;
import static android.support.v4.widget.DrawerLayout.LOCK_MODE_LOCKED_CLOSED;
import static com.github.anrimian.musicplayer.Constants.Tags.CREATE_PLAYLIST_TAG;
import static com.github.anrimian.musicplayer.Constants.Tags.SELECT_PLAYLIST_TAG;
import static com.github.anrimian.musicplayer.ui.common.format.FormatUtils.formatCompositionAuthor;
import static com.github.anrimian.musicplayer.ui.common.format.FormatUtils.formatCompositionName;
import static com.github.anrimian.musicplayer.ui.common.format.FormatUtils.formatMilliseconds;
import static com.github.anrimian.musicplayer.ui.common.format.MessagesUtils.getAddToPlayListCompleteMessage;
import static com.github.anrimian.musicplayer.ui.common.format.MessagesUtils.getDeleteCompleteMessage;
import static com.github.anrimian.musicplayer.ui.utils.views.menu.ActionMenuUtil.setupMenu;
import static com.github.anrimian.musicplayer.utils.AndroidUtils.getColorFromAttr;

/**
 * Created on 19.10.2017.
 */

public class PlayerFragment extends MvpAppCompatFragment implements BackButtonListener, PlayerView {

    private static final int NO_ITEM = -1;
    private static final String SELECTED_DRAWER_ITEM = "selected_drawer_item";
    private static final String BOTTOM_SHEET_STATE = "bottom_sheet_state";

    @InjectPresenter
    PlayerPresenter presenter;

    @BindView(R.id.drawer)
    DrawerLayout drawer;

    @BindView(R.id.navigation_view)
    NavigationView navigationView;

    @Nullable
    @BindView(R.id.coordinator_bottom_sheet)
    View bottomSheetCoordinator;

    @Nullable
    @BindView(R.id.bottom_sheet_left_shadow)
    View bottomSheetLeftShadow;

    @Nullable
    @BindView(R.id.bottom_sheet_top_left_shadow)
    View bottomSheetTopLeftShadow;

    @BindView(R.id.rv_playlist)
    RecyclerView rvPlayList;

    @BindView(R.id.iv_play_pause)
    ImageView ivPlayPause;

    @BindView(R.id.iv_skip_to_previous)
    ImageView ivSkipToPrevious;

    @BindView(R.id.iv_skip_to_next)
    ImageView ivSkipToNext;

//    @BindView(R.id.iv_play_pause_expanded)
//    ImageView ivPlayPauseExpanded;
//
//    @BindView(R.id.iv_skip_to_previous_expanded)
//    ImageView ivSkipToPreviousExpanded;
//
//    @BindView(R.id.iv_skip_to_next_expanded)
//    ImageView ivSkipToNextExpanded;

    @BindView(R.id.drawer_fragment_container)
    ViewGroup fragmentContainer;

    @BindView(R.id.tv_current_composition)
    TextView tvCurrentComposition;

    @BindView(R.id.btn_infinite_play)
    ImageView btnInfinitePlay;

    @BindView(R.id.btn_random_play)
    ImageView btnRandomPlay;

    @BindView(R.id.tv_played_time)
    TextView tvPlayedTime;

    @BindView(R.id.tv_total_time)
    TextView tvTotalTime;

    @BindView(R.id.sb_track_state)
    AppCompatSeekBar sbTrackState;

    @BindView(R.id.bottom_sheet_top_shadow)
    View bottomSheetTopShadow;

    @BindView(R.id.top_panel)
    View topBottomSheetPanel;

//    @BindView(R.id.bottom_sheet_bottom_shadow)
//    View bottomSheetBottomShadow;

    @BindView(R.id.iv_music_icon)
    ImageView ivMusicIcon;

    @BindView(R.id.btn_actions_menu)
    ImageView btnActionsMenu;

//    @BindView(R.id.tv_current_composition_info)
//    TextView tvCurrentCompositionInfo;

    @BindView(R.id.tv_current_composition_author)
    TextView tvCurrentCompositionAuthor;

//    @BindView(R.id.play_queue_toolbar)
//    AdvancedToolbar advancedToolbar;

    @BindView(R.id.cl_play_queue_container)
    CoordinatorLayout clPlayQueueContainer;

    @BindView(R.id.ml_bottom_sheet)
    MotionLayout mlBottomSheet;

    @BindView(R.id.toolbar)
    AdvancedToolbar toolbar;

//    @BindView(R.id.play_actions_top_shadow)
//    View playActionsTopShadow;

//    @BindView(R.id.bottom_panel)
//    View bottomPanel;

//    @BindView(R.id.bottom_sheet_play_panel_shadow)
//    View bottomSheetPanelShadow;

    @BindView(R.id.title_container)
    View titleContainer;

    @BindView(R.id.acv_play_queue)
    ActionMenuView actionMenuView;

    @BindView(R.id.play_queue_title_container)
    View playQueueTitleContainer;

    @BindView(R.id.tv_queue_subtitle)
    TextView tvQueueSubtitle;

    private BottomSheetBehavior<View> bottomSheetBehavior;

    private PlayQueueAdapter playQueueAdapter;

    private SlideDelegate bottomSheetDelegate;

    private ActionBarDrawerToggle drawerToggle;

    private int selectedDrawerItemId = NO_ITEM;
    private int itemIdToStart = NO_ITEM;

    private LinearLayoutManager playQueueLayoutManager;
    private SeekBarViewWrapper seekBarViewWrapper;

    private DrawerLockStateProcessor drawerLockStateProcessor;

    private UiStatePreferences uiStatePreferences;

    @ProvidePresenter
    PlayerPresenter providePresenter() {
        return Components.getLibraryComponent().playerPresenter();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_drawer, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        RxPermissions rxPermissions = new RxPermissions(requireActivity());
        if (!rxPermissions.isGranted(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            requireFragmentManager()
                    .beginTransaction()
                    .replace(R.id.main_activity_container, new StartFragment())
                    .commit();
            return;
        }
        MusicServiceManager.initialize();
        uiStatePreferences = Components.getAppComponent().uiStatePreferences();

        toolbar.initializeViews();
        toolbar.setupWithActivity((AppCompatActivity) requireActivity());

        drawerLockStateProcessor = new DrawerLockStateProcessor(drawer);
        drawerLockStateProcessor.setupWithFragmentManager(getChildFragmentManager());
        toolbar.setSearchModeListener(drawerLockStateProcessor::onSearchModeChanged);

        navigationView.setNavigationItemSelectedListener(this::onNavigationItemSelected);
        navigationView.inflateHeaderView(R.layout.partial_drawer_header);

        drawerToggle = new ActionBarDrawerToggle(requireActivity(), drawer, R.string.open_drawer, R.string.close_drawer);
        DrawerArrowDrawable drawerArrowDrawable = createDrawerArrowDrawable();
        drawerToggle.setDrawerArrowDrawable(drawerArrowDrawable);

        drawer.addDrawerListener(new SimpleDrawerListener(this::onDrawerClosed));

        setupMenu(actionMenuView, R.menu.play_queue_menu, this::onPlayQueueMenuItemClicked);

        int bottomSheetState;
        if (savedInstanceState == null) {
            bottomSheetState = uiStatePreferences.isPlayerPanelOpen()? STATE_EXPANDED: STATE_COLLAPSED;
            drawerLockStateProcessor.onBottomSheetOpened(uiStatePreferences.isPlayerPanelOpen());
        } else {
            bottomSheetState = savedInstanceState.getInt(BOTTOM_SHEET_STATE);
        }

        bottomSheetDelegate = getBottomSheetDelegate(drawerArrowDrawable);
        bottomSheetDelegate.onSlide(bottomSheetState == STATE_COLLAPSED ? 0f : 1f);

        bottomSheetBehavior = BottomSheetBehavior.from(mlBottomSheet);
        mlBottomSheet.setClickable(true);
        bottomSheetBehavior.setBottomSheetCallback(new SimpleBottomSheetCallback(
                this::onBottomSheetStateChanged,
                this::onBottomSheetSlided
        ));
        bottomSheetBehavior.setState(bottomSheetState);

        toolbar.setupWithFragmentManager(getChildFragmentManager(),
                drawerArrowDrawable,
                () -> bottomSheetBehavior.getState() == STATE_EXPANDED);

        ivSkipToPrevious.setOnClickListener(v -> presenter.onSkipToPreviousButtonClicked());
        ivSkipToNext.setOnClickListener(v -> presenter.onSkipToNextButtonClicked());

        playQueueLayoutManager = new LinearLayoutManager(requireContext());
        rvPlayList.setLayoutManager(playQueueLayoutManager);

        Fragment currentFragment = getChildFragmentManager().findFragmentById(R.id.drawer_fragment_container);
        if (currentFragment == null || savedInstanceState == null) {
            showDrawerScreen(uiStatePreferences.getSelectedDrawerScreen());
        } else {
            selectedDrawerItemId = savedInstanceState.getInt(SELECTED_DRAWER_ITEM, NO_ITEM);
        }

        btnActionsMenu.setOnClickListener(this::onCompositionMenuClicked);
        topBottomSheetPanel.setOnClickListener(v -> onTopPanelClicked());

        seekBarViewWrapper = new SeekBarViewWrapper(sbTrackState);
        seekBarViewWrapper.setProgressChangeListener(presenter::onTrackRewoundTo);
        seekBarViewWrapper.setOnSeekStartListener(presenter::onSeekStart);
        seekBarViewWrapper.setOnSeekStopListener(presenter::onSeekStop);

        ChoosePlayListDialogFragment fragment = (ChoosePlayListDialogFragment) getChildFragmentManager()
                .findFragmentByTag(SELECT_PLAYLIST_TAG);
        if (fragment != null) {
            fragment.setOnCompleteListener(presenter::onPlayListForAddingSelected);
        }

        CreatePlayListDialogFragment createPlayListFragment = (CreatePlayListDialogFragment) getChildFragmentManager()
                .findFragmentByTag(CREATE_PLAYLIST_TAG);
        if (createPlayListFragment != null) {
            createPlayListFragment.setOnCompleteListener(presenter::onPlayListForAddingCreated);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (drawer.getDrawerLockMode(GravityCompat.START) != LOCK_MODE_LOCKED_CLOSED
                    && !toolbar.isInSearchMode()) {
                drawer.openDrawer(GravityCompat.START);
            } else {
                onBackPressed();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(SELECTED_DRAWER_ITEM, selectedDrawerItemId);
        outState.putInt(BOTTOM_SHEET_STATE, bottomSheetBehavior.getState());
    }

    @Override
    public boolean onBackPressed() {
        if (bottomSheetBehavior.getState() == STATE_EXPANDED) {
            bottomSheetBehavior.setState(STATE_COLLAPSED);
            return true;
        }
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
            return true;
        }
        if (toolbar.isInSearchMode()) {
            toolbar.setSearchModeEnabled(false);
            return true;
        }
        FragmentManager fm = getChildFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.drawer_fragment_container);
        boolean processed = fragment instanceof BackButtonListener
                && ((BackButtonListener) fragment).onBackPressed();
        if (!processed) {
            if (fm.getBackStackEntryCount() > 0) {
                fm.popBackStack();
                processed = true;
            }
        }
        return processed;
    }

    @Override
    public void onStart() {
        super.onStart();
        presenter.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        presenter.onStop();
    }

    @Override
    public void showStopState() {
        ivPlayPause.setImageResource(R.drawable.ic_play);
        ivPlayPause.setOnClickListener(v -> presenter.onPlayButtonClicked());
    }

    @Override
    public void showPlayState() {
        ivPlayPause.setImageResource(R.drawable.ic_pause);
        ivPlayPause.setOnClickListener(v -> presenter.onStopButtonClicked());
    }

    @Override
    public void showMusicControls(boolean show) {
        setContentBottomHeight(show ?
                getResources().getDimensionPixelSize(R.dimen.bottom_sheet_height) : 0);
        bottomSheetTopShadow.setVisibility(show? View.VISIBLE: View.GONE);

        if (!show && bottomSheetBehavior.getState() == STATE_EXPANDED) {
            bottomSheetBehavior.setState(STATE_COLLAPSED);
        }
    }

    @Override
    public void showCurrentQueueItem(PlayQueueItem item) {
        Composition composition = item.getComposition();
        tvCurrentComposition.setText(formatCompositionName(composition));
        tvTotalTime.setText(formatMilliseconds(composition.getDuration()));
        tvCurrentCompositionAuthor.setText(formatCompositionAuthor(composition, requireContext()));

        ImageFormatUtils.displayImage(ivMusicIcon, composition);

        playQueueAdapter.onCurrentItemChanged(item);
    }

    @Override
    public void scrollQueueToPosition(int position) {
        if (position >= playQueueLayoutManager.findLastVisibleItemPosition()) {
            playQueueLayoutManager.scrollToPositionWithOffset(position, 0);
        } else {
            playQueueLayoutManager.scrollToPosition(position);
        }
    }

    @Override
    public void bindPlayList(List<PlayQueueItem> currentPlayList) {
        playQueueAdapter = new PlayQueueAdapter(currentPlayList);
        playQueueAdapter.setOnCompositionClickListener(presenter::onCompositionItemClicked);
        playQueueAdapter.setOnDeleteCompositionClickListener(presenter::onDeleteCompositionButtonClicked);
        playQueueAdapter.setOnAddToPlaylistClickListener(presenter::onAddQueueItemToPlayListButtonClicked);
        rvPlayList.setAdapter(playQueueAdapter);
    }

    @Override
    public void updatePlayQueue(List<PlayQueueItem> currentPlayList, List<PlayQueueItem> newPlayList) {
        playQueueAdapter.updatePlayList(currentPlayList, newPlayList);
    }

    @Override
    public void showInfinitePlayingButton(boolean active) {
        if (active) {
            int selectedColor = getColorFromAttr(requireContext(), R.attr.colorAccent);
            btnInfinitePlay.setColorFilter(selectedColor);
            btnInfinitePlay.setOnClickListener(v -> presenter.onInfiniteButtonClicked(false));
        } else {
            btnInfinitePlay.clearColorFilter();
            btnInfinitePlay.setOnClickListener(v -> presenter.onInfiniteButtonClicked(true));
        }
    }

    @Override
    public void showRandomPlayingButton(boolean active) {
        if (active) {
            int selectedColor = getColorFromAttr(requireContext(), R.attr.colorAccent);
            btnRandomPlay.setColorFilter(selectedColor);
            btnRandomPlay.setOnClickListener(v -> presenter.onRandomPlayingButtonClicked(false));
        } else {
            btnRandomPlay.clearColorFilter();
            btnRandomPlay.setOnClickListener(v -> presenter.onRandomPlayingButtonClicked(true));
        }
    }

    @Override
    public void showTrackState(long currentPosition, long duration) {
        int progress = 0;
        if (duration != 0) {
            progress = (int) (currentPosition * 100 / duration);
        }
        seekBarViewWrapper.setProgress(progress);
        tvPlayedTime.setText(formatMilliseconds(currentPosition));
    }

    @Override
    public void showShareMusicDialog(String filePath) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("audio/*");
        Uri fileUri = FileProvider.getUriForFile(requireContext(), getString(R.string.file_provider_authorities), new File(filePath));
        intent.putExtra(Intent.EXTRA_STREAM, fileUri);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        startActivity(Intent.createChooser(intent, getString(R.string.share)));
    }

    @Override
    public void showPlayQueueSubtitle(int size) {
        tvQueueSubtitle.setText(getResources().getQuantityString(R.plurals.compositions_count, size, size));
    }

    @Override
    public void showAddingToPlayListError(ErrorCommand errorCommand) {
        Snackbar.make(clPlayQueueContainer,
                getString(R.string.add_to_playlist_error_template, errorCommand.getMessage()),
                Snackbar.LENGTH_SHORT)
                .show();
    }

    @Override
    public void showAddingToPlayListComplete(PlayList playList, List<Composition> compositions) {
        String text = getAddToPlayListCompleteMessage(requireActivity(), playList, compositions);
        Snackbar.make(clPlayQueueContainer, text, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void showSelectPlayListDialog() {
        ChoosePlayListDialogFragment dialog = new ChoosePlayListDialogFragment();
        dialog.setOnCompleteListener(presenter::onPlayListForAddingSelected);
        dialog.show(getChildFragmentManager(), SELECT_PLAYLIST_TAG);
    }

    @Override
    public void showConfirmDeleteDialog(List<Composition> compositionsToDelete) {
        DialogUtils.showConfirmDeleteDialog(requireContext(),
                compositionsToDelete,
                presenter::onDeleteCompositionsDialogConfirmed);
    }

    @Override
    public void showDeleteCompositionError(ErrorCommand errorCommand) {
        Snackbar.make(clPlayQueueContainer,
                getString(R.string.add_to_playlist_error_template, errorCommand.getMessage()),
                Snackbar.LENGTH_SHORT)
                .show();
    }

    @Override
    public void showDeleteCompositionMessage(List<Composition> compositionsToDelete) {
        String text = getDeleteCompleteMessage(requireActivity(), compositionsToDelete);
        Snackbar.make(clPlayQueueContainer, text, Snackbar.LENGTH_SHORT).show();
    }

    private boolean onPlayQueueMenuItemClicked(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.menu_save_as_playlist: {
                CreatePlayListDialogFragment fragment = new CreatePlayListDialogFragment();
                fragment.setOnCompleteListener(presenter::onPlayListForAddingCreated);
                fragment.show(getChildFragmentManager(), CREATE_PLAYLIST_TAG);
                break;
            }
        }
        return true;
    }

    private void onNavigationIconClicked() {
        if (drawer.getDrawerLockMode(GravityCompat.START) != LOCK_MODE_LOCKED_CLOSED) {
            drawer.openDrawer(GravityCompat.START);
        } else {
            onBackPressed();
        }
    }

    private void onBottomPanelClicked() {
        if (bottomSheetBehavior.getState() == STATE_EXPANDED) {
            bottomSheetBehavior.setState(STATE_COLLAPSED);
        }
    }

    private void onTopPanelClicked() {
        if (bottomSheetBehavior.getState() == STATE_COLLAPSED) {
            bottomSheetBehavior.setState(STATE_EXPANDED);
        }
    }

    private void setContentBottomHeight(int heightInPixels) {
        bottomSheetBehavior.setPeekHeight(heightInPixels);

        CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) fragmentContainer.getLayoutParams();
        layoutParams.bottomMargin = heightInPixels;
        fragmentContainer.setLayoutParams(layoutParams);
    }

    private void startFragment(Fragment fragment) {
        FragmentUtils.startFragment(fragment,
                getChildFragmentManager(),
                R.id.drawer_fragment_container);
    }

    private void showScreen(int menuItemId) {
        Fragment fragment = null;
        int screenId = 0;
        switch (menuItemId) {
            case R.id.menu_library: {
                fragment = getLibraryFragment();
                screenId = Screens.LIBRARY;
                break;
            }
            case R.id.menu_play_lists: {
                fragment = new PlayListsFragment();
                screenId = Screens.PLAY_LISTS;
                break;
            }
        }
        uiStatePreferences.setSelectedDrawerScreen(screenId);
        startFragment(fragment);
    }

    private void showDrawerScreen(int screenId) {
        int itemId = 0;
        Fragment fragment = null;
        switch (screenId) {
            case Screens.LIBRARY: {
                itemId = R.id.menu_library;
                fragment = getLibraryFragment();
                break;
            }
            case Screens.PLAY_LISTS: {
                itemId = R.id.menu_play_lists;
                fragment = new PlayListsFragment();
                break;
            }
        }

        selectedDrawerItemId = itemId;
        navigationView.setCheckedItem(itemId);
        startFragment(fragment);
    }

    private Fragment getLibraryFragment() {
        switch (uiStatePreferences.getSelectedLibraryScreen()) {
            case Screens.LIBRARY_COMPOSITIONS: {
                return new LibraryCompositionsFragment();
            }
            case Screens.LIBRARY_FOLDERS: {
                return new LibraryFoldersRootFragment();
            }
            default: {
                return new LibraryCompositionsFragment();
            }
        }
    }

    private void clearFragment() {
        FragmentManager fm = getChildFragmentManager();
        Fragment currentFragment = fm.findFragmentById(R.id.drawer_fragment_container);
        if (currentFragment != null) {
            fm.beginTransaction()
                    .remove(currentFragment)
                    .commit();
        }
    }

    private void onCompositionMenuClicked(View view) {
        PopupMenu popup = new PopupMenu(requireContext(), view);
        popup.inflate(R.menu.composition_full_actions_menu);
        popup.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.menu_add_to_playlist: {
                    presenter.onAddCurrentCompositionToPlayListButtonClicked();
                    return true;
                }
                case R.id.menu_share: {
                    presenter.onShareCompositionButtonClicked();
                    return true;
                }
                case R.id.menu_delete: {
                    presenter.onDeleteCurrentCompositionButtonClicked();
                    return true;
                }
            }
            return false;
        });
        popup.show();
    }

    private DrawerArrowDrawable createDrawerArrowDrawable() {
        DrawerArrowDrawable drawerArrowDrawable = new DrawerArrowDrawable(requireActivity());
        drawerArrowDrawable.setColor(getColorFromAttr(requireActivity(), android.R.attr.textColorPrimaryInverse));
        return drawerArrowDrawable;
    }

    private boolean onNavigationItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (selectedDrawerItemId != itemId) {
            selectedDrawerItemId = itemId;
            itemIdToStart = itemId;
            clearFragment();
        }
        drawer.closeDrawer(Gravity.START);
        return true;
    }

    private void onDrawerClosed() {
        if (itemIdToStart != NO_ITEM) {
            showScreen(itemIdToStart);
        }
    }

    private SlideDelegate getBottomSheetDelegate(DrawerArrowDrawable drawerArrowDrawable) {
        DelegateManager delegateManager = new DelegateManager();
        delegateManager
                .addDelegate(new BoundValuesDelegate(0.4f, 1f, new VisibilityDelegate(playQueueTitleContainer)))
                .addDelegate(new ReverseDelegate(new BoundValuesDelegate(0.0f, 0.8f, new ToolbarMenuVisibilityDelegate(toolbar))))
                .addDelegate(new BoundValuesDelegate(0f, 0.6f, new ReverseDelegate(new VisibilityDelegate(titleContainer))))
                .addDelegate(new TextSizeDelegate(tvCurrentComposition, R.dimen.current_composition_collapse_text_size, R.dimen.current_composition_expand_text_size))
                .addDelegate(new MotionLayoutDelegate(mlBottomSheet))
                .addDelegate(new BoundValuesDelegate(0.95f, 1f, new VisibilityDelegate(rvPlayList)))
                .addDelegate(new BoundValuesDelegate(0.7f, 0.95f, new ReverseDelegate(new VisibilityDelegate(fragmentContainer))))
                .addDelegate(new BoundValuesDelegate(0.3f, 1.0f, new ExpandViewDelegate(R.dimen.music_icon_size, ivMusicIcon)))
                .addDelegate(new BoundValuesDelegate(0.95f, 1.0f, new VisibilityDelegate(tvCurrentCompositionAuthor)))
                .addDelegate(new BoundValuesDelegate(0.4f, 1.0f, new VisibilityDelegate(btnActionsMenu)))
                .addDelegate(new BoundValuesDelegate(0.93f, 1.0f, new VisibilityDelegate(sbTrackState)))
                .addDelegate(new BoundValuesDelegate(0.98f, 1.0f, new VisibilityDelegate(btnInfinitePlay)))
                .addDelegate(new BoundValuesDelegate(0.98f, 1.0f, new VisibilityDelegate(btnRandomPlay)))
                .addDelegate(new BoundValuesDelegate(0.97f, 1.0f, new VisibilityDelegate(tvPlayedTime)))
                .addDelegate(new DrawerArrowDelegate(
                        drawerArrowDrawable,
                        () -> getChildFragmentManager().getBackStackEntryCount() != 0 || toolbar.isInSearchMode()))
                .addDelegate(new BoundValuesDelegate(0.97f, 1.0f, new VisibilityDelegate(tvTotalTime)));

        if (bottomSheetCoordinator != null) {
            delegateManager.addDelegate(new ChangeWidthDelegate(
                    0.5f,
                    bottomSheetCoordinator));
            delegateManager.addDelegate(new LeftBottomShadowDelegate(
                    bottomSheetLeftShadow,
                    bottomSheetTopLeftShadow,
                    mlBottomSheet,
                    bottomSheetCoordinator));
        }
        return new BoundValuesDelegate(0.008f, 0.95f, delegateManager);
    }

    private void onBottomSheetStateChanged(Integer newState) {
        switch (newState) {
            case STATE_COLLAPSED: {
                drawerLockStateProcessor.onBottomSheetOpened(false);
                bottomSheetDelegate.onSlide(0f);
                uiStatePreferences.setPlayerPanelOpen(false);
                return;
            }
            case STATE_EXPANDED: {
                drawerLockStateProcessor.onBottomSheetOpened(true);
                bottomSheetDelegate.onSlide(1f);
                uiStatePreferences.setPlayerPanelOpen(true);
            }
        }
    }

    private void onBottomSheetSlided(Float slideOffset) {
        if (slideOffset > 0F && slideOffset < 1f) {
            bottomSheetDelegate.onSlide(slideOffset);
        }
    }
}
