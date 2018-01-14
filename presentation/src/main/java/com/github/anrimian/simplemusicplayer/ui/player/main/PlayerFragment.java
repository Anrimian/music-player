package com.github.anrimian.simplemusicplayer.ui.player.main;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.arellomobile.mvp.MvpAppCompatFragment;
import com.arellomobile.mvp.presenter.InjectPresenter;
import com.arellomobile.mvp.presenter.ProvidePresenter;
import com.github.anrimian.simplemusicplayer.R;
import com.github.anrimian.simplemusicplayer.di.Components;
import com.github.anrimian.simplemusicplayer.domain.models.Composition;
import com.github.anrimian.simplemusicplayer.infrastructure.service.MusicService;
import com.github.anrimian.simplemusicplayer.ui.library.storage.StorageLibraryFragment;
import com.github.anrimian.simplemusicplayer.ui.player.main.view.adapter.PlayListAdapter;
import com.github.anrimian.simplemusicplayer.utils.fragments.BackButtonListener;
import com.github.anrimian.simplemusicplayer.utils.views.bottom_sheet.BottomSheetDelegateManager;
import com.github.anrimian.simplemusicplayer.utils.views.bottom_sheet.TargetViewBottomSheetDelegate;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.github.anrimian.simplemusicplayer.utils.format.FormatUtils.formatMilliseconds;

/**
 * Created on 19.10.2017.
 */

public class PlayerFragment extends MvpAppCompatFragment implements PlayerView, BackButtonListener {

    private static final String BOTTOM_SHEET_STATE = "bottom_sheet_state";
    private static final String TOOLBAR_Y = "toolbar_y";
    private static final String TOOLBAR_START_Y = "toolbar_start_y";

    @InjectPresenter
    PlayerPresenter presenter;

    @BindView(R.id.bottom_sheet)
    CoordinatorLayout bottomSheet;

//    @BindView(R.id.view_pager)
//    ViewPager viewPager;

    @BindView(R.id.rv_playlist)
    RecyclerView rvPlayList;

    @BindView(R.id.iv_play_pause)
    ImageView ivPlayPause;

    @BindView(R.id.iv_skip_to_previous)
    ImageView ivSkipToPrevious;

    @BindView(R.id.iv_skip_to_next)
    ImageView ivSkipToNext;

    @BindView(R.id.iv_play_pause_expanded)
    ImageView ivPlayPauseExpanded;

    @BindView(R.id.iv_skip_to_previous_expanded)
    ImageView ivSkipToPreviousExpanded;

    @BindView(R.id.iv_skip_to_next_expanded)
    ImageView ivSkipToNextExpanded;

    @BindView(R.id.library_fragment_container)
    ViewGroup fragmentContainer;

    @BindView(R.id.tv_description)
    TextView tvDescription;

    @BindView(R.id.btn_infinite_play)
    Button btnInfinitePlay;

    @BindView(R.id.btn_random_play)
    Button btnRandomPlay;

    @BindView(R.id.tv_played_time)
    TextView tvPlayedTime;

    @BindView(R.id.pb_track_state)
    ProgressBar pbTrackState;

    private View toolbar;

    private BottomSheetBehavior<CoordinatorLayout> behavior;

    private PlayListAdapter playListAdapter;

    private MusicServiceConnection musicServiceConnection = new MusicServiceConnection();
    private BottomSheetDelegateManager bottomSheetDelegateManager = new BottomSheetDelegateManager();

    @ProvidePresenter
    PlayerPresenter providePresenter() {
        return Components.getLibraryComponent().libraryPresenter();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_library, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        //noinspection ConstantConditions
        getActivity().setTitle(R.string.library);

        bottomSheetDelegateManager.addDelegate(new TargetViewBottomSheetDelegate(ivPlayPause, ivPlayPauseExpanded))
                .addDelegate(new TargetViewBottomSheetDelegate(ivSkipToPrevious, ivSkipToPreviousExpanded))
                .addDelegate(new TargetViewBottomSheetDelegate(ivSkipToNext, ivSkipToNextExpanded));

        behavior = BottomSheetBehavior.from(bottomSheet);
        bottomSheet.setClickable(true);

        toolbar = getActivity().findViewById(R.id.toolbar);

        int bottomSheetState = BottomSheetBehavior.STATE_COLLAPSED;
        if (savedInstanceState != null) {
            bottomSheetState = savedInstanceState.getInt(BOTTOM_SHEET_STATE);
            if (bottomSheetState == BottomSheetBehavior.STATE_EXPANDED) {
                bottomSheetDelegateManager.onSlide(1f);
            }
//            toolbar.setY(savedInstanceState.getFloat(TOOLBAR_Y));
        }
        behavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                System.out.println(newState);
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                if (slideOffset > 0F && slideOffset < 1f) {
                    bottomSheetDelegateManager.onSlide(slideOffset);
                }
            }
        });

        /*appBarStartY = toolbar.getY();
        behavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {

            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                Log.d("LibraryFragment", "slideOffset: " + slideOffset);
                if (slideOffset > 0F && slideOffset < 1f) {
                    int contentHeight = view.getMeasuredHeight() - behavior.getPeekHeight();
                    int appBarHeight = toolbar.getMeasuredHeight();
                    //int expandedHeight = (int) ((float) contentHeight) * slideOffset;
                    float appBarY = appBarStartY - (appBarHeight * slideOffset);
                    Log.d("LibraryFragment", "appBarStartY: " + appBarStartY + ", appBarY: " + appBarY);
                    toolbar.setY(appBarY);
                    toolbar.setAlpha(1 - slideOffset);
                }
                //appBarLayout.setY();
            }
        });*/
        behavior.setState(bottomSheetState);

