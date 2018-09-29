package com.github.anrimian.musicplayer.ui.utils.slidr;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.View;

import com.r0adkll.slidr.model.SlidrConfig;
import com.r0adkll.slidr.widget.SliderPanel;

class FragmentPanelSlideListener implements SliderPanel.OnPanelSlideListener {

    private final Fragment fragment;
    private final SlidrConfig config;

    private SlidrFragment.SlideListener slideListener;

    FragmentPanelSlideListener(@NonNull Fragment fragment,
                               @NonNull SlidrConfig config,
                               SlidrFragment.SlideListener slideListener) {
        this.fragment = fragment;
        this.config = config;
        this.slideListener = slideListener;
    }


    @Override
    public void onStateChanged(int state) {
        if (config.getListener() != null) {
            config.getListener().onSlideStateChanged(state);
        }
    }


    @Override
    public void onClosed() {
        if (config.getListener() != null) {
            config.getListener().onSlideClosed();
        }

        FragmentManager fm = fragment.requireFragmentManager();
        if (fm.getBackStackEntryCount() > 0) {
            fm.popBackStack();
        }
    }

    @Override
    public void onOpened() {
        if (config.getListener() != null) {
            config.getListener().onSlideOpened();
        }
    }


    @Override
    public void onSlideChange(float percent) {
        if (config.getListener() != null) {
            config.getListener().onSlideChange(percent);
        }
        if (slideListener != null) {
            slideListener.onSlideChange(percent);
        }
    }
}
