package com.github.anrimian.musicplayer.data.utils.rx.content_observer;

import android.content.ContentResolver;
import android.database.ContentObserver;
import android.net.Uri;
import androidx.annotation.NonNull;

import io.reactivex.Emitter;
import io.reactivex.Observable;

import static com.github.anrimian.musicplayer.domain.Constants.TRIGGER;

public class RxContentObserver {

    public static Observable<Object> getObservable(ContentResolver contentResolver,
                                                   @NonNull Uri uri) {
        return getObservable(contentResolver, uri, false);
    }

    public static Observable<Object> getObservable(ContentResolver contentResolver,
                                                   @NonNull Uri uri,
                                                   boolean notifyForDescendants) {
        return Observable.create(emitter -> {
            ContentObserver contentObserver = new EmitterContentObserver(emitter);
            contentResolver.registerContentObserver(uri, notifyForDescendants, contentObserver);
            emitter.setDisposable(new ContentObserverDisposable(contentObserver, contentResolver));
        });
    }

    private static class EmitterContentObserver extends ContentObserver {

        private Emitter<Object> changeEmitter;

        EmitterContentObserver(Emitter<Object> changeEmitter) {
            super(null);
            this.changeEmitter = changeEmitter;
        }

        @Override
        public boolean deliverSelfNotifications() {
            return false;
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            changeEmitter.onNext(TRIGGER);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            super.onChange(selfChange, uri);
        }

    }
}
