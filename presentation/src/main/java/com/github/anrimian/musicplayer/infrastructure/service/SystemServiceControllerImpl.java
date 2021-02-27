package com.github.anrimian.musicplayer.infrastructure.service;

import android.content.Context;
import android.content.Intent;

import com.github.anrimian.musicplayer.domain.controllers.SystemServiceController;
import com.github.anrimian.musicplayer.infrastructure.service.music.MusicService;

public class SystemServiceControllerImpl implements SystemServiceController {

    private final Context context;

    public SystemServiceControllerImpl(Context context) {
        this.context = context;
    }

    @Override
    public void startMusicService() {
        Intent intent = new Intent(context, MusicService.class);
        intent.putExtra(MusicService.START_FOREGROUND_SIGNAL, 1);
        context.startService(intent);
    }
}
