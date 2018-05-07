package com.github.anrimian.simplemusicplayer.data.utils.rx.content_observer;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.util.Log;

import com.github.anrimian.simplemusicplayer.data.storage.StorageMusicDataSource;
import com.github.anrimian.simplemusicplayer.domain.models.composition.Composition;
import com.github.anrimian.simplemusicplayer.domain.utils.changes.Change;

import io.reactivex.Emitter;
import io.reactivex.Observable;

public class RxContentObserver {

    public static Observable<Object> getObservable(Context context,
                                                   @NonNull Uri uri) {
        return getObservable(context, uri, false);
    }

    public static Observable<Object> getObservable(Context context,
                                                   @NonNull Uri uri,
                                                   boolean notifyForDescendants) {
        return Observable.create(emitter -> {
            ContentObserver contentObserver = new EmitterContentObserver(emitter);
            ContentResolver contentResolver = context.getContentResolver();
            contentResolver.registerContentObserver(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    false,
                    contentObserver);
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
            return true;
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            changeEmitter.onNext(new Object());
            Log.d("KEK", "onChange: " + selfChange);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            super.onChange(selfChange, uri);
            Log.d("KEK", "onChange: " + selfChange + ", uri: " + uri);
        }

    }
}
