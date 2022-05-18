package com.github.anrimian.musicplayer.data.storage.providers.albums;

import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;

import android.content.Context;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class StorageAlbumsProviderTest {

    private StorageAlbumsProvider albumsProvider;

    @BeforeEach
    public void setUp() {
        Context appContext = getInstrumentation().getTargetContext();
        albumsProvider = new StorageAlbumsProvider(appContext);
    }

    @Test
    public void testStorageReturnValues() {
//        LongSparseArray<StorageAlbum> map = albumsProvider.getAlbums();
//        for(int i = 0, size = map.size(); i < size; i++) {
//            StorageAlbum item = map.valueAt(i);
//            Log.i("TEST_STORAGE", "item: " + item);
//            System.out.println(item);
//        }
    }
}