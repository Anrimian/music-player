package com.github.anrimian.simplemusicplayer.ui.library;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.github.anrimian.simplemusicplayer.R;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created on 19.10.2017.
 */

public class LibraryFragment extends Fragment {

    private static final String BOTTOM_SHEET_STATE = "bottom_sheet_state";

    @BindView(R.id.bottom_sheet)
    FrameLayout bottomSheet;

    @BindView(R.id.appbarlayout)
    AppBarLayout appBarLayout;

    private BottomSheetBehavior<FrameLayout> behavior;

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

        int bottomSheetState = BottomSheetBehavior.STATE_COLLAPSED;
        if (savedInstanceState != null) {
            bottomSheetState = savedInstanceState.getInt(BOTTOM_SHEET_STATE);
        }

        float appBarStartY = appBarLayout.getY();
        /*behavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {

            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                Log.d("LibraryFragment", "slideOffset: " + slideOffset);
                if (slideOffset > 0F && slideOffset < 1f) {
                    int contentHeight = view.getMeasuredHeight() - behavior.getPeekHeight();
                    int appBarHeight = appBarLayout.getMeasuredHeight();
                    //int expandedHeight = (int) ((float) contentHeight) * slideOffset;
                    float appBarY = appBarStartY - (appBarHeight * slideOffset);
                    Log.d("LibraryFragment", "appBarStartY: " + appBarStartY + ", appBarY: " + appBarY);
                    appBarLayout.setY(appBarY);
                    appBarLayout.setAlpha(1 - slideOffset);
                }
                //appBarLayout.setY();
            }
        });*/
        behavior.setState(bottomSheetState);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(BOTTOM_SHEET_STATE, behavior.getState());
    }
}
