package com.github.anrimian.simplemusicplayer.ui.library;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MenuItem;
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
        behavior.setState(bottomSheetState);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(BOTTOM_SHEET_STATE, behavior.getState());
    }
}
