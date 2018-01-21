package com.github.anrimian.simplemusicplayer.utils.views.bottom_sheet;

/**
 * Created on 21.01.2018.
 */

public class ReverseDelegate implements BottomSheetDelegate {

    private BottomSheetDelegate delegate;

    public ReverseDelegate(BottomSheetDelegate delegate) {
        this.delegate = delegate;
    }

    @Override
    public void onSlide(float slideOffset) {
        delegate.onSlide(1 - slideOffset);
    }
}
