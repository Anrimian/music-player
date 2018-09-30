package com.github.anrimian.musicplayer.ui.common;

import android.support.v7.widget.RecyclerView;

import jp.wasabeef.recyclerview.animators.SlideInUpAnimator;

public class AnimationUtils {

    private static final int ANIMATION_DURATION = 250;

    public static RecyclerView.ItemAnimator getStartItemAnimator() {
        RecyclerView.ItemAnimator itemAnimator = new SlideInUpAnimator();
        itemAnimator.setAddDuration(ANIMATION_DURATION);
        itemAnimator.setChangeDuration(ANIMATION_DURATION);
        itemAnimator.setMoveDuration(ANIMATION_DURATION);
        itemAnimator.setRemoveDuration(ANIMATION_DURATION);
        return itemAnimator;
    }
}
