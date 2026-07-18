package com.github.anrimian.musicplayer.data.storage.providers.music;

import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import android.content.Context;
import android.util.Log;

import com.github.anrimian.musicplayer.domain.Constants;
import com.github.anrimian.musicplayer.domain.interactors.analytics.NoOpAnalytics;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import hu.akarnokd.rxjava3.math.MathObservable;
import io.reactivex.rxjava3.core.Observable;

public class StorageMusicProviderTest {

    private SystemAudioCatalogProvider storageMusicProvider;

    @BeforeEach
    public void before() {
        Context appContext = getInstrumentation().getTargetContext();
        storageMusicProvider = new SystemAudioCatalogProvider(appContext, NoOpAnalytics.INSTANCE);
    }

    @Test
    public void testRepositoryReturnValues() {
        Map<AudioFileKey, StorageAudioFile> map = storageMusicProvider.getAudioFiles(0, false, Constants.DEFAULT_REMOTE_EXTENSIONS);
        if (map == null) {
            map = new HashMap<>();
        }
        for(var composition : map.values()) {
            System.out.println(composition);
            assertNotNull(composition.getParentPath());
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
        Map<AudioFileKey, StorageAudioFile> map = storageMusicProvider.getAudioFiles(0, false, Constants.DEFAULT_REMOTE_EXTENSIONS);
        if (map == null) {
            Log.d("KEK", "load failed");
        }
        long endTime = System.currentTimeMillis();
        return endTime - startTime;
    }
}