package com.github.anrimian.simplemusicplayer.data.storage;

import android.Manifest;
import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.GrantPermissionRule;
import android.util.Log;

import com.github.anrimian.simplemusicplayer.domain.models.playlist.PlayList;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.List;

import io.reactivex.observers.TestObserver;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static junit.framework.Assert.assertNotNull;

public class StoragePlayListsProviderTest {

    @Rule
    public GrantPermissionRule permissionRule = GrantPermissionRule.grant(Manifest.permission.READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE);

    private StoragePlayListsProvider storagePlayListsProvider;

    @Before
    public void setUp() {
        Context appContext = InstrumentationRegistry.getTargetContext();
        storagePlayListsProvider = new StoragePlayListsProvider(appContext,
                new StorageMusicProvider(appContext));
    }

    @Test
    public void getPlayLists() {
        List<PlayList> playLists = storagePlayListsProvider.getPlayLists();
        assertNotNull(playLists);
    }

    @Test
    public void createAndDeletePlayListTest() {
        TestObserver<List<PlayList>> playListsObserver = storagePlayListsProvider.getChangeObservable()
                .test();

        storagePlayListsProvider.createPlayList("test playlist4");

        for (PlayList playList: storagePlayListsProvider.getPlayLists()) {
            if (playList.getName().equals("test playlist4")) {
                storagePlayListsProvider.deletePlayList(playList.getId());
            }
        }

        playListsObserver.assertValueCount(1);
    }
}