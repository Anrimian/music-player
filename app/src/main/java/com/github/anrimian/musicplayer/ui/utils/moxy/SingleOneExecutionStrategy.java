package com.github.anrimian.musicplayer.ui.utils.moxy;

import java.util.Iterator;
import java.util.List;

import moxy.MvpView;
import moxy.viewstate.ViewCommand;
import moxy.viewstate.strategy.StateStrategy;

/**
 * Same as OneExecutionStrategy, but with replace previous calls
 */
public class SingleOneExecutionStrategy implements StateStrategy {

    @Override
    public <View extends MvpView> void beforeApply(final List<ViewCommand<View>> currentState,
                                                   final ViewCommand<View> incomingCommand) {
        Iterator<ViewCommand<View>> iterator = currentState.iterator();

        while (iterator.hasNext()) {
            ViewCommand<View> entry = iterator.next();

            if (entry.getClass() == incomingCommand.getClass()) {
                iterator.remove();
                break;
            }
        }

        currentState.add(incomingCommand);
    }

    @Override
    public <View extends MvpView> void afterApply(final List<ViewCommand<View>> currentState,
                                                  final ViewCommand<View> incomingCommand) {
        currentState.remove(incomingCommand);
    }
}
