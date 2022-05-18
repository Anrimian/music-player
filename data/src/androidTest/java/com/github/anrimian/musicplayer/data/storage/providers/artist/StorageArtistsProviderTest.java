package com.github.anrimian.musicplayer.data.storage.providers.artist;

import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;

import android.content.Context;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class StorageArtistsProviderTest {

    private StorageArtistsProvider artistsProvider;

    @BeforeEach
    public void setUp() {
        Context appContext = getInstrumentation().getTargetContext();
        artistsProvider = new StorageArtistsProvider(appContext);
    }

    @Test
    public void testStorageReturnValues() {
//        LongSparseArray<StorageArtist> map = artistsProvider.getArtists();
//        for(int i = 0, size = map.size(); i < size; i++) {
//            StorageArtist item = map.valueAt(i);
//            System.out.println(item);
//        }
    }
}