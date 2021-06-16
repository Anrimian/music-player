package com.github.anrimian.musicplayer.ui.settings.player;

import moxy.MvpView;
import moxy.viewstate.strategy.alias.AddToEndSingle;

public interface PlayerSettingsView extends MvpView {

    @AddToEndSingle
    void showDecreaseVolumeOnAudioFocusLossEnabled(boolean checked);

    @AddToEndSingle
    void showSelectedEqualizerType(int type);
}
