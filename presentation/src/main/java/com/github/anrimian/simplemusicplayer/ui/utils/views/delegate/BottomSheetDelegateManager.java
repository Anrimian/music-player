package com.github.anrimian.simplemusicplayer.ui.utils.views.delegate;

import java.util.LinkedList;
import java.util.List;

/**
 * Created on 13.01.2018.
 */

public class BottomSheetDelegateManager implements BottomSheetDelegate {

    private List<BottomSheetDelegate> bottomSheetDelegates = new LinkedList<>();

    @Override
    public void onSlide(float slideOffset) {
        for (BottomSheetDelegate delegate : bottomSheetDelegates) {
            delegate.onSlide(slideOffset);
        }
    }

    public BottomSheetDelegateManager addDelegate(BottomSheetDelegate bottomSheetDelegate) {
        bottomSheetDelegates.add(bottomSheetDelegate);
        return this;
    }
}
