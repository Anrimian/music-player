package com.github.anrimian.musicplayer.ui.settings.display;

import moxy.MvpView;
import moxy.viewstate.strategy.AddToEndSingleStrategy;
import moxy.viewstate.strategy.StateStrategyType;

public interface DisplaySettingsView extends MvpView {

    @StateStrategyType(AddToEndSingleStrategy.class)
    void showCoversChecked(boolean checked);

    @StateStrategyType(AddToEndSingleStrategy.class)
    void showCoversInNotificationChecked(boolean checked);

    @StateStrategyType(AddToEndSingleStrategy.class)
    void showColoredNotificationChecked(boolean checked);

    @StateStrategyType(AddToEndSingleStrategy.class)
    void showCoversOnLockScreenChecked(boolean checked);

    @StateStrategyType(AddToEndSingleStrategy.class)
    void showCoversInNotificationEnabled(boolean enabled);

    @StateStrategyType(AddToEndSingleStrategy.class)
    void showColoredNotificationEnabled(boolean enabled);

    @StateStrategyType(AddToEndSingleStrategy.class)
    void showShowCoversOnLockScreenEnabled(boolean enabled);
}
