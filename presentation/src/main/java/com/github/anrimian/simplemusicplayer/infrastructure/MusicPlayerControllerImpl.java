package com.github.anrimian.simplemusicplayer.infrastructure;

import android.content.Context;

import com.github.anrimian.simplemusicplayer.domain.controllers.MusicPlayerController;
import com.github.anrimian.simplemusicplayer.domain.models.Composition;
import com.github.anrimian.simplemusicplayer.infrastructure.service.MusicService;
import com.github.anrimian.simplemusicplayer.infrastructure.service.MusicServiceBinder;
import com.github.anrimian.simplemusicplayer.utils.services.BoundServiceWrapper;

import java.util.List;

/**
 * Created on 03.11.2017.
 */

public class MusicPlayerControllerImpl implements MusicPlayerController {

    private BoundServiceWrapper<MusicServiceBinder> serviceWrapper;

    public MusicPlayerControllerImpl(Context context) {
        serviceWrapper = new BoundServiceWrapper<>(context, MusicService.class, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void play(List<Composition> compositions) {
        serviceWrapper.call(binder -> binder.play(compositions));
    }

    @Override
    public void changePlayState() {
        serviceWrapper.call(MusicServiceBinder::changePlayState);
    }

    @Override
    public void skipToPrevious() {
        serviceWrapper.call(MusicServiceBinder::skipToPrevious);
    }

    @Override
    public void skipToNext() {
        serviceWrapper.call(MusicServiceBinder::skipToNext);
    }
}
