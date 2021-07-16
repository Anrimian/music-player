package com.github.anrimian.musicplayer.data.utils.rx.content_observer;

import android.content.ContentResolver;
import android.database.ContentObserver;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.disposables.Disposable;

/**
 * Created on 05.11.2017.
 */

public class ContentObserverDisposable implements Disposable {

    private final ContentObserver contentObserver;
    private final ContentResolver contentResolver;

    private boolean isDisposed = false;

    public ContentObserverDisposable(@NonNull ContentObserver contentObserver,
                                     @NonNull ContentResolver contentResolver) {
        this.contentObserver = contentObserver;
        this.contentResolver = contentResolver;
    }

    @Override
    public void dispose() {
        if (!isDisposed) {
            contentResolver.unregisterContentObserver(contentObserver);
            isDisposed = true;
        }
    }

    @Override
    public boolean isDisposed() {
        return isDisposed;
    }
}
