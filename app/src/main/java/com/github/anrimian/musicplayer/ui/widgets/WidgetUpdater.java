package com.github.anrimian.musicplayer.ui.widgets;

import static com.github.anrimian.musicplayer.Constants.Arguments.COMPOSITION_AUTHOR_ARG;
import static com.github.anrimian.musicplayer.Constants.Arguments.COMPOSITION_ID_ARG;
import static com.github.anrimian.musicplayer.Constants.Arguments.COMPOSITION_IS_FILE_EXISTS_ARG;
import static com.github.anrimian.musicplayer.Constants.Arguments.COMPOSITION_NAME_ARG;
import static com.github.anrimian.musicplayer.Constants.Arguments.COMPOSITION_SIZE;
import static com.github.anrimian.musicplayer.Constants.Arguments.COMPOSITION_UPDATE_TIME_ARG;
import static com.github.anrimian.musicplayer.Constants.Arguments.COVERS_ENABLED_ARG;
import static com.github.anrimian.musicplayer.Constants.Arguments.PLAY_ARG;
import static com.github.anrimian.musicplayer.Constants.Arguments.QUEUE_SIZE_ARG;
import static com.github.anrimian.musicplayer.Constants.Arguments.RANDOM_PLAY_ARG;
import static com.github.anrimian.musicplayer.Constants.Arguments.REPEAT_ARG;
import static com.github.anrimian.musicplayer.domain.models.utils.CompositionHelper.formatCompositionName;
import static com.github.anrimian.musicplayer.ui.common.format.FormatUtils.formatCompositionAuthor;

import android.content.Context;
import android.content.Intent;

import com.github.anrimian.musicplayer.data.utils.rx.RxUtilsKt;
import com.github.anrimian.musicplayer.domain.interactors.player.LibraryPlayerInteractor;
import com.github.anrimian.musicplayer.domain.interactors.settings.DisplaySettingsInteractor;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.play_queue.PlayQueueEvent;
import com.github.anrimian.musicplayer.domain.models.play_queue.PlayQueueItem;
import com.github.anrimian.musicplayer.domain.models.player.PlayerState;
import com.github.anrimian.musicplayer.ui.common.format.FormatUtilsKt;

import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.disposables.CompositeDisposable;

public class WidgetUpdater {

    public static final String UPDATE_FROM_INTENT = "update_from_intent";

    private final Context context;
    private final LibraryPlayerInteractor musicPlayerInteractor;
    private final DisplaySettingsInteractor displaySettingsInteractor;
    private final Scheduler scheduler;

    private final CompositeDisposable updateDisposable = new CompositeDisposable();

    private final WidgetState widgetState = new WidgetState();

    public WidgetUpdater(Context context,
                         LibraryPlayerInteractor musicPlayerInteractor,
                         DisplaySettingsInteractor displaySettingsInteractor,
                         Scheduler scheduler) {
        this.context = context;
        this.musicPlayerInteractor = musicPlayerInteractor;
        this.displaySettingsInteractor = displaySettingsInteractor;
        this.scheduler = scheduler;
    }

    public void start() {
        if (updateDisposable.size() > 0) {
            return;
        }
        updateDisposable.add(RxUtilsKt.retryWithDelay(Observable.combineLatest(
                musicPlayerInteractor.getCurrentQueueItemObservable(),
                musicPlayerInteractor.getPlayQueueSizeObservable(),
                musicPlayerInteractor.getIsPlayingStateObservable(),
                musicPlayerInteractor.getPlayerStateObservable(),
                displaySettingsInteractor.getCoversEnabledObservable(),
                musicPlayerInteractor.getRepeatModeObservable(),
                musicPlayerInteractor.getRandomPlayingObservable(),
                widgetState::set)
                .observeOn(scheduler), 10, 10, TimeUnit.SECONDS)
                .subscribe(this::onWidgetStateReceived));
    }

    private void onWidgetStateReceived(WidgetState widgetState) {
        Intent intent = new Intent("android.appwidget.action.APPWIDGET_UPDATE");
        intent.setPackage(context.getPackageName());
        intent.putExtra(UPDATE_FROM_INTENT, true);

        String compositionName = null;
        String compositionAuthor = null;
        long compositionId = 0;
        long compositionUpdateTime = 0;
        long compositionSize = 0;
        boolean isFileExists = false;
        PlayQueueItem item = widgetState.playQueueEvent.getPlayQueueItem();
        if (item != null) {
            Composition composition = item.getComposition();
            compositionName = formatCompositionName(composition);
            compositionAuthor = formatCompositionAuthor(composition, context).toString();
            compositionId = composition.getId();
            compositionUpdateTime = composition.getDateModified().getTime();
            compositionSize = composition.getSize();
            isFileExists = composition.isFileExists();
        }
        WidgetDataHolder.setWidgetInfo(context,
                compositionName,
                compositionAuthor,
                compositionId,
                compositionUpdateTime,
                compositionSize,
                isFileExists,
                widgetState.playQueueSize,
                widgetState.randomPlay,
                widgetState.repeatMode,
                widgetState.isCoversEnabled
        );
        intent.putExtra(COMPOSITION_NAME_ARG, compositionName);
        intent.putExtra(COMPOSITION_AUTHOR_ARG, compositionAuthor);
        intent.putExtra(COMPOSITION_ID_ARG, compositionId);
        intent.putExtra(COMPOSITION_UPDATE_TIME_ARG, compositionUpdateTime);
        intent.putExtra(COMPOSITION_SIZE, compositionSize);
        intent.putExtra(COMPOSITION_IS_FILE_EXISTS_ARG, isFileExists);
        intent.putExtra(QUEUE_SIZE_ARG, widgetState.playQueueSize);
        intent.putExtra(RANDOM_PLAY_ARG, widgetState.randomPlay);
        intent.putExtra(REPEAT_ARG, widgetState.repeatMode);
        intent.putExtra(COVERS_ENABLED_ARG, widgetState.isCoversEnabled);
        int playerState = FormatUtilsKt.getRemoteViewPlayerState(widgetState.isPlaying, widgetState.playerState);
        intent.putExtra(PLAY_ARG, playerState);

        context.sendBroadcast(intent);
    }

    private static class WidgetState {

        PlayQueueEvent playQueueEvent;
        int playQueueSize;
        boolean isPlaying;
        PlayerState playerState;
        boolean isCoversEnabled;
        int repeatMode;
        boolean randomPlay;

        private WidgetState set(PlayQueueEvent playQueueEvent,
                                int playQueueSize,
                                boolean isPlaying,
                                PlayerState playerState,
                                boolean isCoversEnabled,
                                int repeatMode,
                                boolean randomPlay) {
            this.playQueueEvent = playQueueEvent;
            this.playQueueSize = playQueueSize;
            this.isPlaying = isPlaying;
            this.playerState = playerState;
            this.isCoversEnabled = isCoversEnabled;
            this.repeatMode = repeatMode;
            this.randomPlay = randomPlay;
            return this;
        }
    }
}
