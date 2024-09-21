package com.github.anrimian.musicplayer.data.database.dao.play_list;

import static com.github.anrimian.musicplayer.data.database.utils.DatabaseUtils.getSearchArg;
import static com.github.anrimian.musicplayer.data.database.utils.DatabaseUtils.getSearchArgs;

import androidx.sqlite.db.SimpleSQLiteQuery;

import com.github.anrimian.musicplayer.data.database.LibraryDatabase;
import com.github.anrimian.musicplayer.data.database.dao.compositions.CompositionsDao;
import com.github.anrimian.musicplayer.data.database.entities.playlist.PlayListEntryEntity;
import com.github.anrimian.musicplayer.data.models.exceptions.DuplicatePlaylistEntriesException;
import com.github.anrimian.musicplayer.data.models.exceptions.PlayListAlreadyExistsException;
import com.github.anrimian.musicplayer.data.repositories.scanner.storage.playlists.m3uparser.PlayListEntry;
import com.github.anrimian.musicplayer.data.storage.providers.playlists.AppPlayList;
import com.github.anrimian.musicplayer.data.storage.providers.playlists.StoragePlayList;
import com.github.anrimian.musicplayer.data.storage.providers.playlists.StoragePlayListItem;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.playlist.PlayList;
import com.github.anrimian.musicplayer.domain.models.playlist.PlayListItem;
import com.github.anrimian.musicplayer.domain.utils.functions.Function;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import io.reactivex.rxjava3.core.Observable;

public class PlayListsDaoWrapper {

    private final PlayListDao playListDao;
    private final CompositionsDao compositionsDao;
    private final LibraryDatabase libraryDatabase;

    public PlayListsDaoWrapper(PlayListDao playListDao,
                               CompositionsDao compositionsDao,
                               LibraryDatabase libraryDatabase) {
        this.playListDao = playListDao;
        this.compositionsDao = compositionsDao;
        this.libraryDatabase = libraryDatabase;
    }

    public void insertStoragePlaylist(StoragePlayList playList, List<StoragePlayListItem> entries) {
        libraryDatabase.runInTransaction(() -> {
            long storageId = playList.getStorageId();
            if (playListDao.isPlayListExistsByStorageId(storageId)) {
                return;
            }

            long id = playListDao.insertPlayList(
                    storageId,
                    getUniquePlayListName(playList.getName()),
                    playList.getDateAdded(),
                    playList.getDateModified()
            );
            insertPlayListItems(entries, id);
        });
    }

    public long insertPlayList(String name,
                               Date dateAdded,
                               Date dateModified,
                               List<Long> compositionsIds) {
        return libraryDatabase.runInTransaction(() -> {
            long playlistId = playListDao.insertPlayList(null, name, dateAdded, dateModified);
            insertPlayListEntries(playlistId, compositionsIds);
            return playlistId;
        });
    }

    public void setPlayListEntries(long playlistId, List<Long> compositionsIds) {
        libraryDatabase.runInTransaction(() -> {
            if (!playListDao.isPlaylistExists(playlistId)) {
                return;
            }
            playListDao.clearPlayListEntries(playlistId);
            insertPlayListEntries(playlistId, compositionsIds);
        });
    }

    public long insertPlayList(String name,
                               Date dateAdded,
                               Date dateModified,
                               Function<Long> storagePlayListFetcher) {
        if (playListDao.isPlayListWithNameExists(name)) {
            throw new PlayListAlreadyExistsException();
        }
        Long storageId = storagePlayListFetcher.call();
        long id = playListDao.insertPlayList(storageId, name, dateAdded, dateModified);
        if (id == -1) {
            throw new IllegalStateException("db not modified");
        }
        return id;
    }

    public List<AppPlayList> getAllAsStoragePlayLists() {
        return playListDao.getAllAsStoragePlayLists();
    }

    public List<AppPlayList> getAllPlayLists() {
        return playListDao.getAllPlayLists();
    }

    public AppPlayList getPlayList(long playlistId) {
        return playListDao.getPlayList(playlistId);
    }

    public void deletePlayList(long id) {
        playListDao.deletePlayList(id);
    }

    public void updatePlayListName(long id, String name) {
        if (playListDao.isPlayListWithNameExists(name)) {
            throw new PlayListAlreadyExistsException();
        }
        playListDao.updatePlayListName(id, name);
    }

    public Observable<List<PlayList>> getPlayListsObservable(String searchQuery) {
        return playListDao.getPlayListsObservable(getSearchArg(searchQuery));
    }

    public Observable<PlayList> getPlayListsObservable(long id) {
        return playListDao.getPlayListObservable(id)
                .takeWhile(items -> !items.isEmpty())
                .map(items -> items.get(0));
    }

    public Observable<List<PlayListItem>> getPlayListItemsObservable(long playListId,
                                                                     boolean useFileName,
                                                                     @Nullable String searchText) {
        String query = PlayListDao.getPlaylistItemsQuery(useFileName);
        Object[] args = new Object[4];
        args[0] = playListId;
        String[] searchArgs = getSearchArgs(searchText, 3);
        System.arraycopy(searchArgs, 0, args, 1, 3);
        SimpleSQLiteQuery sqlQuery = new SimpleSQLiteQuery(query, args);
        return playListDao.getPlayListItemsObservable(sqlQuery);
    }

    public Set<Long> getPlayListsForCompositions(List<Long> compositionIds) {
        Set<Long> playlistsIds = new HashSet<>();
        for (long compositionId: compositionIds) {
            playlistsIds.addAll(playListDao.getPlaylistsForComposition(compositionId));
        }
        return playlistsIds;
    }

    public List<Long> getCompositionIdsInPlaylist(long playlistId) {
        return playListDao.getCompositionIdsInPlaylist(playlistId);
    }

