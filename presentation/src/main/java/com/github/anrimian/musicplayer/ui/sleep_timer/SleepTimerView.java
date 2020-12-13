package com.github.anrimian.musicplayer.ui.sleep_timer;

import com.github.anrimian.musicplayer.domain.interactors.player.SleepTimerInteractor;

import moxy.MvpView;
import moxy.viewstate.strategy.AddToEndSingleStrategy;
import moxy.viewstate.strategy.StateStrategyType;

public interface SleepTimerView extends MvpView {

    @StateStrategyType(AddToEndSingleStrategy.class)
    void showSleepTimerTime(long sleepTimerTimeMillis);

    @StateStrategyType(AddToEndSingleStrategy.class)
    void showSleepTimerState(SleepTimerInteractor.SleepTimerState sleepTimerState);

    @StateStrategyType(AddToEndSingleStrategy.class)
    void showSleepRemainingSeconds(long remainingSeconds);
}
