package com.github.anrimian.musicplayer.data.repositories.playlists;

import android.Manifest;
import android.content.Context;
import android.util.Log;

import com.github.anrimian.musicplayer.data.models.StoragePlayList;
import com.github.anrimian.musicplayer.data.storage.providers.playlists.StoragePlayListsProvider;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.playlist.PlayListItem;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.List;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.GrantPermissionRule;
import io.reactivex.observers.TestObserver;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

public class StoragePlayListProviderTest {

    @Rule
    public GrantPermissionRule permissionRule = GrantPermissionRule.grant(Manifest.permission.READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE);

    private StoragePlayListsProvider storagePlayListsProvider;

    @Before
    public void setUp() {
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        storagePlayListsProvider = new StoragePlayListsProvider(appContext);
    }

    @Test
    public void getPlayLists() {
        List<StoragePlayList> playLists = storagePlayListsProvider.getPlayLists();
        assertNotNull(playLists);
    }

    @Test
    public void createAndDeletePlayListTest() {
        TestObserver<List<StoragePlayList>> playListsObserver = storagePlayListsProvider.getChangeObservable()
                .test();

        StoragePlayList createdPlayList = storagePlayListsProvider.createPlayList("test playlist10");
        assertEquals("test playlist10", createdPlayList.getName());

        for (StoragePlayList playList: storagePlayListsProvider.getPlayLists()) {
            if (playList.getName().equals("test playlist10")) {
                storagePlayListsProvider.deletePlayList(playList.getId());
            }
        }

        playListsObserver.assertValueCount(1);
    }

    @Test
    public void addCompositionToPlayListTest() {
        Composition composition = findComposition(0);
        Log.d("KEK", "composition: " + composition);

        storagePlayListsProvider.createPlayList("test playlist6");

        StoragePlayList playList = getPlayList("test playlist6");
        try {
            storagePlayListsProvider.addCompositionToPlayList(composition.getId(),
                    playList.getId(),
                    0);

            List<PlayListItem> items = storagePlayListsProvider.getPlayListItems(playList.getId());
            assertEquals(1, items.size());
            assertEquals(composition, items.get(0).getComposition());

            storagePlayListsProvider.deleteItemFromPlayList(items.get(0).getItemId(), playList.getId());

            List<PlayListItem> deletedItems = storagePlayListsProvider.getPlayListItems(playList.getId());
            assertEquals(0, deletedItems.size());
        } finally {
            storagePlayListsProvider.deletePlayList(playList.getId());
        }
    }

    @Test
    public void moveItemInPlayListTest() {
        Composition compositionOne = findComposition(0);
        Composition compositionTwo = findComposition(1);
        Composition compositionThree = findComposition(2);

        storagePlayListsProvider.createPlayList("test playlist7");

        StoragePlayList playList = getPlayList("test playlist7");

        try {
            storagePlayListsProvider.addCompositionToPlayList(compositionOne.getId(),
                    playList.getId(),
                    0);

            storagePlayListsProvider.addCompositionToPlayList(compositionTwo.getId(),
                    playList.getId(),
                    1);

            storagePlayListsProvider.addCompositionToPlayList(compositionThree.getId(),
                    playList.getId(),
                    2);

            List<PlayListItem> items = storagePlayListsProvider.getPlayListItems(playList.getId());
            assertEquals(compositionOne, items.get(0).getComposition());
            assertEquals(compositionTwo, items.get(1).getComposition());
            assertEquals(compositionThree, items.get(2).getComposition());

            storagePlayListsProvider.moveItemInPlayList(playList.getId(), 2, 0);

            List<PlayListItem> movedItems = storagePlayListsProvider.getPlayListItems(playList.getId());
            assertEquals(compositionThree, movedItems.get(0).getComposition());
            assertEquals(compositionOne, movedItems.get(1).getComposition());
            assertEquals(compositionTwo, movedItems.get(2).getComposition());
        } finally {
            storagePlayListsProvider.deletePlayList(playList.getId());
        }
    }

    private StoragePlayList getPlayList(String name) {
        for (StoragePlayList playList: storagePlayListsProvider.getPlayLists()) {
            if (playList.getName().equals(name)) {
                return playList;
            }
        }
        throw new IllegalStateException("play list not found, name: " + name);
    }

    private Composition findComposition(int index) {
        for (StoragePlayList playList: storagePlayListsProvider.getPlayLists()) {
            List<PlayListItem> items = storagePlayListsProvider.getPlayListItems(playList.getId());

            if (index < items.size()) {
                return items.get(index).getComposition();
            }
        }
        throw new IllegalStateException("composition not found");
    }
}