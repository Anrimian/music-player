package com.github.anrimian.simplemusicplayer.data.storage;

import android.Manifest;
import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.GrantPermissionRule;

import com.github.anrimian.simplemusicplayer.domain.models.composition.Composition;


import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.List;
import java.util.Map;

public class StorageMusicProviderTest {

    @Rule
    public GrantPermissionRule permissionRule = GrantPermissionRule.grant(Manifest.permission.READ_EXTERNAL_STORAGE);

    private StorageMusicProvider storageMusicProvider;

    @Before
    public void before() {
        Context appContext = InstrumentationRegistry.getTargetContext();
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