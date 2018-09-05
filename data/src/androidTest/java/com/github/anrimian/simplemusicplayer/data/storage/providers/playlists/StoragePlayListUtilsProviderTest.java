package com.github.anrimian.simplemusicplayer.data.storage.providers.playlists;

import android.Manifest;
import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.GrantPermissionRule;
import android.util.Log;

import com.github.anrimian.simplemusicplayer.data.models.StoragePlayList;
import com.github.anrimian.simplemusicplayer.data.storage.providers.music.StorageMusicProvider;
import com.github.anrimian.simplemusicplayer.data.storage.providers.playlists.StoragePlayListsProvider;
import com.github.anrimian.simplemusicplayer.domain.models.composition.Composition;
import com.github.anrimian.simplemusicplayer.domain.models.playlist.PlayList;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.List;

import io.reactivex.observers.TestObserver;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

public class StoragePlayListUtilsProviderTest {

    @Rule
    public GrantPermissionRule permissionRule = GrantPermissionRule.grant(Manifest.permission.READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE);

    private StoragePlayListsProvider storagePlayListsProvider;

    @Before
    public void setUp() {
        Context appContext = InstrumentationRegistry.getTargetContext();
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
            storagePlayListsProvider.addCompositionInPlayList(composition.getId(),
                    playList.getId(),
                    0);

            List<Composition> compositions = storagePlayListsProvider.getCompositions(playList.getId());
            assertEquals(1, compositions.size());
            assert compositions.contains(composition);

            storagePlayListsProvider.deleteCompositionFromPlayList(composition.getId(), playList.getId());

            List<Composition> deletedCompositions = storagePlayListsProvider.getCompositions(playList.getId());
            assertEquals(0, deletedCompositions.size());
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
            storagePlayListsProvider.addCompositionInPlayList(compositionOne.getId(),
                    playList.getId(),
                    0);

            storagePlayListsProvider.addCompositionInPlayList(compositionTwo.getId(),
                    playList.getId(),
                    1);

            storagePlayListsProvider.addCompositionInPlayList(compositionThree.getId(),
                    playList.getId(),
                    2);

            List<Composition> compositions = storagePlayListsProvider.getCompositions(playList.getId());
            assertEquals(compositionOne, compositions.get(0));
            assertEquals(compositionTwo, compositions.get(1));
            assertEquals(compositionThree, compositions.get(2));

            storagePlayListsProvider.moveItemInPlayList(playList.getId(), 2, 0);

            List<Composition> movedCompositions = storagePlayListsProvider.getCompositions(playList.getId());
            assertEquals(compositionOne, movedCompositions.get(1));
            assertEquals(compositionTwo, movedCompositions.get(2));
            assertEquals(compositionThree, movedCompositions.get(0));
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
            List<Composition> compositions = storagePlayListsProvider.getCompositions(playList.getId());

            if (index < compositions.size()) {
                return compositions.get(index);
            }
        }
        throw new IllegalStateException("composition not found");
    }
}