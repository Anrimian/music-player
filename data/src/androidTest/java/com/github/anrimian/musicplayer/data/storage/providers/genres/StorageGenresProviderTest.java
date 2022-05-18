package com.github.anrimian.musicplayer.data.storage.providers.genres;

import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;

import android.content.Context;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class StorageGenresProviderTest {

    private StorageGenresProvider albumsProvider;

    @BeforeEach
    public void setUp() {
        Context appContext = getInstrumentation().getTargetContext();
        albumsProvider = new StorageGenresProvider(appContext);
    }

    @Test
    public void testStorageReturnValues() {
//        LongSparseArray<StorageGenre> map = albumsProvider.getGenres();
//        for(int i = 0, size = map.size(); i < size; i++) {
//            StorageGenre item = map.valueAt(i);
//            LongSparseArray<StorageGenreItem> items = albumsProvider.getGenreItems(item.getId());
//            Log.i("TEST_STORAGE", "item: " + item + ", members: " + items);
//            System.out.println(item);
//        }
    }
}