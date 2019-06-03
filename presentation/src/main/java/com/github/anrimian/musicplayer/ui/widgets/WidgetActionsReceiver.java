package com.github.anrimian.musicplayer.ui.widgets;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.github.anrimian.musicplayer.di.Components;
import com.github.anrimian.musicplayer.domain.business.player.MusicPlayerInteractor;

import static com.github.anrimian.musicplayer.Constants.Actions.SKIP_TO_NEXT;
import static com.github.anrimian.musicplayer.Constants.Actions.SKIP_TO_PREVIOUS;


public class WidgetActionsReceiver extends BroadcastReceiver {

    public static final String WIDGET_ACTION = "widget_action";

    @Override
    public void onReceive(Context context, Intent intent) {
        MusicPlayerInteractor interactor = Components.getAppComponent().musicPlayerInteractor();

        int action = intent.getIntExtra(WIDGET_ACTION, 0);
        Log.d("KEK", "onReceive, action: " + action);

        if (action == 0) {
            return;
        }
        switch (action) {
            case SKIP_TO_NEXT: {
                interactor.skipToNext();
                break;
            }
            case SKIP_TO_PREVIOUS: {
                interactor.skipToPrevious();
                break;
            }
        }

    }
}
