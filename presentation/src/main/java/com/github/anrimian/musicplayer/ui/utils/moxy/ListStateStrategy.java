package com.github.anrimian.musicplayer.ui.utils.moxy;

import com.arellomobile.mvp.MvpView;
import com.arellomobile.mvp.viewstate.ViewCommand;
import com.arellomobile.mvp.viewstate.strategy.StateStrategy;

import java.util.List;

public class ListStateStrategy implements StateStrategy {

    private static final int NO_POSITION = -1;

    @Override
    public <View extends MvpView> void beforeApply(List<ViewCommand<View>> currentState, ViewCommand<View> incomingCommand) {
        int lastSameCommandPosition = -1;
        for (int i = 0; i < currentState.size(); i++) {
            ViewCommand<View> entry = currentState.get(i);

            if (entry.getClass() == incomingCommand.getClass()) {
                lastSameCommandPosition = i;
            } else {
                break;
            }
        }
        currentState.add(lastSameCommandPosition + 1, incomingCommand);
    }

    @Override
    public <View extends MvpView> void afterApply(List<ViewCommand<View>> currentState, ViewCommand<View> incomingCommand) {
        int firstCommandPosition = NO_POSITION;
        for (int i = 0; i < currentState.size(); i++) {
            ViewCommand<View> entry = currentState.get(i);

            if (entry.getClass() == incomingCommand.getClass()) {
                if (firstCommandPosition == NO_POSITION) {
                    firstCommandPosition = i;
                } else {
                    currentState.remove(firstCommandPosition);
                    break;
                }
            } else {
                break;
            }
        }
    }
}
