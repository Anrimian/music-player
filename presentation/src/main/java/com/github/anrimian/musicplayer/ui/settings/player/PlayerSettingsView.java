package com.github.anrimian.musicplayer.ui.settings.player;

import com.arellomobile.mvp.MvpView;
import com.arellomobile.mvp.viewstate.strategy.AddToEndSingleStrategy;
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType;

public interface PlayerSettingsView extends MvpView {

    @StateStrategyType(AddToEndSingleStrategy.class)
    void showDecreaseVolumeOnAudioFocusLossEnabled(boolean checked);
}
