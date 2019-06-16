package com.github.anrimian.musicplayer.ui.widgets;

import android.content.Context;
import android.content.Intent;

import com.github.anrimian.musicplayer.domain.business.player.MusicPlayerInteractor;
import com.github.anrimian.musicplayer.domain.models.composition.PlayQueueEvent;
import com.github.anrimian.musicplayer.domain.models.composition.PlayQueueItem;
import com.github.anrimian.musicplayer.domain.models.player.PlayerState;

import java.util.List;

import io.reactivex.disposables.CompositeDisposable;

import static com.github.anrimian.musicplayer.Constants.Arguments.COMPOSITION_AUTHOR_ARG;
import static com.github.anrimian.musicplayer.Constants.Arguments.COMPOSITION_NAME_ARG;
import static com.github.anrimian.musicplayer.Constants.Arguments.PLAY_ARG;
import static com.github.anrimian.musicplayer.Constants.Arguments.QUEUE_SIZE_ARG;
import static com.github.anrimian.musicplayer.domain.models.composition.CompositionModelHelper.formatCompositionName;
import static com.github.anrimian.musicplayer.ui.common.format.FormatUtils.formatCompositionAuthor;

public class WidgetUpdater {

    public static final String ACTION_UPDATE_COMPOSITION = "action_update_composition";
    public static final String ACTION_UPDATE_QUEUE = "action_update_queue";
    public static final String ACTION_UPDATE_PLAY_STATE = "action_update_play_state";

    private final Context context;
    private final MusicPlayerInteractor musicPlayerInteractor;

    private final CompositeDisposable updateDisposable = new CompositeDisposable();


    //FIXME update when no service or activity started
    public WidgetUpdater(Context context, MusicPlayerInteractor musicPlayerInteractor) {
        this.context = context;
        this.musicPlayerInteractor = musicPlayerInteractor;
    }

    public void start() {
        if (updateDisposable.size() > 0) {
            return;
        }
        updateDisposable.add(musicPlayerInteractor
                .getCurrentCompositionObservable()
                .subscribe(this::onCurrentCompositionReceived));

        updateDisposable.add(musicPlayerInteractor
                .getPlayQueueObservable()
                .subscribe(this::onPlayQueueReceived));

        updateDisposable.add(musicPlayerInteractor
                .getPlayerStateObservable()
                .subscribe(this::onPlayStateReceived));
    }

    private void onPlayQueueReceived(List<PlayQueueItem> playQueueItems) {
        Intent intent = new Intent(context, WidgetProviderSmall.class);
        intent.setAction(ACTION_UPDATE_QUEUE);
        intent.putExtra(QUEUE_SIZE_ARG, playQueueItems.size());

        WidgetDataHolder.setCurrentQueueSize(context, playQueueItems.size());

        context.sendBroadcast(intent);
    }

    private void onCurrentCompositionReceived(PlayQueueEvent playQueueEvent) {
        String compositionName = null;
        String compositionAuthor = null;
        PlayQueueItem item = playQueueEvent.getPlayQueueItem();
        if (item != null) {
            compositionName = formatCompositionName(item.getComposition());
            compositionAuthor = formatCompositionAuthor(item.getComposition(), context).toString();
        }

        Intent intent = new Intent(context, WidgetProviderSmall.class);
        intent.setAction(ACTION_UPDATE_COMPOSITION);
        intent.putExtra(COMPOSITION_NAME_ARG, compositionName);
        intent.putExtra(COMPOSITION_AUTHOR_ARG, compositionAuthor);

        WidgetDataHolder.setCompositionName(context, compositionName);
        WidgetDataHolder.setCompositionAuthor(context, compositionAuthor);

        context.sendBroadcast(intent);
    }

    private void onPlayStateReceived(PlayerState playerState) {
        Intent intent = new Intent(context, WidgetProviderSmall.class);
        intent.setAction(ACTION_UPDATE_PLAY_STATE);
        intent.putExtra(PLAY_ARG, playerState == PlayerState.PLAY);
        context.sendBroadcast(intent);
    }
}
