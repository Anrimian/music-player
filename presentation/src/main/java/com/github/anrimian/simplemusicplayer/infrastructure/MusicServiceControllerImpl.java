package com.github.anrimian.simplemusicplayer.infrastructure;

import android.content.Context;
import android.content.Intent;

import com.github.anrimian.simplemusicplayer.domain.controllers.MusicServiceController;
import com.github.anrimian.simplemusicplayer.domain.models.Composition;
import com.github.anrimian.simplemusicplayer.domain.models.player.PlayerState;
import com.github.anrimian.simplemusicplayer.infrastructure.service.MusicService;
import com.github.anrimian.simplemusicplayer.infrastructure.service.MusicServiceBinder;
import com.github.anrimian.simplemusicplayer.utils.services.BoundServiceWrapper;

import java.util.List;

import io.reactivex.Observable;

/**
 * Created on 10.11.2017.
 */

public class MusicServiceControllerImpl implements MusicServiceController {

    private Context context;

    private BoundServiceWrapper<MusicServiceBinder> musicServiceBinderWrapper;

    public MusicServiceControllerImpl(Context context) {
        this.context = context;
        musicServiceBinderWrapper = new BoundServiceWrapper<>(context, MusicService.class, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void start() {
        Intent intent = new Intent(context, MusicService.class);
        context.startService(intent);
    }

    @Override
    public void startPlaying(List<Composition> compositions) {
        musicServiceBinderWrapper.call(binder -> binder.startPlaying(compositions));
    }

    @Override
    public void play() {
        musicServiceBinderWrapper.call(MusicServiceBinder::play);
    }

    @Override
    public void pause() {
        musicServiceBinderWrapper.call(MusicServiceBinder::pause);
    }

    @Override
    public void stop() {
        musicServiceBinderWrapper.call(MusicServiceBinder::stop);
    }

    @Override
    public void skipToPrevious() {
        musicServiceBinderWrapper.call(MusicServiceBinder::skipToPrevious);
    }

    @Override
    public void skipToNext() {
        musicServiceBinderWrapper.call(MusicServiceBinder::skipToNext);
    }

    @Override
    public Observable<PlayerState> getPlayerStateObservable() {
//        return musicServiceBinderWrapper.call(MusicServiceBinder::skipToNext);/TODO finish
        return null;
    }

    @Override
    public Observable<Composition> getCurrentCompositionObservable() {
        return null;
    }

    @Override
    public Observable<List<Composition>> getCurrentPlayListObservable() {
        return null;
    }

    @Override
    public Observable<Long> getTrackPositionObservable() {
        return null;
    }

    @Override
    public boolean isInfinitePlayingEnabled() {
        return false;
    }

    @Override
    public boolean isRandomPlayingEnabled() {
        return false;
    }

    @Override
    public void setRandomPlayingEnabled(boolean enabled) {

    }

    @Override
    public void setInfinitePlayingEnabled(boolean enabled) {

    }
}
