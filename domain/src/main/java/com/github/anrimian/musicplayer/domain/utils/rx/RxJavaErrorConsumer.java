package com.github.anrimian.musicplayer.domain.utils.rx;

import io.reactivex.exceptions.UndeliverableException;
import io.reactivex.functions.Consumer;

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
