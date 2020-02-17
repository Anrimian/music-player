package com.github.anrimian.musicplayer.data.utils;

import androidx.collection.LongSparseArray;

import com.github.anrimian.musicplayer.data.database.entities.play_queue.PlayQueueEntity;
import com.github.anrimian.musicplayer.data.storage.providers.music.StorageComposition;
import com.github.anrimian.musicplayer.data.storage.providers.music.StorageFullComposition;
import com.github.anrimian.musicplayer.data.storage.providers.playlists.StoragePlayList;
import com.github.anrimian.musicplayer.data.storage.providers.playlists.StoragePlayListItem;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.composition.folders.FileSource;
import com.github.anrimian.musicplayer.domain.models.composition.folders.Folder;
import com.github.anrimian.musicplayer.domain.models.play_queue.PlayQueueEvent;
import com.github.anrimian.musicplayer.domain.models.play_queue.PlayQueueItem;
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
            Composition composition = fakeComposition(i, "music-" + i);
            compositions.add(composition);
        }
        return compositions;
    }


    public static List<StoragePlayListItem> getFakeStoragePlayListItems() {
        List<StoragePlayListItem> items = new ArrayList<>();
        for (int i = 0; i < 100000; i++) {
            StoragePlayListItem item = new StoragePlayListItem(i, i);
            items.add(item);
        }
        return items;
    }

    public static List<StoragePlayListItem> getFakeStoragePlayListItems(int count) {
        List<StoragePlayListItem> items = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            StoragePlayListItem item = new StoragePlayListItem(i, i);
            items.add(item);
        }
        return items;
    }

    public static Map<Long, StoragePlayListItem> getFakeStoragePlayListItemsMap() {
        Map<Long, StoragePlayListItem> items = new HashMap<>();
        for (long i = 0; i < 100000; i++) {
            StoragePlayListItem item = new StoragePlayListItem(i, i);
            items.put(i, item);
        }
        return items;
    }

    public static List<PlayListItem> getFakePlayListItems() {
        List<PlayListItem> items = new ArrayList<>();
        for (int i = 0; i < 100000; i++) {
            Composition composition = fakeComposition(i, "music-" + i);
            PlayListItem item = new PlayListItem(i, composition);
            items.add(item);
        }
        return items;
    }

    public static List<PlayQueueItem> getFakeItems() {
        List<PlayQueueItem> items = new ArrayList<>();
        for (int i = 0; i < 100000; i++) {
            Composition composition = fakeComposition(i, "music-" + i);
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

    public static Composition fakeComposition(long id) {
        return new Composition(null,
                null,
                null,
                String.valueOf(id),
                0,
                0,
                id,
                ++id,
                new Date(0),
                new Date(0),
                null);
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
            Composition composition = fakeComposition(i, "music-" + i);
            compositions.put(i, composition);
        }
        return compositions;
    }

    public static LongSparseArray<StorageComposition> getFakeStorageCompositionsMap() {
        LongSparseArray<StorageComposition> compositions = new LongSparseArray<>();
        for (long i = 0; i < 100000; i++) {
            StorageComposition composition = fakeStorageComposition(i, "music-" + i);
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
        return new Composition(null,
                null,
                null,
                filePath,
                0,
                0,
                id,
                ++id,
                new Date(createDate),
                new Date(0),
                null);
    }

    public static Composition fakeComposition(long id,
                                              String filePath,
                                              long createDate,
                                              long modifyDate) {
        return new Composition(null,
                null,
                null,
                filePath,
                0,
                0,
                id,
                ++id,
                new Date(createDate),
                new Date(modifyDate),
                null);
    }

    public static Composition fakeCompositionWithSize(long id, String filePath, long size) {
        return new Composition(null,
                null,
                null,
                filePath,
                0,
                size,
                id,
                ++id,
                new Date(0),
                new Date(0),
                null);
    }

    public static Composition fakeComposition(long id, long createDate) {
        return new Composition(null,
                null,
                null,
                String.valueOf(id),
                0,
                0,
                id,
                ++id,
                new Date(createDate * 1000L),
                new Date(0),
                null);
    }

    public static Composition fakeComposition(long id, String filePath) {
        return new Composition(null,
                null,
                null,
                filePath,
                0,
                0,
                id,
                ++id,
                new Date(0),
                new Date(0),
                null);
    }

    public static Composition fakeCompositionWithTitle(long id, String title) {
        return new Composition(null,
                title,
                null,
                String.valueOf(id),
                0,
                0,
                id,
                ++id,
                new Date(0),
                new Date(0),
                null);
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

    public static LongSparseArray<StoragePlayList> storagePlayLists(long count) {
        LongSparseArray<StoragePlayList> items = new LongSparseArray<>();
        for (long i = 0; i < count; i++) {
            items.put(i, storagePlayList(i));
        }
        return items;
    }

    public static List<StoragePlayList> storagePlayListsAsList(long count) {
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

    public static StorageComposition fakeStorageComposition(long id, String title) {
        return new StorageLocalCompositionBuilder(id, id, title).build();
    }

    public static StorageComposition fakeStorageComposition(long id,
                                                            String title,
                                                            long createDate,
                                                            long modifyDate) {
        return new StorageLocalCompositionBuilder(id, id, title)
                .createDate(createDate)
                .modifyDate(modifyDate)
                .build();
    }

    public static class StorageLocalCompositionBuilder {

        private String artist;
        private String albumArtist;
        private String title;
        private String album;
        private String filePath;

        private long duration;
        private long size;
        private final long id;
        private final long storageId;

        private Long folderId;

        private Date dateAdded = new Date(0);
        private Date dateModified = new Date(0);

        public StorageLocalCompositionBuilder(long id, long storageId, String title) {
            this.id = id;
            this.storageId = storageId;
            this.title = title;
        }

        public StorageLocalCompositionBuilder folderId(Long folderId) {
            this.folderId = folderId;
            return this;
        }

        public StorageLocalCompositionBuilder createDate(long date) {
            dateAdded = new Date(date);
            return this;
        }

        public StorageLocalCompositionBuilder modifyDate(long date) {
            dateModified = new Date(date);
            return this;
        }

        public StorageComposition build() {
            return new StorageComposition(artist,
                    albumArtist,
                    title,
                    album,
                    filePath,
                    duration,
                    size,
                    id,
                    storageId,
                    folderId,
                    dateAdded,
                    dateModified);
        }
    }

    public static StorageFullComposition fakeStorageFullComposition(long id, String title) {
        return new StorageCompositionBuilder(id, title).build();
    }

    public static class StorageCompositionBuilder {

        private long id;
        private String title;
        private String relativePath = "";
        private Date createDate = new Date(0);
        private Date modifyDate = new Date(0);

        public StorageCompositionBuilder(long id, String title) {
            this.id = id;
            this.title = title;
        }

        public StorageCompositionBuilder relativePath(String relativePath) {
            this.relativePath = relativePath;
            return this;
        }


        public StorageCompositionBuilder createDate(long date) {
            createDate = new Date(date);
            return this;
        }

        public StorageCompositionBuilder modifyDate(long date) {
            modifyDate = new Date(date);
            return this;
        }

        public StorageFullComposition build() {
            return new StorageFullComposition(null,
                    title,
                    "",
                    relativePath,
                    0,
                    0,
                    id,
                    createDate,
                    modifyDate,
                    null);
        }
    }
}
