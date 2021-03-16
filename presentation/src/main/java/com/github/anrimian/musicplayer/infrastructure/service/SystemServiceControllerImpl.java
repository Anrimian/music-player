package com.github.anrimian.musicplayer.infrastructure.service;

import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.github.anrimian.musicplayer.Constants;
import com.github.anrimian.musicplayer.domain.controllers.SystemServiceController;
import com.github.anrimian.musicplayer.infrastructure.service.music.MusicService;

public class SystemServiceControllerImpl implements SystemServiceController {

    private final Context context;

    public static void startPlayForegroundService(Context context) {
        startPlayForegroundService(context, 0);
    }

    public static void startPlayForegroundService(Context context, int playDelay) {
        Intent intent = new Intent(context, MusicService.class);
        intent.putExtra(MusicService.START_FOREGROUND_SIGNAL, 1);
        intent.putExtra(MusicService.REQUEST_CODE, Constants.Actions.PLAY);
        intent.putExtra(MusicService.PLAY_DELAY_MILLIS, playDelay);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent);
        } else {
            context.startService(intent);
        }
    }

    public SystemServiceControllerImpl(Context context) {
        this.context = context;
    }

    @Override
    public void startMusicService() {
        Intent intent = new Intent(context, MusicService.class);
        intent.putExtra(MusicService.START_FOREGROUND_SIGNAL, 1);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent);
        } else {
            context.startService(intent);
        }
    }
}
