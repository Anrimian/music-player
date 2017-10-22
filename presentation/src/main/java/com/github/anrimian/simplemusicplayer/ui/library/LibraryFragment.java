package com.github.anrimian.simplemusicplayer.ui.library;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.anrimian.simplemusicplayer.R;
import com.github.anrimian.simplemusicplayer.ui.player.music_info.MusicInfoFragment;
import com.github.anrimian.simplemusicplayer.ui.player.play_queue.PlayQueueFragment;
import com.github.anrimian.simplemusicplayer.utils.view_pager.ViewPagerAdapter;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created on 19.10.2017.
 */

public class LibraryFragment extends Fragment {

    private static final String BOTTOM_SHEET_STATE = "bottom_sheet_state";
    private static final String TOOLBAR_Y = "toolbar_y";
    private static final String TOOLBAR_START_Y = "toolbar_start_y";

    @BindView(R.id.bottom_sheet)
    CoordinatorLayout bottomSheet;

    @BindView(R.id.view_pager)
    ViewPager viewPager;

    private View toolbar;

    private BottomSheetBehavior<CoordinatorLayout> behavior;

//    private float appBarStartY;

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
}