//        ViewPagerAdapter pagerAdapter = new ViewPagerAdapter(getChildFragmentManager());
//        pagerAdapter.addFragment(MusicInfoFragment::new);
//        pagerAdapter.addFragment(PlayQueueFragment::new);
//        viewPager.setAdapter(pagerAdapter);

        startFragment(StorageLibraryFragment.newInstance(null));

        ivSkipToPrevious.setOnClickListener(v -> presenter.onSkipToPreviousButtonClicked());
        ivSkipToNext.setOnClickListener(v -> presenter.onSkipToNextButtonClicked());
        btnInfinitePlay.setOnClickListener(v -> presenter.onInfinitePlayingButtonClicked(!v.isSelected()));
        btnRandomPlay.setOnClickListener(v -> presenter.onRandomPlayingButtonClicked(!v.isSelected()));

        rvPlayList.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    @Override
    public void onStart() {
        super.onStart();
        presenter.onStart();
        Intent intent = new Intent(getActivity(), MusicService.class);
        getActivity().startService(intent);
        getActivity().bindService(intent, musicServiceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onStop() {
        super.onStop();
        getActivity().unbindService(musicServiceConnection);
        presenter.onStop();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
//        toolbar.setY(appBarStartY);
//        toolbar.setAlpha(1);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(BOTTOM_SHEET_STATE, behavior.getState());
//        outState.putFloat(TOOLBAR_Y, toolbar.getY());
//        outState.putFloat(TOOLBAR_START_Y, appBarStartY);
    }

    @Override
    public void onDetach() {
        super.onDetach();
//        coordinatorDelegate.onDetach();
    }

    @Override
    public void showStopState() {
        setContentBottomHeight(getResources().getDimensionPixelSize(R.dimen.bottom_sheet_height));
        ivPlayPause.setImageResource(R.drawable.ic_play);
        ivPlayPause.setOnClickListener(v -> presenter.onPlayButtonClicked());
    }

    @Override
    public void showPlayState() {
        setContentBottomHeight(getResources().getDimensionPixelSize(R.dimen.bottom_sheet_height));
        ivPlayPause.setImageResource(R.drawable.ic_pause);
        ivPlayPause.setOnClickListener(v -> presenter.onStopButtonClicked());
    }

    @Override
    public void hideMusicControls() {
        setContentBottomHeight(0);
    }

    @Override
    public void showCurrentComposition(Composition composition) {
        tvDescription.setText(composition.getDisplayName());
    }

    @Override
    public void bindPlayList(List<Composition> currentPlayList) {
        playListAdapter = new PlayListAdapter(currentPlayList);
        rvPlayList.setAdapter(playListAdapter);
    }

    @Override
    public void updatePlayList() {
        playListAdapter.notifyDataSetChanged();
    }

    @Override
    public void showInfinitePlayingButton(boolean active) {
        btnInfinitePlay.setSelected(active);
        btnInfinitePlay.setPressed(active);
        btnInfinitePlay.setText(active? "INFINITE(ON)": "INFINITE(OFF)");
    }

    @Override
    public void showRandomPlayingButton(boolean active) {
        btnRandomPlay.setSelected(active);
        btnRandomPlay.setPressed(active);
        btnRandomPlay.setText(active? "RANDOM(ON)": "RANDOM(OFF)");
    }

    @Override
    public void showTrackState(long currentPosition, long duration) {
        int progress = (int) (currentPosition * 100 / duration);
        pbTrackState.setProgress(progress);
        tvPlayedTime.setText(formatMilliseconds(currentPosition));
    }

    @Override
    public boolean onBackPressed() {
        Fragment fragment = getChildFragmentManager().findFragmentById(R.id.library_fragment_container);
        return fragment instanceof BackButtonListener && ((BackButtonListener) fragment).onBackPressed();
    }

    private static final float UNDEFINED = -1;

    private float btnPlayStartX = UNDEFINED;
    private float btnPlayStartY = UNDEFINED;
    private float btnPlayEndX = UNDEFINED;
    private float btnPlayEndY = UNDEFINED;

    private void onSlide(float slideOffset) {
        if (btnPlayStartX == UNDEFINED) {
            btnPlayStartX = ivPlayPause.getX();
            btnPlayStartY = ivPlayPause.getY();
            btnPlayEndX = ivPlayPauseExpanded.getX();
            btnPlayEndY = ivPlayPauseExpanded.getY();
        }
        float deltaX = btnPlayEndX - btnPlayStartX;
        float deltaY = btnPlayEndY - btnPlayStartY;
        ivPlayPause.setX(btnPlayStartX + (deltaX * slideOffset));
        ivPlayPause.setY(btnPlayStartY + (deltaY * slideOffset));
    }

    private void setContentBottomHeight(int heightInPixels) {
        behavior.setPeekHeight(heightInPixels);

        CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) fragmentContainer.getLayoutParams();
        layoutParams.bottomMargin = heightInPixels;
        fragmentContainer.setLayoutParams(layoutParams);
    }

    private void startFragment(Fragment fragment) {
        FragmentManager fragmentManager = getChildFragmentManager();
        Fragment existFragment = fragmentManager.findFragmentById(R.id.library_fragment_container);
        if (existFragment == null || existFragment.getClass() != fragment.getClass()) {
            fragmentManager.beginTransaction()
                    .setCustomAnimations(R.anim.anim_alpha_appear, R.anim.anim_alpha_disappear)
                    .replace(R.id.library_fragment_container, fragment)
                    .commit();
        }
    }

    private class MusicServiceConnection implements android.content.ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {

        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    }
}
