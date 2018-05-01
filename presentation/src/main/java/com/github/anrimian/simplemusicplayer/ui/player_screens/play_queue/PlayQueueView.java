package com.github.anrimian.simplemusicplayer.ui.player_screens.play_queue;

import com.arellomobile.mvp.MvpView;
import com.arellomobile.mvp.viewstate.strategy.AddToEndSingleStrategy;
import com.arellomobile.mvp.viewstate.strategy.SkipStrategy;
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType;
import com.github.anrimian.simplemusicplayer.domain.models.composition.Composition;

import java.util.List;

/**
 * Created on 11.02.2018.
 */

public interface PlayQueueView extends MvpView {

    @StateStrategyType(AddToEndSingleStrategy.class)
    void bindPlayList(List<Composition> currentPlayList);

    @StateStrategyType(SkipStrategy.class)
    void updatePlayList();
}
