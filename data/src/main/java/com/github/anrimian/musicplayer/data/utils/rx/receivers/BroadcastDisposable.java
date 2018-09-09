package com.github.anrimian.musicplayer.data.utils.rx.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;

import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;

/**
 * Created on 05.11.2017.
 */

class BroadcastDisposable implements Disposable {

    private BroadcastReceiver receiver;
    private Context ctx;

    private boolean isDisposed = false;

    BroadcastDisposable(@NonNull BroadcastReceiver receiver, @NonNull Context ctx) {
        this.receiver = receiver;
        this.ctx = ctx;
    }

    @Override
    public void dispose() {
        if (!isDisposed) {
            ctx.unregisterReceiver(receiver);
            isDisposed = true;
        }
    }

    @Override
    public boolean isDisposed() {
        return isDisposed;
    }
}
