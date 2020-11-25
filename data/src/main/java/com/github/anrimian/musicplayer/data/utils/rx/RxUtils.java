package com.github.anrimian.musicplayer.data.utils.rx;

import android.annotation.SuppressLint;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.subjects.BehaviorSubject;

public class RxUtils {

    public static Completable share(Completable completable) {
        return completable.toObservable().share().flatMapCompletable(o -> Completable.complete());
    }

    public static boolean isInactive(@Nullable Disposable disposable) {
        return disposable == null || disposable.isDisposed();
    }

    public static boolean isActive(@Nullable Disposable disposable) {
        return disposable != null && !disposable.isDisposed();
    }

    public static void dispose(@Nullable Disposable disposable,
                               @Nonnull CompositeDisposable compositeDisposable) {
        if (disposable != null && !disposable.isDisposed()) {
            compositeDisposable.remove(disposable);
        }
    }

    public static void dispose(@io.reactivex.rxjava3.annotations.Nullable Disposable disposable) {
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
        }
    }

    public static <T> Observable<T> withDefaultValue(BehaviorSubject<T> subject, Creator<T> creator) {
        return Observable.<T>create(emitter -> {
            if (subject.getValue() == null) {
                try {
                    T value = creator.create();
                    if (value != null) {
                        subject.onNext(value);
                    }
                } catch (Exception e) {
                    emitter.onError(e);
                }
            }
        }).mergeWith(subject);
    }

    @SuppressLint("CheckResult")
    public static <T> Observable<T> withDefaultValue(BehaviorSubject<T> subject, Single<T> creator) {
        return Observable.<T>create(emitter -> {
            if (subject.getValue() == null) {
                creator.subscribe(subject::onNext, emitter::onError);
            }
        }).mergeWith(subject);
    }

    @SuppressLint("CheckResult")
    public static <T> Observable<T> withDefaultValue(BehaviorSubject<T> subject, Maybe<T> creator) {
        return Observable.<T>create(emitter -> {
            if (subject.getValue() == null) {
                creator.subscribe(subject::onNext, emitter::onError);
            }
        }).mergeWith(subject);
    }

    public interface Creator<T> {
        T create();
    }
}
