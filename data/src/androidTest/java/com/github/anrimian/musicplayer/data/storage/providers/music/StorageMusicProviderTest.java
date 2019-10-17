package com.github.anrimian.musicplayer.data.storage.providers.music;

import android.Manifest;
import android.content.Context;
import android.util.Log;

import androidx.test.rule.GrantPermissionRule;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.Map;

import hu.akarnokd.rxjava2.math.MathObservable;
import io.reactivex.Observable;

import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;

public class StorageMusicProviderTest {

    @Rule
    public GrantPermissionRule permissionRule = GrantPermissionRule.grant(Manifest.permission.READ_EXTERNAL_STORAGE);

    private StorageMusicProvider storageMusicProvider;

    @Before
    public void before() {
        Context appContext = getInstrumentation().getTargetContext();
        storageMusicProvider = new StorageMusicProvider(appContext);
    }

    @Test
    public void testRepositoryReturnValues() {
        Map<Long, StorageComposition> compositions = storageMusicProvider.getCompositions();
        for (StorageComposition composition: compositions.values()) {
            System.out.println(composition);
            Assert.assertNotNull(composition.getFilePath());
        }
    }

    @Test
    public void testLoadingPerformance() {
        Observable<Long> observable = Observable.range(0, 15)
                .map(o -> load())
                .doOnNext(time -> Log.d("TEST_TEST", "load: " + time + " ms"));

        MathObservable.averageDouble(observable)
                .subscribe((Double avg) -> Log.d("TEST_TEST", "average load: " + avg + " ms"));
    }

    private long load() {
        long startTime = System.currentTimeMillis();
        storageMusicProvider.getCompositions();
        long endTime = System.currentTimeMillis();
        return endTime - startTime;
    }
}