    public List<Composition> getCompositionsInPlaylist(long playlistId, boolean useFileName) {
        String query = PlayListDao.getCompositionsQuery(useFileName);
        SimpleSQLiteQuery sqlQuery = new SimpleSQLiteQuery(query, new Object[] { playlistId });
        return playListDao.getCompositionsInPlaylist(sqlQuery);
    }

    public List<PlayListEntry> getPlayListItemsAsFileEntries(long playListId) {
        return playListDao.getPlayListItemsAsFileEntries(playListId);
    }

    public int deletePlayListEntry(long id, long playListId) {
        return libraryDatabase.runInTransaction(() -> {
            int position = playListDao.selectPositionById(id);
            playListDao.deletePlayListEntry(id);
            playListDao.decreasePositionsAfter(position, playListId);
            return position;
        });
    }

    public void insertPlayListItems(List<StoragePlayListItem> items, long playListId) {
        insertPlayListItems(items, playListId, playListDao.selectMaxOrder(playListId));
    }

    //? change checkForDuplicates, ignoreDuplicates to enum
    // DuplicateCheckType: INSERT_ALL, INSERT_EXCLUDE_DUPLICATES, ABORT_ON_DUPLICATE
    public List<Composition> addCompositions(List<Composition> compositions,
                                             long playListId,
                                             int position,
                                             boolean checkForDuplicates,
                                             boolean ignoreDuplicates) {
        List<Composition> compositionsToInsert = compositions;
        if (checkForDuplicates || ignoreDuplicates) {
            List<Composition> duplicates = getCompositionsInPlaylist(playListId, compositions);
            if (!duplicates.isEmpty()) {
                if (!ignoreDuplicates) {
                    boolean hasNonDuplicates = duplicates.size() < compositions.size();
                    throw new DuplicatePlaylistEntriesException(duplicates, hasNonDuplicates);
                }
                compositionsToInsert = new ArrayList<>(compositions.size());
                Set<Composition> duplicatesSet = new HashSet<>(duplicates);
                for (Composition composition : compositions) {
                    if (!duplicatesSet.contains(composition)) {
                        compositionsToInsert.add(composition);
                    }
                }
            }
        }
        addCompositions(compositionsToInsert, playListId, position);
        return compositionsToInsert;
    }

    public void insertPlayListItems(List<StoragePlayListItem> items,
                                    long playListId,
                                    int position) {
        libraryDatabase.runInTransaction(() -> {
            playListDao.increasePositionsByCountAfter(items.size(), position, playListId);

            List<PlayListEntryEntity> entities = new ArrayList<>(items.size());
            int orderPosition = position;
            for (StoragePlayListItem item : items) {
                long compositionId = compositionsDao.selectIdByStorageId(item.getAudioId());
                if (compositionId == 0) {
                    continue;
                }

                PlayListEntryEntity entryEntity = new PlayListEntryEntity(
                        item.getItemId(),
                        compositionId,
                        playListId,
                        orderPosition++
                );
                entities.add(entryEntity);
            }
            playListDao.insertPlayListEntries(entities);
        });
    }

    public Long selectStorageId(long id) {
        return playListDao.selectStorageId(id);
    }

    public Long selectStorageItemId(long id) {
        return playListDao.selectStorageItemId(id);
    }

    public void moveItems(long playListId, int fromPos, int toPos) {
        playListDao.moveItems(playListId, fromPos, toPos);
    }

    public String selectPlayListName(long playListId) {
        return playListDao.selectPlayListName(playListId);
    }

    public long findPlaylist(String name) {
        return playListDao.findPlaylist(name);
    }

    public int getPlaylistSize(long playListId) {
        return playListDao.getPlaylistSize(playListId);
    }

    private void insertPlayListEntries(long playlistId, List<Long> compositionsIds) {
        for (int i = 0; i < compositionsIds.size(); i++) {
            long compositionId = compositionsIds.get(i);
            if (compositionsDao.isCompositionExists(compositionId)) {
                playListDao.insertPlayListEntry(null, compositionId, playlistId, i);
            }
        }
    }

    private void addCompositions(List<Composition> compositions,
                                 long playListId,
                                 int position) {
        libraryDatabase.runInTransaction(() -> {
            playListDao.increasePositionsByCountAfter(compositions.size(), position, playListId);

            List<PlayListEntryEntity> entities = new ArrayList<>(compositions.size());
            int orderPosition = position;
            for (Composition item : compositions) {
                PlayListEntryEntity entryEntity = new PlayListEntryEntity(
                        null,
                        item.getId(),
                        playListId,
                        orderPosition++
                );
                entities.add(entryEntity);
            }
            playListDao.insertPlayListEntries(entities);
            playListDao.updatePlayListModifyTime(playListId, new Date());
        });
    }

    private List<Composition> getCompositionsInPlaylist(long playListId, List<Composition> compositions) {
        List<Long> playlistCompositions = playListDao.getCompositionIdsInPlaylist(playListId);
        Set<Long> playlistCompositionsSet = new HashSet<>(playlistCompositions);
        List<Composition> foundCompositions = new ArrayList<>();
        for (Composition composition: compositions) {
            if (playlistCompositionsSet.contains(composition.getId())) {
                foundCompositions.add(composition);
            }
        }
        return foundCompositions;
    }

    private String getUniquePlayListName(String name) {
        return getUniquePlayListName(name, "");
    }

    private String getUniquePlayListName(String name, String salt) {
        String uniqueName = name;
        int i = 0;
        while (playListDao.isPlayListWithNameExists(uniqueName)) {
            i++;
            uniqueName = name + "("+ i + ")" + salt;
        }
        return uniqueName;
    }

}
