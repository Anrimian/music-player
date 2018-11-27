package com.github.anrimian.musicplayer.ui.utils.views.bottom_sheet;

import android.view.View;

import com.github.anrimian.musicplayer.domain.utils.java.Callback;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import androidx.annotation.NonNull;

public class SimpleBottomSheetCallback extends BottomSheetBehavior.BottomSheetCallback {

    private Callback<Integer> onStateChangedCallback;
    private Callback<Float> onSlideCallback;

    public SimpleBottomSheetCallback(Callback<Integer> onStateChangedCallback,
                                     Callback<Float> onSlideCallback) {
        this.onStateChangedCallback = onStateChangedCallback;
        this.onSlideCallback = onSlideCallback;
    }

    @Override
    public void onStateChanged(@NonNull View bottomSheet, int newState) {
        if (onStateChangedCallback != null) {
            onStateChangedCallback.call(newState);
        }
    }

    @Override
    public void onSlide(@NonNull View bottomSheet, float slideOffset) {
        if (onSlideCallback != null && slideOffset > 0F && slideOffset < 1f) {
            onSlideCallback.call(slideOffset);
        }
    }
}
