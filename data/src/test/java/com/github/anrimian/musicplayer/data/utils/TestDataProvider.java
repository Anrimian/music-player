package com.github.anrimian.musicplayer.data.utils;

import com.github.anrimian.musicplayer.data.database.entities.play_queue.PlayQueueEntity;
import com.github.anrimian.musicplayer.data.models.StoragePlayList;
import com.github.anrimian.musicplayer.data.storage.providers.playlists.StoragePlayListItem;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.composition.PlayQueueEvent;
import com.github.anrimian.musicplayer.domain.models.composition.PlayQueueItem;
import com.github.anrimian.musicplayer.domain.models.composition.folders.FileSource;
import com.github.anrimian.musicplayer.domain.models.composition.folders.Folder;
import com.github.anrimian.musicplayer.domain.models.playlist.PlayListItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.Single;

import static java.util.Arrays.asList;

/**
 * Created on 16.04.2018.
 */
public class TestDataProvider {

    public static List<Composition> getFakeCompositions() {
        List<Composition> compositions = new ArrayList<>();
        for (int i = 0; i < 100000; i++) {
            Composition composition = new Composition();

            composition.setFilePath("music-" + i);
            composition.setId(i);
            compositions.add(composition);
        }
        return compositions;
    }

    public static List<StoragePlayListItem> getFakeStoragePlayListItems() {
        List<StoragePlayListItem> items = new ArrayList<>();
        for (int i = 0; i < 100000; i++) {
            Composition composition = new Composition();

            composition.setFilePath("music-" + i);
            composition.setId(i);

            StoragePlayListItem item = new StoragePlayListItem(i, i);
            items.add(item);
        }
        return items;
    }

    public static List<PlayListItem> getFakePlayListItems() {
        List<PlayListItem> items = new ArrayList<>();
        for (int i = 0; i < 100000; i++) {
            Composition composition = new Composition();

            composition.setFilePath("music-" + i);
            composition.setId(i);

            PlayListItem item = new PlayListItem(i, composition);
            items.add(item);
        }
        return items;
    }

    public static List<PlayQueueItem> getFakeItems() {
        List<PlayQueueItem> items = new ArrayList<>();
        for (int i = 0; i < 100000; i++) {
            Composition composition = new Composition();

            composition.setFilePath("music-" + i);
            composition.setId(i);

            PlayQueueItem item = new PlayQueueItem(i, composition);
            items.add(item);
        }
        return items;
    }

    public static List<PlayQueueItem> getReversedFakeItems() {
        List<PlayQueueItem> items = getFakeItems();
        Collections.reverse(items);
        return items;
    }

    public static Composition fakeComposition(int index) {
        return getFakeCompositions().get(index);
    }

    public static StoragePlayListItem fakeStoragePlayListItem(int index) {
        return getFakeStoragePlayListItems().get(index);
    }

    public static PlayListItem fakePlayListItem(int index) {
        return getFakePlayListItems().get(index);
    }


    public static PlayQueueItem fakeItem(int index) {
        return getFakeItems().get(index);
    }

    public static Map<Long, Composition> getFakeCompositionsMap() {
        Map<Long, Composition> compositions = new HashMap<>();
        for (long i = 0; i < 100000; i++) {
            Composition composition = new Composition();

            composition.setFilePath("music-" + i);
            composition.setId(i);
            compositions.put(i, composition);
        }
        return compositions;
    }

    public static PlayQueueEvent currentItem(int pos) {
        return new PlayQueueEvent(new PlayQueueItem(pos, fakeComposition(pos)), 0L);
    }

    public static PlayQueueEvent currentItem(int itemId, int compositionId) {
        return new PlayQueueEvent(new PlayQueueItem(itemId, fakeComposition(compositionId)), 0L);
    }

    public static Composition fakeComposition(long id, String filePath, long createDate) {
        Composition composition = new Composition();
        composition.setId(id);
        composition.setFilePath(filePath);
        composition.setDateAdded(new Date(createDate));
        return composition;
    }

    public static Composition fakeComposition(long id, String name) {
        Composition composition = new Composition();
        composition.setId(id);
        composition.setDisplayName(name);
        composition.setFilePath(name);
        return composition;
    }

    public static Composition fakeComposition(long id, long createDate) {
        Composition composition = new Composition();
        composition.setId(id);
        composition.setFilePath(String.valueOf(id));
        composition.setDateAdded(new Date(createDate * 1000L));
        return composition;
    }

    public static Single<Folder> getTestFolderSingle(FileSource... fileSources) {
        return Single.just(getTestFolder(fileSources));
    }

    public static Folder getTestFolder(FileSource... fileSources) {
        return new Folder(Observable.create(emitter -> emitter.onNext(asList(fileSources))),
                Observable.never(),
                Observable.never());
    }

    public static StoragePlayList storagePlayList(long i) {
        return new StoragePlayList(i, "test" + i, new Date(i), new Date(i));
    }

    public static List<StoragePlayList> storagePlayLists(long count) {
        List<StoragePlayList> compositions = new ArrayList<>((int) count);
        for (long i = 0; i < count; i++) {
            compositions.add(storagePlayList(i));
        }
        return compositions;
    }

    public static PlayQueueEntity queueEntity(long id,
                                              long audioId,
                                              int position,
                                              int shuffledPosition) {
        PlayQueueEntity entity = new PlayQueueEntity();
        entity.setId(id);
        entity.setAudioId(audioId);
        entity.setPosition(position);
        entity.setShuffledPosition(shuffledPosition);
        return entity;
    }
}
