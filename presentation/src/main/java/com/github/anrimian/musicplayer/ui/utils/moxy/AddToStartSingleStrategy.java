package com.github.anrimian.musicplayer.ui.utils.moxy;

import com.arellomobile.mvp.MvpView;
import com.arellomobile.mvp.viewstate.ViewCommand;
import com.arellomobile.mvp.viewstate.strategy.StateStrategy;

import java.util.Iterator;
import java.util.List;

public class AddToStartSingleStrategy implements StateStrategy {
	@Override
	public <View extends MvpView> void beforeApply(List<ViewCommand<View>> currentState, ViewCommand<View> incomingCommand) {
		Iterator<ViewCommand<View>> iterator = currentState.iterator();

		while (iterator.hasNext()) {
			ViewCommand<View> entry = iterator.next();

			if (entry.getClass() == incomingCommand.getClass()) {
				iterator.remove();
				break;
			}
		}

		currentState.add(0, incomingCommand);
	}

	@Override
	public <View extends MvpView> void afterApply(List<ViewCommand<View>> currentState, ViewCommand<View> incomingCommand) {
		// pass
	}
}
