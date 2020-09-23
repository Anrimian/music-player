package com.github.anrimian.musicplayer.domain.utils.rx;

import io.reactivex.rxjava3.exceptions.UndeliverableException;
import io.reactivex.rxjava3.functions.Consumer;

public class RxJavaErrorConsumer implements Consumer<Throwable> {

    @Override
    public void accept(Throwable e) {
        if (e instanceof UndeliverableException) {
            return;
        }
        Thread.currentThread()
                .getUncaughtExceptionHandler()
                .uncaughtException(Thread.currentThread(), e);
    }
}
