package com.github.anrimian.musicplayer.data.utils.exo_player;

import androidx.annotation.NonNull;

import com.github.anrimian.musicplayer.domain.utils.functions.Callback;
import com.google.android.exoplayer2.PlaybackException;
import com.google.android.exoplayer2.Player;

public class PlayerEventListener implements Player.Listener {

    private final Runnable onEnded;
    private final Callback<PlaybackException> errorCallback;

    public PlayerEventListener(Runnable onEnded, Callback<PlaybackException> errorCallback) {
        this.onEnded = onEnded;
        this.errorCallback = errorCallback;
    }

    @Override
    public void onPlaybackStateChanged(int state) {
        //            case Player.STATE_BUFFERING: {
        //                break;
        //            }
        //            case Player.STATE_IDLE: {
        //                break;
        //            }
        //            case Player.STATE_READY: {
        //                break;
        //            }
        if (state == Player.STATE_ENDED) {
            onEnded.run();
        }
    }

    @Override
    public void onPlayerError(@NonNull PlaybackException error) {
        errorCallback.call(error);
    }
}
