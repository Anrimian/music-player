package com.github.anrimian.simplemusicplayer.ui.library.main;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.arellomobile.mvp.MvpAppCompatFragment;
import com.arellomobile.mvp.presenter.InjectPresenter;
import com.arellomobile.mvp.presenter.ProvidePresenter;
import com.github.anrimian.simplemusicplayer.R;
import com.github.anrimian.simplemusicplayer.di.Components;
import com.github.anrimian.simplemusicplayer.ui.library.storage.StorageLibraryFragment;
import com.github.anrimian.simplemusicplayer.ui.player.music_info.MusicInfoFragment;
import com.github.anrimian.simplemusicplayer.ui.player.play_queue.PlayQueueFragment;
import com.github.anrimian.simplemusicplayer.utils.view_pager.ViewPagerAdapter;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created on 19.10.2017.
 */

public class LibraryFragment extends MvpAppCompatFragment implements LibraryView {

    private static final String BOTTOM_SHEET_STATE = "bottom_sheet_state";
    private static final String TOOLBAR_Y = "toolbar_y";
    private static final String TOOLBAR_START_Y = "toolbar_start_y";

    @InjectPresenter
    LibraryPresenter presenter;

    @BindView(R.id.bottom_sheet)
    CoordinatorLayout bottomSheet;

    @BindView(R.id.view_pager)
    ViewPager viewPager;

    @BindView(R.id.iv_play_pause)
    ImageView ivPlayPause;

    @BindView(R.id.iv_skip_to_previous)
    ImageView ivSkipToPrevious;

    @BindView(R.id.iv_skip_to_next)
    ImageView ivSkipToNext;

    private View toolbar;

    private BottomSheetBehavior<CoordinatorLayout> behavior;

//    private float appBarStartY;

//    private FragmentCoordinatorDelegate coordinatorDelegate;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
//        coordinatorDelegate = new FragmentCoordinatorDelegate(getActivity(), R.id.drawer_fragment_container);
//        coordinatorDelegate.onAttach();
    }

    @ProvidePresenter
    LibraryPresenter providePresenter() {
        return Components.getLibraryComponent().libraryPresenter();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_library, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        behavior = BottomSheetBehavior.from(bottomSheet);
        bottomSheet.setClickable(true);//save map from clicks


        toolbar = getActivity().findViewById(R.id.toolbar);

        int bottomSheetState = BottomSheetBehavior.STATE_COLLAPSED;
        if (savedInstanceState != null) {
            bottomSheetState = savedInstanceState.getInt(BOTTOM_SHEET_STATE);
            toolbar.setY(savedInstanceState.getFloat(TOOLBAR_Y));
        }


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

        ViewPagerAdapter pagerAdapter = new ViewPagerAdapter(getChildFragmentManager());
        pagerAdapter.addFragment(MusicInfoFragment::new);
        pagerAdapter.addFragment(PlayQueueFragment::new);
        viewPager.setAdapter(pagerAdapter);

        startFragment(StorageLibraryFragment.newInstance(null));

        ivPlayPause.setOnClickListener(v -> presenter.onPlayPauseButtonClicked());
        ivSkipToPrevious.setOnClickListener(v -> presenter.onSkipToPreviousButtonClicked());
        ivSkipToNext.setOnClickListener(v -> presenter.onSkipToNextButtonClicked());
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
        ivPlayPause.setImageResource(R.drawable.ic_play);
    }

    @Override
    public void showPlayState() {
        ivPlayPause.setImageResource(R.drawable.ic_pause);
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
}
