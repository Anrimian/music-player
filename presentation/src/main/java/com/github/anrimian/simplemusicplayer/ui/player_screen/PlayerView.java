package com.github.anrimian.simplemusicplayer.ui.player_screen;

import com.arellomobile.mvp.MvpView;
import com.arellomobile.mvp.viewstate.strategy.AddToEndSingleStrategy;
import com.arellomobile.mvp.viewstate.strategy.OneExecutionStateStrategy;
import com.arellomobile.mvp.viewstate.strategy.SkipStrategy;
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType;
import com.github.anrimian.simplemusicplayer.domain.models.composition.Composition;
import com.github.anrimian.simplemusicplayer.domain.models.playlist.PlayList;
import com.github.anrimian.simplemusicplayer.ui.common.error.ErrorCommand;
import com.github.anrimian.simplemusicplayer.ui.utils.moxy.SingleStateByTagStrategy;

import java.util.List;

/**
 * Created on 02.11.2017.
 */

public interface PlayerView extends MvpView {

    String PLAYER_STATE = "player_state";

    @StateStrategyType(value = SingleStateByTagStrategy.class, tag = PLAYER_STATE)
    void showStopState();

    @StateStrategyType(value = SingleStateByTagStrategy.class, tag = PLAYER_STATE)
    void showPlayState();

    @StateStrategyType(AddToEndSingleStrategy.class)
    void showMusicControls(boolean show);

    @StateStrategyType(AddToEndSingleStrategy.class)
    void showCurrentComposition(Composition composition, int position);

    @StateStrategyType(AddToEndSingleStrategy.class)
    void bindPlayList(List<Composition> currentPlayList);

    @StateStrategyType(SkipStrategy.class)
    void updatePlayQueue(List<Composition> currentPlayList, List<Composition> newPlayList);

    @StateStrategyType(AddToEndSingleStrategy.class)
    void showInfinitePlayingButton(boolean active);

    @StateStrategyType(AddToEndSingleStrategy.class)
    void showRandomPlayingButton(boolean active);

    @StateStrategyType(AddToEndSingleStrategy.class)
    void showTrackState(long currentPosition, long duration);

    @StateStrategyType(OneExecutionStateStrategy.class)
    void showShareMusicDialog(String filePath);

    @StateStrategyType(OneExecutionStateStrategy.class)
    void showAddingToPlayListError(ErrorCommand errorCommand);

    @StateStrategyType(OneExecutionStateStrategy.class)
    void showAddingToPlayListComplete(PlayList playList, Composition composition);
}
