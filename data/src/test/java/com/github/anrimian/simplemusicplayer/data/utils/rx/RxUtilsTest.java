package com.github.anrimian.simplemusicplayer.data.utils.rx;

import org.junit.Test;

import java.util.concurrent.TimeUnit;

import io.reactivex.Single;
import io.reactivex.observers.TestObserver;
import io.reactivex.schedulers.TestScheduler;
import io.reactivex.subjects.BehaviorSubject;

import static com.github.anrimian.simplemusicplayer.data.utils.rx.RxUtils.withDefaultValue;

/**
 * Created on 02.06.2018.
 */
public class RxUtilsTest {

    private TestScheduler scheduler = new TestScheduler();

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