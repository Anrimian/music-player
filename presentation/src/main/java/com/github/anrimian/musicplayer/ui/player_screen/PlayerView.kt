package com.github.anrimian.musicplayer.ui.player_screen;

import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.play_queue.PlayQueueItem;
import com.github.anrimian.musicplayer.domain.models.player.PlayerState;
import com.github.anrimian.musicplayer.domain.models.playlist.PlayList;
import com.github.anrimian.musicplayer.domain.models.scanner.FileScannerState;
import com.github.anrimian.musicplayer.ui.common.error.ErrorCommand;

import java.util.List;

import javax.annotation.Nullable;

import moxy.MvpView;
import moxy.viewstate.strategy.alias.AddToEndSingle;
import moxy.viewstate.strategy.alias.OneExecution;
import moxy.viewstate.strategy.alias.Skip;

/**
 * Created on 02.11.2017.
 */

public interface PlayerView extends MvpView {

    @AddToEndSingle
    void showPlayerState(PlayerState state);

    @AddToEndSingle
    void setButtonPanelState(boolean expanded);

    @AddToEndSingle
    void setMusicControlsEnabled(boolean show);

    @AddToEndSingle
    void showCurrentQueueItem(@Nullable PlayQueueItem item, boolean showCover);

    @OneExecution
    void scrollQueueToPosition(int position);

    @AddToEndSingle
    void updatePlayQueue(List<PlayQueueItem> items);

    @AddToEndSingle
    void showRepeatMode(int mode);

    @AddToEndSingle
    void showRandomPlayingButton(boolean active);

    @AddToEndSingle
    void showTrackState(long currentPosition, long duration);

    @OneExecution
    void showSelectPlayListDialog();

    @OneExecution
    void showShareMusicDialog(Composition composition);

    @OneExecution
    void showAddingToPlayListError(ErrorCommand errorCommand);

    @OneExecution
    void showAddingToPlayListComplete(PlayList playList, List<Composition> compositions);

    @OneExecution
    void showConfirmDeleteDialog(List<Composition> compositionsToDelete);

    @OneExecution
    void showDeleteCompositionError(ErrorCommand errorCommand);

    @OneExecution
    void showDeleteCompositionMessage(List<Composition> compositionsToDelete);

    @AddToEndSingle
    void showPlayQueueSubtitle(int size);

    @OneExecution
    void showDrawerScreen(int selectedDrawerScreen, long selectedPlayListScreen);

    @OneExecution
    void showLibraryScreen(int selectedLibraryScreen);

    @Skip
    void notifyItemMoved(int from, int to);

    @AddToEndSingle
    void setPlayQueueCoversEnabled(boolean isCoversEnabled);

    @Skip
    void startEditCompositionScreen(long id);

    @OneExecution
    void showErrorMessage(ErrorCommand errorCommand);

    @OneExecution
    void showDeletedItemMessage();

    @AddToEndSingle
    void displayPlaybackSpeed(float speed);

    @AddToEndSingle
    void showSpeedChangeFeatureVisible(boolean visible);

    @AddToEndSingle
    void showSleepTimerRemainingTime(long remainingMillis);

    @AddToEndSingle
    void showFileScannerState(FileScannerState state);
}
