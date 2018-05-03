package com.github.anrimian.simplemusicplayer.data.utils.rx.content_observer;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;

import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;

/**
 * Created on 05.11.2017.
 */

public class ContentObserverDisposable implements Disposable {

    private ContentObserver contentObserver;
    private ContentResolver contentResolver;

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
