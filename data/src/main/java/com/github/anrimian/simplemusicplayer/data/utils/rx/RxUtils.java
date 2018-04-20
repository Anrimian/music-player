package com.github.anrimian.simplemusicplayer.data.utils.rx;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;

public class RxUtils {

    public static Completable share(Completable completable) {
        return completable.toObservable().share().flatMapCompletable(o -> Completable.complete());
    }

    public static <T> Observable<T> withDefaultValue(BehaviorSubject<T> subject, Creator<T> creator) {
        return Observable.<T>create(emitter -> {
            if (subject.getValue() == null) {
                T value = creator.create();
                if (value != null) {
                    subject.onNext(value);
                }
            }
        }).mergeWith(subject);
    }

    public interface Creator<T> {
        T create();
    }
}
