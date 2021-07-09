package com.github.anrimian.musicplayer.ui.settings.display;

import moxy.MvpView;
import moxy.viewstate.strategy.alias.AddToEndSingle;

public interface DisplaySettingsView extends MvpView {

    @AddToEndSingle
    void showCoversChecked(boolean checked);

    @AddToEndSingle
    void showCoversInNotificationChecked(boolean checked);

    @AddToEndSingle
    void showColoredNotificationChecked(boolean checked);

    @AddToEndSingle
    void showNotificationCoverStubChecked(boolean checked);

    @AddToEndSingle
    void showCoversOnLockScreenChecked(boolean checked);

    @AddToEndSingle
    void showCoversInNotificationEnabled(boolean enabled);

    @AddToEndSingle
    void showColoredNotificationEnabled(boolean enabled);

    @AddToEndSingle
    void showNotificationCoverStubEnabled(boolean enabled);

    @AddToEndSingle
    void showShowCoversOnLockScreenEnabled(boolean enabled);
}
