package com.github.anrimian.simplemusicplayer.infrastructure.service;

import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.github.anrimian.simplemusicplayer.domain.business.player.MusicPlayerInteractor;
import com.github.anrimian.simplemusicplayer.domain.models.player.PlayerState;
import com.github.anrimian.simplemusicplayer.infrastructure.service.music.MusicService;

/**
 * Created on 08.04.2018.
 */

public class ServiceManager {

    private Context context;
    private MusicPlayerInteractor musicPlayerInteractor;

    public ServiceManager(Context context, MusicPlayerInteractor musicPlayerInteractor) {
        this.context = context;
        this.musicPlayerInteractor = musicPlayerInteractor;
        subscribeOnPlayerActions();
    }

    private void subscribeOnPlayerActions() {
        musicPlayerInteractor.getPlayerStateObservable()
                .subscribe(this::onPlayerStateChanged);
    }

    private void onPlayerStateChanged(PlayerState playerState) {
        switch (playerState) {
            case PLAY: {
                Intent intent = new Intent(context, MusicService.class);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent);
                } else {
                    context.startService(intent);
                }
                break;
            }
            default: {
                break;
            }
        }
    }
}
