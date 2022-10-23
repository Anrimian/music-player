package com.github.anrimian.musicplayer.domain.utils;


import static com.github.anrimian.musicplayer.domain.utils.rx.RxUtils.withDefaultValue;

import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.observers.TestObserver;
import io.reactivex.rxjava3.schedulers.TestScheduler;
import io.reactivex.rxjava3.subjects.BehaviorSubject;

/**
 * Created on 02.06.2018.
 */
public class RxUtilsTest {

    private final TestScheduler scheduler = new TestScheduler();

    @Test
    public void withDefaultValueTest() {
        Single<Integer> single = Single.just(1).delay(1, TimeUnit.SECONDS, scheduler);

        BehaviorSubject<Integer> subject = BehaviorSubject.create();

        TestObserver<Integer> testObserver = withDefaultValue(subject, single)
                .timeout(1500, TimeUnit.MILLISECONDS)
                .test();

        testObserver.assertNoValues();
        scheduler.advanceTimeBy(500, TimeUnit.MILLISECONDS);
        testObserver.assertNoValues();
        scheduler.advanceTimeBy(1000, TimeUnit.MILLISECONDS);
        testObserver.assertValue(1);
    }
}