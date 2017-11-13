package com.github.anrimian.simplemusicplayer.infrastructure;

import android.content.Context;
import android.content.Intent;

import com.github.anrimian.simplemusicplayer.domain.controllers.MusicServiceController;
import com.github.anrimian.simplemusicplayer.infrastructure.service.MusicService;

/**
 * Created on 10.11.2017.
 */

public class MusicServiceControllerImpl implements MusicServiceController {

    private Context context;

    public MusicServiceControllerImpl(Context context) {
        this.context = context;
    }

    @Override
    public void start() {
        Intent intent = new Intent(context, MusicService.class);
        context.startService(intent);
    }
}
