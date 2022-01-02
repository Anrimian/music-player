package com.github.anrimian.musicplayer.ui.utils.views.bottom_sheet;

import android.view.View;

import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.util.LinkedList;
import java.util.List;

import androidx.annotation.NonNull;

public class CompositeBottomSheetCallback extends BottomSheetBehavior.BottomSheetCallback {

    private final List<BottomSheetBehavior.BottomSheetCallback> callbacks = new LinkedList<>();

    @Override
    public void onStateChanged(@NonNull View bottomSheet, int newState) {
        for (BottomSheetBehavior.BottomSheetCallback callback: callbacks) {
            callback.onStateChanged(bottomSheet, newState);
        }
    }

    @Override
    public void onSlide(@NonNull View bottomSheet, float slideOffset) {
        for (BottomSheetBehavior.BottomSheetCallback callback: callbacks) {
            callback.onSlide(bottomSheet, slideOffset);
        }
    }

    public void add(BottomSheetBehavior.BottomSheetCallback callback) {
        callbacks.add(callback);
    }
}
