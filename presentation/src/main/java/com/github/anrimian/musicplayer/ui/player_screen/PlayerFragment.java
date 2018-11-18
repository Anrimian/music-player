package com.github.anrimian.musicplayer.ui.player_screen;

import android.Manifest;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.motion.MotionLayout;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
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
import com.github.anrimian.musicplayer.di.Components;
import com.github.anrimian.musicplayer.domain.models.Screens;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.composition.PlayQueueItem;
import com.github.anrimian.musicplayer.domain.models.player.modes.RepeatMode;
import com.github.anrimian.musicplayer.domain.models.playlist.PlayList;
import com.github.anrimian.musicplayer.infrastructure.service.MusicServiceManager;
import com.github.anrimian.musicplayer.ui.ScreensMap;
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
import com.github.anrimian.musicplayer.ui.utils.fragments.navigation.FragmentNavigation;
import com.github.anrimian.musicplayer.ui.utils.fragments.navigation.JugglerView;
import com.github.anrimian.musicplayer.ui.utils.views.bottom_sheet.SimpleBottomSheetCallback;
import com.github.anrimian.musicplayer.ui.utils.views.delegate.BoundValuesDelegate;
import com.github.anrimian.musicplayer.ui.utils.views.delegate.DelegateManager;
import com.github.anrimian.musicplayer.ui.utils.views.delegate.DrawerArrowDelegate;
import com.github.anrimian.musicplayer.ui.utils.views.delegate.ExpandViewDelegate;
import com.github.anrimian.musicplayer.ui.utils.views.delegate.LeftBottomShadowDelegate;
import com.github.anrimian.musicplayer.ui.utils.views.delegate.MotionLayoutDelegate;
import com.github.anrimian.musicplayer.ui.utils.views.delegate.MoveXDelegate;
import com.github.anrimian.musicplayer.ui.utils.views.delegate.MoveYDelegate;
import com.github.anrimian.musicplayer.ui.utils.views.delegate.ReverseDelegate;
import com.github.anrimian.musicplayer.ui.utils.views.delegate.SlideDelegate;
import com.github.anrimian.musicplayer.ui.utils.views.delegate.TextSizeDelegate;
import com.github.anrimian.musicplayer.ui.utils.views.delegate.ToolbarMenuVisibilityDelegate;
import com.github.anrimian.musicplayer.ui.utils.views.delegate.VisibilityDelegate;
import com.github.anrimian.musicplayer.ui.utils.views.drawer.SimpleDrawerListener;
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.RecyclerViewUtils;
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.diff_utils.DiffUtilHelper;
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.diff_utils.calculator.ListUpdate;
import com.github.anrimian.musicplayer.ui.utils.views.seek_bar.SeekBarViewWrapper;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.support.design.widget.BottomSheetBehavior.STATE_COLLAPSED;
import static android.support.design.widget.BottomSheetBehavior.STATE_EXPANDED;
import static android.support.v4.widget.DrawerLayout.LOCK_MODE_LOCKED_CLOSED;
import static android.view.View.INVISIBLE;
import static com.github.anrimian.musicplayer.Constants.Tags.CREATE_PLAYLIST_TAG;
import static com.github.anrimian.musicplayer.Constants.Tags.SELECT_PLAYLIST_TAG;
import static com.github.anrimian.musicplayer.ui.common.DialogUtils.shareFile;
import static com.github.anrimian.musicplayer.ui.common.format.FormatUtils.formatCompositionAuthor;
import static com.github.anrimian.musicplayer.ui.common.format.FormatUtils.formatCompositionName;
import static com.github.anrimian.musicplayer.ui.common.format.FormatUtils.formatMilliseconds;
import static com.github.anrimian.musicplayer.ui.common.format.MessagesUtils.getAddToPlayListCompleteMessage;
import static com.github.anrimian.musicplayer.ui.common.format.MessagesUtils.getDeleteCompleteMessage;
import static com.github.anrimian.musicplayer.ui.utils.views.menu.ActionMenuUtil.setupMenu;
import static com.github.anrimian.musicplayer.utils.AndroidUtils.getColorFromAttr;
import static com.github.anrimian.musicplayer.utils.AndroidUtils.getResourceIdFromAttr;
import static com.github.anrimian.musicplayer.utils.ViewUtils.insertMenuItemIcons;

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
    CoordinatorLayout bottomSheetCoordinator;

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

    @BindView(R.id.drawer_fragment_container)
    JugglerView fragmentContainer;

    @BindView(R.id.tv_current_composition)
    TextView tvCurrentComposition;

    @BindView(R.id.btn_infinite_play)
    ImageView btnRepeatMode;

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

    @BindView(R.id.iv_music_icon)
    ImageView ivMusicIcon;

    @BindView(R.id.btn_actions_menu)
    ImageView btnActionsMenu;

    @BindView(R.id.tv_current_composition_author)
    TextView tvCurrentCompositionAuthor;

    @BindView(R.id.cl_play_queue_container)
    CoordinatorLayout clPlayQueueContainer;

    @BindView(R.id.ml_bottom_sheet)
    MotionLayout mlBottomSheet;

    @BindView(R.id.toolbar)
    AdvancedToolbar toolbar;

    @BindView(R.id.toolbar_content_container)
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

    private int selectedDrawerItemId = NO_ITEM;
    private int itemIdToStart = NO_ITEM;

    private LinearLayoutManager playQueueLayoutManager;
    private SeekBarViewWrapper seekBarViewWrapper;

    private DrawerLockStateProcessor drawerLockStateProcessor;

    private FragmentNavigation navigation;

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
        setViewStartState();

        RxPermissions rxPermissions = new RxPermissions(requireActivity());
        if (!rxPermissions.isGranted(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            requireFragmentManager()
                    .beginTransaction()
                    .replace(R.id.main_activity_container, new StartFragment())
                    .commit();
            return;
        }
        MusicServiceManager.initialize();

        toolbar.initializeViews();
        toolbar.setupWithActivity((AppCompatActivity) requireActivity());

        navigation = FragmentNavigation.from(getChildFragmentManager());
        navigation.initialize(fragmentContainer);
        navigation.checkForEqualityOnReplace(true);
        navigation.setExitAnimation(R.anim.anim_slide_out_right);
        navigation.setEnterAnimation(R.anim.anim_slide_in_right);
        navigation.setRootExitAnimation(R.anim.anim_alpha_disappear);

        drawerLockStateProcessor = new DrawerLockStateProcessor(drawer);
        drawerLockStateProcessor.setupWithNavigation(navigation);
        toolbar.setSearchModeListener(drawerLockStateProcessor::onSearchModeChanged);

        navigationView.setNavigationItemSelectedListener(this::onNavigationItemSelected);
        navigationView.inflateHeaderView(R.layout.partial_drawer_header);

        ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(requireActivity(), drawer, R.string.open_drawer, R.string.close_drawer);
        DrawerArrowDrawable drawerArrowDrawable = createDrawerArrowDrawable();
        drawerToggle.setDrawerArrowDrawable(drawerArrowDrawable);

        drawer.addDrawerListener(new SimpleDrawerListener(this::onDrawerClosed));

        setupMenu(actionMenuView, R.menu.play_queue_menu, this::onPlayQueueMenuItemClicked);

        bottomSheetDelegate = getBottomSheetDelegate(drawerArrowDrawable);
        bottomSheetBehavior = BottomSheetBehavior.from(mlBottomSheet);
        mlBottomSheet.setClickable(true);
        bottomSheetBehavior.setBottomSheetCallback(new SimpleBottomSheetCallback(
                this::onBottomSheetStateChanged,
                this::onBottomSheetSlided
        ));

        toolbar.setupWithNavigation(navigation,
                drawerArrowDrawable,
                () -> bottomSheetBehavior.getState() == STATE_EXPANDED);

        ivSkipToPrevious.setOnClickListener(v -> presenter.onSkipToPreviousButtonClicked());
        ivSkipToNext.setOnClickListener(v -> presenter.onSkipToNextButtonClicked());
        btnRepeatMode.setOnClickListener(this::onRepeatModeButtonClicked);

        playQueueLayoutManager = new LinearLayoutManager(requireContext());
        rvPlayList.setLayoutManager(playQueueLayoutManager);
        RecyclerViewUtils.attachSwipeToDelete(rvPlayList,
                getColorFromAttr(requireContext(), R.attr.colorAccent),
                presenter::onItemSwipedToDelete);

        if (savedInstanceState != null) {
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

    private void setViewStartState() {
        playQueueTitleContainer.setVisibility(INVISIBLE);
        titleContainer.setVisibility(INVISIBLE);
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
    public void onDestroyView() {
        super.onDestroyView();
        toolbar.release();
        drawerLockStateProcessor.release();
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

        Fragment fragment = navigation.getFragmentOnTop();
        boolean processed = fragment instanceof BackButtonListener
                && ((BackButtonListener) fragment).onBackPressed();
        if (!processed) {
            processed = FragmentNavigation.from(getChildFragmentManager()).goBack();
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
    public void expandBottomPanel() {
        setButtonsSelectableBackground(R.drawable.bg_selectable_round_shape);

        drawerLockStateProcessor.onBottomSheetOpened(true);
        bottomSheetDelegate.onSlide(1f);
        if (bottomSheetBehavior.getState() != STATE_EXPANDED) {
            bottomSheetBehavior.setState(STATE_EXPANDED);
        }
    }

    @Override
    public void collapseBottomPanel() {
        setButtonsSelectableBackground(
                getResourceIdFromAttr(requireContext(),
                        R.attr.selectableItemBackgroundBorderless)
        );

        drawerLockStateProcessor.onBottomSheetOpened(false);
        bottomSheetDelegate.onSlide(0f);
        if (bottomSheetBehavior.getState() != STATE_COLLAPSED) {
            bottomSheetBehavior.setState(STATE_COLLAPSED);
        }
    }

    @Override
    public void showDrawerScreen(int screenId) {
        int itemId = ScreensMap.getMenuIdId(screenId);
        selectedDrawerItemId = itemId;
        navigationView.setCheckedItem(itemId);

        switch (screenId) {
            case Screens.LIBRARY: {
                presenter.onLibraryScreenSelected();
                break;
            }
            case Screens.PLAY_LISTS: {
                startFragment(new PlayListsFragment());
                break;
            }
        }
    }

    @Override
    public void showLibraryScreen(int selectedLibraryScreen) {
        Fragment fragment;
        switch (selectedLibraryScreen) {
            case Screens.LIBRARY_COMPOSITIONS: {
                fragment = new LibraryCompositionsFragment();
                break;
            }
            case Screens.LIBRARY_FOLDERS: {
                fragment = new LibraryFoldersRootFragment();
                break;
            }
            default: {
                fragment = new LibraryCompositionsFragment();
            }
        }
        startFragment(fragment);
    }

    @Override
    public void showStopState() {
        ivPlayPause.setImageResource(R.drawable.ic_play);
        ivPlayPause.setContentDescription(getString(R.string.play));
        ivPlayPause.setOnClickListener(v -> presenter.onPlayButtonClicked());
    }

    @Override
    public void showPlayState() {
        ivPlayPause.setImageResource(R.drawable.ic_pause);
        ivPlayPause.setContentDescription(getString(R.string.pause));
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
    public void showCurrentQueueItem(PlayQueueItem item, int position) {
        Composition composition = item.getComposition();
        tvCurrentComposition.setText(formatCompositionName(composition));
        tvTotalTime.setText(formatMilliseconds(composition.getDuration()));
        tvCurrentCompositionAuthor.setText(formatCompositionAuthor(composition, requireContext()));
        seekBarViewWrapper.setMax(composition.getDuration());

        ImageFormatUtils.displayImage(ivMusicIcon, composition);

        playQueueAdapter.onCurrentItemChanged(item, position);
    }

    @Override
    public void scrollQueueToPosition(int position, boolean smoothScroll) {
        if (position > playQueueLayoutManager.findFirstVisibleItemPosition() &&
                position < playQueueLayoutManager.findLastVisibleItemPosition()) {
            return;
        }

        rvPlayList.post(() -> {
            if (smoothScroll) {
                RecyclerViewUtils.smoothScrollToTop(position,
                        playQueueLayoutManager,
                        requireContext(),
                        200);
            } else {
                playQueueLayoutManager.scrollToPositionWithOffset(position, 0);
            }
        });
    }

    @Override
    public void updatePlayQueue(ListUpdate<PlayQueueItem> update, boolean keepPosition) {
        List<PlayQueueItem> list = update.getNewList();
        if (playQueueAdapter == null) {
            playQueueAdapter = new PlayQueueAdapter(list);
            playQueueAdapter.setOnCompositionClickListener(presenter::onCompositionItemClicked);
            playQueueAdapter.setOnDeleteCompositionClickListener(presenter::onDeleteCompositionButtonClicked);
            playQueueAdapter.setOnAddToPlaylistClickListener(presenter::onAddQueueItemToPlayListButtonClicked);
            playQueueAdapter.setOnDeleteItemClickListener(presenter::onDeleteQueueItemClicked);
            rvPlayList.setAdapter(playQueueAdapter);
        } else {
            playQueueAdapter.setItems(list);
            if (keepPosition) {
                DiffUtilHelper.update(update.getDiffResult(), rvPlayList);
            } else {
                update.getDiffResult().dispatchUpdatesTo(playQueueAdapter);
            }
        }
    }

    @Override
    public void showRepeatMode(int mode) {
        @DrawableRes int iconRes = R.drawable.ic_repeat_off;
        switch (mode) {
            case RepeatMode.NONE: {
                iconRes = R.drawable.ic_repeat_off;
                break;
            }
            case RepeatMode.REPEAT_COMPOSITION: {
                iconRes = R.drawable.ic_repeat_once;
                break;
            }
            case RepeatMode.REPEAT_PLAY_LIST: {
                iconRes = R.drawable.ic_repeat;
                break;
            }
        }
        btnRepeatMode.setImageResource(iconRes);
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
        seekBarViewWrapper.setProgress(currentPosition);
        tvPlayedTime.setText(formatMilliseconds(currentPosition));
    }

    @Override
    public void showShareMusicDialog(String filePath) {
        shareFile(requireContext(), filePath);
    }

    @Override
    public void showPlayQueueSubtitle(int size) {
        tvQueueSubtitle.setText(getResources().getQuantityString(R.plurals.compositions_count, size, size));
    }

    @Override
    public void setSkipToNextButtonEnabled(boolean enabled) {
        int color = enabled? ContextCompat.getColor(requireContext(), R.color.icon_color) :
                getColorFromAttr(requireContext(), R.attr.colorControlNormal);
        ivSkipToNext.setColorFilter(color);
        ivSkipToNext.setEnabled(enabled);
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

    private void setButtonsSelectableBackground(@DrawableRes int resId) {
        ivPlayPause.setBackgroundResource(resId);
        ivSkipToNext.setBackgroundResource(resId);
        ivSkipToPrevious.setBackgroundResource(resId);
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
        navigation.newRootFragment(() -> fragment, 0, R.anim.anim_alpha_appear);
    }

    private void clearFragment() {
        navigation.clearRootFragment(R.anim.anim_alpha_disappear);
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
            int screenId = ScreensMap.getScreenId(itemIdToStart);
            presenter.onDrawerScreenSelected(screenId);
        }
    }

    private void onRepeatModeButtonClicked(View view) {
        PopupMenu popup = new PopupMenu(requireContext(), view);
        popup.inflate(R.menu.repeat_mode_menu);
        popup.setOnMenuItemClickListener(item -> {
            int repeatMode = RepeatMode.NONE;
            switch (item.getItemId()) {
                case R.id.menu_repeat_playlist: {
                    repeatMode = RepeatMode.REPEAT_PLAY_LIST;
                    break;
                }
                case R.id.menu_repeat_composition: {
                    repeatMode = RepeatMode.REPEAT_COMPOSITION;
                    break;
                }
                case R.id.menu_do_not_repeat: {
                    repeatMode = RepeatMode.NONE;
                    break;
                }
            }
            presenter.onRepeatModeChanged(repeatMode);
            return true;
        });
        insertMenuItemIcons(requireContext(), popup);
        popup.show();
    }

    private SlideDelegate getBottomSheetDelegate(DrawerArrowDrawable drawerArrowDrawable) {
        DelegateManager boundDelegateManager = new DelegateManager();
        boundDelegateManager
                .addDelegate(new BoundValuesDelegate(0.4f, 1f, new VisibilityDelegate(playQueueTitleContainer)))
                .addDelegate(new ReverseDelegate(new BoundValuesDelegate(0.0f, 0.8f, new ToolbarMenuVisibilityDelegate(toolbar))))
                .addDelegate(new BoundValuesDelegate(0f, 0.6f, new ReverseDelegate(new VisibilityDelegate(titleContainer))))
                .addDelegate(new TextSizeDelegate(tvCurrentComposition, R.dimen.current_composition_collapse_text_size, R.dimen.current_composition_expand_text_size))
                .addDelegate(new MotionLayoutDelegate(mlBottomSheet))
                .addDelegate(new BoundValuesDelegate(0.7f, 0.95f, new ReverseDelegate(new VisibilityDelegate(fragmentContainer))))
                .addDelegate(new BoundValuesDelegate(0.3f, 1.0f, new ExpandViewDelegate(R.dimen.music_icon_size, ivMusicIcon)))
                .addDelegate(new BoundValuesDelegate(0.95f, 1.0f, new VisibilityDelegate(tvCurrentCompositionAuthor)))
                .addDelegate(new BoundValuesDelegate(0.4f, 1.0f, new VisibilityDelegate(btnActionsMenu)))
                .addDelegate(new BoundValuesDelegate(0.93f, 1.0f, new VisibilityDelegate(sbTrackState)))
                .addDelegate(new BoundValuesDelegate(0.98f, 1.0f, new VisibilityDelegate(btnRepeatMode)))
                .addDelegate(new BoundValuesDelegate(0.98f, 1.0f, new VisibilityDelegate(btnRandomPlay)))
                .addDelegate(new BoundValuesDelegate(0.97f, 1.0f, new VisibilityDelegate(tvPlayedTime)))
                .addDelegate(new DrawerArrowDelegate(
                        drawerArrowDrawable,
                        () -> navigation.getScreensCount() > 1 || toolbar.isInSearchMode()))
                .addDelegate(new BoundValuesDelegate(0.97f, 1.0f, new VisibilityDelegate(tvTotalTime)));

        DelegateManager delegateManager = new DelegateManager();
        if (bottomSheetCoordinator != null) {//landscape
            boundDelegateManager.addDelegate(new MoveXDelegate(
                    0.5f,
                    bottomSheetCoordinator));
            boundDelegateManager.addDelegate(new LeftBottomShadowDelegate(
                    bottomSheetLeftShadow,
                    bottomSheetTopLeftShadow,
                    mlBottomSheet,
                    bottomSheetCoordinator));
            delegateManager.addDelegate(new MoveYDelegate(clPlayQueueContainer, 0.85f));
        } else {
            boundDelegateManager.addDelegate(new BoundValuesDelegate(0.90f, 1f, new VisibilityDelegate(clPlayQueueContainer)));
            delegateManager.addDelegate(new MoveYDelegate(clPlayQueueContainer, 0.3f));
        }
        delegateManager.addDelegate(new BoundValuesDelegate(0.008f, 0.95f, boundDelegateManager));
        return delegateManager;
    }

    private void onBottomSheetStateChanged(Integer newState) {
        switch (newState) {
            case STATE_COLLAPSED: {
                presenter.onBottomPanelCollapsed();
                return;
            }
            case STATE_EXPANDED: {
                presenter.onBottomPanelExpanded();
            }
        }
    }

    private void onBottomSheetSlided(Float slideOffset) {
        if (slideOffset > 0F && slideOffset < 1f) {
            bottomSheetDelegate.onSlide(slideOffset);
        }
    }
}
