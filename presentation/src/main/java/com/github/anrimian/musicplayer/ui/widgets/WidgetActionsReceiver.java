package com.github.anrimian.musicplayer.ui.widgets;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.github.anrimian.musicplayer.di.Components;
import com.github.anrimian.musicplayer.domain.business.player.MusicPlayerInteractor;

import static com.github.anrimian.musicplayer.Constants.Actions.PAUSE;
import static com.github.anrimian.musicplayer.Constants.Actions.PLAY;
import static com.github.anrimian.musicplayer.Constants.Actions.SKIP_TO_NEXT;
import static com.github.anrimian.musicplayer.Constants.Actions.SKIP_TO_PREVIOUS;
import static com.github.anrimian.musicplayer.infrastructure.service.music.MusicService.REQUEST_CODE;


public class WidgetActionsReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        MusicPlayerInteractor interactor = Components.getAppComponent().musicPlayerInteractor();

        int action = intent.getIntExtra(REQUEST_CODE, 0);

        if (action == 0) {
            return;
        }
        switch (action) {
            case SKIP_TO_PREVIOUS: {
                interactor.skipToPrevious();
                break;
            }
            case SKIP_TO_NEXT: {
                interactor.skipToNext();
                break;
            }
            case PAUSE:
            case PLAY: {
                interactor.playOrPause();
                break;
            }
        }

    }
}
