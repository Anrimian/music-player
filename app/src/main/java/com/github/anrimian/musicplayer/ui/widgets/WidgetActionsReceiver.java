package com.github.anrimian.musicplayer.ui.widgets;

import static com.github.anrimian.musicplayer.Constants.Actions.CHANGE_REPEAT_MODE;
import static com.github.anrimian.musicplayer.Constants.Actions.CHANGE_SHUFFLE_NODE;
import static com.github.anrimian.musicplayer.Constants.Actions.FAST_FORWARD;
import static com.github.anrimian.musicplayer.Constants.Actions.PAUSE;
import static com.github.anrimian.musicplayer.Constants.Actions.PLAY;
import static com.github.anrimian.musicplayer.Constants.Actions.REWIND;
import static com.github.anrimian.musicplayer.Constants.Actions.SKIP_TO_NEXT;
import static com.github.anrimian.musicplayer.Constants.Actions.SKIP_TO_PREVIOUS;
import static com.github.anrimian.musicplayer.infrastructure.service.music.MusicService.REQUEST_CODE;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.github.anrimian.musicplayer.R;
import com.github.anrimian.musicplayer.data.utils.Permissions;
import com.github.anrimian.musicplayer.di.Components;
import com.github.anrimian.musicplayer.di.app.AppComponent;
import com.github.anrimian.musicplayer.domain.interactors.player.LibraryPlayerInteractor;
import com.github.anrimian.musicplayer.infrastructure.service.SystemServiceControllerImpl;


public class WidgetActionsReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        AppComponent appComponent = Components.getAppComponent();
        if (!Permissions.hasFilePermission(context)) {
            appComponent.notificationsDisplayer().showErrorNotification(R.string.no_file_permission);
            return;
        }

        int action = intent.getIntExtra(REQUEST_CODE, 0);

        if (action == 0) {
            return;
        }

        LibraryPlayerInteractor interactor = appComponent.libraryPlayerInteractor();
        switch (action) {
            case SKIP_TO_PREVIOUS: {
                interactor.skipToPrevious();
                break;
            }
            case SKIP_TO_NEXT: {
                interactor.skipToNext();
                break;
            }
            case PAUSE: {
                interactor.pause();
                break;
            }
            case PLAY: {
                SystemServiceControllerImpl.startPlayForegroundService(context);
                break;
            }
            case CHANGE_REPEAT_MODE: {
                interactor.changeRepeatMode();
                break;
            }
            case CHANGE_SHUFFLE_NODE: {
                interactor.setRandomPlayingEnabled(!interactor.isRandomPlayingEnabled());
                break;
            }
            case REWIND: {
                interactor.fastSeekBackward();
                break;
            }
            case FAST_FORWARD: {
                interactor.fastSeekForward();
                break;
            }
        }
    }
}
