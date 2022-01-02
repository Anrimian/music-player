package com.github.anrimian.musicplayer.ui.utils.slidr;

import androidx.annotation.NonNull;

import com.r0adkll.slidr.model.SlidrConfig;
import com.r0adkll.slidr.widget.SliderPanel;

class PanelSlideListener implements SliderPanel.OnPanelSlideListener {

    private final Runnable closeAction;
    private final SlidrConfig config;

    private final SlidrPanel.SlideListener slideListener;

    PanelSlideListener(@NonNull Runnable closeAction,
                       @NonNull SlidrConfig config,
                       SlidrPanel.SlideListener slideListener) {
        this.closeAction = closeAction;
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

        closeAction.run();
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
