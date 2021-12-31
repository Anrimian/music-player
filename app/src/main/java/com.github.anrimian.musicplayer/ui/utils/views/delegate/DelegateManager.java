package com.github.anrimian.musicplayer.ui.utils.views.delegate;

import java.util.LinkedList;
import java.util.List;

/**
 * Created on 13.01.2018.
 */

public class DelegateManager implements SlideDelegate {

    private final List<SlideDelegate> bottomSheetDelegates = new LinkedList<>();

    @Override
    public void onSlide(float slideOffset) {
        for (SlideDelegate delegate : bottomSheetDelegates) {
            delegate.onSlide(slideOffset);
        }
    }

    public DelegateManager addDelegate(SlideDelegate bottomSheetDelegate) {
        bottomSheetDelegates.add(bottomSheetDelegate);
        return this;
    }
}
