package com.github.anrimian.simplemusicplayer.data.storage;

import android.Manifest;
import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.GrantPermissionRule;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class StoragePlayListsProviderTest {

    @Rule
    public GrantPermissionRule permissionRule = GrantPermissionRule.grant(Manifest.permission.READ_EXTERNAL_STORAGE);

    private StoragePlayListsProvider storagePlayListsProvider;

    @Before
    public void setUp() {
        Context appContext = InstrumentationRegistry.getTargetContext();
        storagePlayListsProvider = new StoragePlayListsProvider(appContext,
                new StorageMusicProvider(appContext));
    }

    @Test
    public void getPlayLists() {
        storagePlayListsProvider.getPlayLists();
    }
}