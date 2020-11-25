package com.github.anrimian.musicplayer.domain.utils.rx;

import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableSource;
import io.reactivex.rxjava3.functions.Function;

public class FastDebounceFilter<T> implements Function<T, ObservableSource<T>> {
    private static final int DEBOUNCE_MIN_DELAY_MILLIS = 300;

    private final int time;

    private long lastEmitTime;

    public FastDebounceFilter(int time) {
        this.time = time;
    }

    public FastDebounceFilter() {
        this(DEBOUNCE_MIN_DELAY_MILLIS);
    }

    @Override
    public ObservableSource<T> apply(T t) {
        long currentTime = System.currentTimeMillis();
        if (currentTime > lastEmitTime + time) {
            lastEmitTime = currentTime;
            return Observable.just(t);
        }

        //cancel observable, think later
        return Observable.just(t).delay(time, TimeUnit.MILLISECONDS);
    }
}
