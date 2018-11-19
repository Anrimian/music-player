package com.github.anrimian.musicplayer.data.storage.providers.music;

import android.Manifest;
import android.content.Context;

import com.github.anrimian.musicplayer.domain.models.composition.Composition;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.Map;

import androidx.test.InstrumentationRegistry;
import androidx.test.rule.GrantPermissionRule;

import static androidx.test.platform.app.InstrumentationRegistry.*;

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
        Map<Long, Composition> compositions = storageMusicProvider.getCompositions();
        for (Composition composition: compositions.values()) {
            System.out.println(composition);
            Assert.assertNotNull(composition.getFilePath());
        }
    }

}