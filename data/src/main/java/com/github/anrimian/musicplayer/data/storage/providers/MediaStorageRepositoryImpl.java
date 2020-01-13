package com.github.anrimian.musicplayer.data.storage.providers;

import androidx.collection.LongSparseArray;

import com.github.anrimian.musicplayer.data.database.dao.albums.AlbumsDaoWrapper;
import com.github.anrimian.musicplayer.data.database.dao.artist.ArtistsDaoWrapper;
import com.github.anrimian.musicplayer.data.database.dao.compositions.CompositionsDaoWrapper;
import com.github.anrimian.musicplayer.data.database.dao.genre.GenresDaoWrapper;
import com.github.anrimian.musicplayer.data.database.dao.play_list.PlayListsDaoWrapper;
import com.github.anrimian.musicplayer.data.database.entities.IdPair;
import com.github.anrimian.musicplayer.data.models.changes.Change;
import com.github.anrimian.musicplayer.data.storage.providers.albums.StorageAlbum;
import com.github.anrimian.musicplayer.data.storage.providers.albums.StorageAlbumsProvider;
import com.github.anrimian.musicplayer.data.storage.providers.artist.StorageArtistsProvider;
import com.github.anrimian.musicplayer.data.storage.providers.genres.StorageGenre;
import com.github.anrimian.musicplayer.data.storage.providers.genres.StorageGenreItem;
import com.github.anrimian.musicplayer.data.storage.providers.genres.StorageGenresProvider;
import com.github.anrimian.musicplayer.data.storage.providers.music.StorageComposition;
import com.github.anrimian.musicplayer.data.storage.providers.music.StorageFullComposition;
import com.github.anrimian.musicplayer.data.storage.providers.music.StorageMusicProvider;
import com.github.anrimian.musicplayer.data.storage.providers.playlists.StoragePlayList;
import com.github.anrimian.musicplayer.data.storage.providers.playlists.StoragePlayListItem;
import com.github.anrimian.musicplayer.data.storage.providers.playlists.StoragePlayListsProvider;
import com.github.anrimian.musicplayer.data.utils.collections.AndroidCollectionUtils;
import com.github.anrimian.musicplayer.domain.repositories.MediaStorageRepository;
import com.github.anrimian.musicplayer.domain.utils.Objects;
import com.github.anrimian.musicplayer.domain.utils.validation.DateUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.reactivex.Completable;
import io.reactivex.Scheduler;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

public class MediaStorageRepositoryImpl implements MediaStorageRepository {

    private final StorageMusicProvider musicProvider;
    private final StoragePlayListsProvider playListsProvider;
    private final StorageArtistsProvider artistsProvider;
    private final StorageAlbumsProvider albumsProvider;
    private final StorageGenresProvider genresProvider;
    private final CompositionsDaoWrapper compositionsDao;
    private final PlayListsDaoWrapper playListsDao;
    private final ArtistsDaoWrapper artistsDao;
    private final AlbumsDaoWrapper albumsDao;
    private final GenresDaoWrapper genresDao;
    private final Scheduler scheduler;

    private CompositeDisposable mediaStoreDisposable = new CompositeDisposable();
    private LongSparseArray<Disposable> playListEntriesDisposable = new LongSparseArray<>();
    private LongSparseArray<Disposable> genreEntriesDisposable = new LongSparseArray<>();

    public MediaStorageRepositoryImpl(StorageMusicProvider musicProvider,
                                      StoragePlayListsProvider playListsProvider,
                                      StorageArtistsProvider artistsProvider,
                                      StorageAlbumsProvider albumsProvider,
                                      StorageGenresProvider genresProvider,
                                      CompositionsDaoWrapper compositionsDao,
                                      PlayListsDaoWrapper playListsDao,
                                      ArtistsDaoWrapper artistsDao,
                                      AlbumsDaoWrapper albumsDao,
                                      GenresDaoWrapper genresDao,
                                      Scheduler scheduler) {
        this.musicProvider = musicProvider;
        this.playListsProvider = playListsProvider;
        this.artistsProvider = artistsProvider;
        this.albumsProvider = albumsProvider;
        this.genresProvider = genresProvider;
        this.compositionsDao = compositionsDao;
        this.playListsDao = playListsDao;
        this.artistsDao = artistsDao;
        this.albumsDao = albumsDao;
        this.genresDao = genresDao;
        this.scheduler = scheduler;
    }

    @Override
    public void runStorageObserver() {
        runRescanStorage()
                .doOnComplete(this::subscribeOnMediaStoreChanges)
                .subscribe();
    }

    @Override
    public synchronized void rescanStorage() {
        runRescanStorage().subscribe();
    }

    private void subscribeOnMediaStoreChanges() {
        mediaStoreDisposable.add(musicProvider.getCompositionsObservable()
                .subscribeOn(scheduler)
                .subscribe(this::applyCompositionsData));
        mediaStoreDisposable.add(playListsProvider.getPlayListsObservable()
                .subscribeOn(scheduler)
                .subscribe(this::onStoragePlayListReceived));
        subscribeOnPlaylistData();
        mediaStoreDisposable.add(genresProvider.getGenresObservable()
                .subscribeOn(scheduler)
                .subscribe(this::onStorageGenresReceived));
        subscribeOnGenresData();
    }

    private Completable runRescanStorage() {
        return Completable.fromAction(() -> {
            applyCompositionsData(musicProvider.getCompositions());
            applyPlayListData(playListsProvider.getPlayLists());

            List<IdPair> allPlayLists = playListsDao.getPlayListsIds();
            for (IdPair playListIds: allPlayLists) {
                long storageId = playListIds.getStorageId();
                long dbId = playListIds.getDbId();
                applyPlayListItemsData(dbId, playListsProvider.getPlayListItems(storageId));
            }
            applyGenresData(genresProvider.getGenres());
            List<IdPair> genresIds = genresDao.getGenresIds();
            for (IdPair genreId: genresIds) {
                long storageId = genreId.getStorageId();
                long dbId = genreId.getDbId();
                applyGenreItemsData(dbId, genresProvider.getGenreItems(storageId));
            }
        }).subscribeOn(scheduler);
    }

    private void onStoragePlayListReceived(LongSparseArray<StoragePlayList> newPlayLists) {
        applyPlayListData(newPlayLists);
        subscribeOnPlaylistData();
    }

    private void onStorageGenresReceived(Map<String, StorageGenre> newGenres) {
        applyGenresData(newGenres);
        subscribeOnGenresData();
    }

    private void subscribeOnGenresData() {
        List<IdPair> genresIds = genresDao.getGenresIds();
        for (IdPair genreId: genresIds) {
            long storageId = genreId.getStorageId();
            long dbId = genreId.getDbId();
            if (!genreEntriesDisposable.containsKey(dbId)) {
                Disposable disposable = genresProvider.getGenreItemsObservable(storageId)
                        .startWith(genresProvider.getGenreItems(storageId))
                        .subscribeOn(scheduler)
                        .subscribe(entries -> applyGenreItemsData(dbId, entries));
                genreEntriesDisposable.put(dbId, disposable);
            }
        }
    }

    //can items change order on merge?
    private synchronized void applyPlayListItemsData(long playListId,
                                                     List<StoragePlayListItem> newItems) {
        if (!playListsDao.isPlayListExists(playListId)) {
            playListEntriesDisposable.remove(playListId);
            return;
        }
        List<StoragePlayListItem> currentItems = playListsDao.getPlayListItemsAsStorageItems(playListId);
        LongSparseArray<StoragePlayListItem> currentItemsMap = AndroidCollectionUtils.mapToSparseArray(
                currentItems,
                StoragePlayListItem::getItemId);

        LongSparseArray<StoragePlayListItem> newItemsMap = AndroidCollectionUtils.mapToSparseArray(
                newItems,
                StoragePlayListItem::getItemId);

        List<StoragePlayListItem> addedItems = new ArrayList<>();
        boolean hasChanges = AndroidCollectionUtils.processChanges(currentItemsMap,
                newItemsMap,
                (o1, o2) -> false,
                item -> {},
                addedItems::add,
                item -> {});

        if (hasChanges) {
            playListsDao.insertPlayListItems(addedItems, playListId);
        }
    }

    private synchronized void applyGenreItemsData(long genreId,
                                                  LongSparseArray<StorageGenreItem> newGenreItems) {
        if (newGenreItems.isEmpty() || !genresDao.isGenreExists(genreId)) {
            genresDao.deleteGenre(genreId);
            genreEntriesDisposable.remove(genreId);
            return;
        }

        LongSparseArray<StorageGenreItem> currentItems = genresDao.selectAllAsStorageGenreItems(genreId);
        List<StorageGenreItem> addedItems = new ArrayList<>();
        boolean hasChanges = AndroidCollectionUtils.processChanges(currentItems,
                newGenreItems,
                (o1, o2) -> false,
                item -> {},
                addedItems::add,
                item -> {});

        if (hasChanges) {
            genresDao.applyChanges(addedItems, genreId);
        }
    }

    private synchronized void applyPlayListData(LongSparseArray<StoragePlayList> newPlayLists) {
        List<StoragePlayList> currentPlayLists = playListsDao.getAllAsStoragePlayLists();
        LongSparseArray<StoragePlayList> currentPlayListsMap = AndroidCollectionUtils.mapToSparseArray(currentPlayLists,
                StoragePlayList::getId);

        List<StoragePlayList> addedPlayLists = new ArrayList<>();
        List<StoragePlayList> changedPlayLists = new ArrayList<>();
        boolean hasChanges = AndroidCollectionUtils.processChanges(currentPlayListsMap,
                newPlayLists,
                this::hasActualChanges,
                playList -> {},
                addedPlayLists::add,
                changedPlayLists::add);

        if (hasChanges) {
            playListsDao.applyChanges(addedPlayLists, changedPlayLists);
        }
    }

    private void subscribeOnPlaylistData() {
        List<IdPair> allPlayLists = playListsDao.getPlayListsIds();
        for (IdPair playListIds: allPlayLists) {
            long storageId = playListIds.getStorageId();
            long dbId = playListIds.getDbId();
            if (!playListEntriesDisposable.containsKey(dbId)) {
                Disposable disposable = playListsProvider.getPlayListEntriesObservable(storageId)
                        .startWith(playListsProvider.getPlayListItems(storageId))
                        .subscribeOn(scheduler)
                        .subscribe(entries -> applyPlayListItemsData(dbId, entries));
                playListEntriesDisposable.put(dbId, disposable);
            }
        }
    }

    private synchronized void applyGenresData(Map<String, StorageGenre> newGenres) {
        Set<String> currentGenres = genresDao.selectAllGenreNames();

        List<StorageGenre> addedGenres = new ArrayList<>();
        boolean hasChanges = AndroidCollectionUtils.processChanges(currentGenres,
                newGenres,
                name -> name,
                StorageGenre::getName,
                (o1, o2) -> false,
                item -> {},
                addedGenres::add,
                (name, item) -> {});

        if (hasChanges) {
            genresDao.applyChanges(addedGenres);
        }
    }

    //we can't merge data by storageId in future, merge by path+filename?
    private synchronized void applyCompositionsData(LongSparseArray<StorageFullComposition> newCompositions) {
        LongSparseArray<StorageComposition> currentCompositions = compositionsDao.selectAllAsStorageCompositions();

        List<StorageFullComposition> addedCompositions = new ArrayList<>();
        List<StorageComposition> deletedCompositions = new ArrayList<>();
        List<Change<StorageComposition, StorageFullComposition>> changedCompositions = new ArrayList<>();
        boolean hasChanges = AndroidCollectionUtils.processDiffChanges(currentCompositions,
                newCompositions,
                this::hasActualChanges,
                deletedCompositions::add,
                addedCompositions::add,
                (oldItem, newItem) -> changedCompositions.add(new Change<>(oldItem, newItem)));

        if (hasChanges) {
            compositionsDao.applyChanges(addedCompositions, deletedCompositions, changedCompositions);
        }
    }

    private boolean hasActualChanges(StoragePlayList first, StoragePlayList second) {
        return (!Objects.equals(first.getName(), second.getName())
                || !Objects.equals(first.getDateAdded(), second.getDateAdded())
                || !Objects.equals(first.getDateModified(), second.getDateModified()))
                && DateUtils.isAfter(first.getDateModified(), second.getDateModified());
    }

    private boolean hasActualChanges(StorageComposition first, StorageFullComposition second) {
        if (!DateUtils.isAfter(second.getDateModified(), first.getDateModified())) {
            return false;
        }

        String newAlbumName = null;
        String newAlbumArtist = null;
        StorageAlbum newAlbum = second.getStorageAlbum();
        if (newAlbum != null) {
            newAlbumName = newAlbum.getAlbum();
            newAlbumArtist = newAlbum.getArtist();
        }

        return !(Objects.equals(first.getDateAdded(), second.getDateAdded())
                && first.getDuration() == second.getDuration()
                && Objects.equals(first.getFilePath(), second.getFilePath())
                && first.getSize() == second.getSize()
                && Objects.equals(first.getTitle(), second.getTitle())
                && Objects.equals(first.getArtist(), second.getArtist())
                && Objects.equals(first.getAlbum(), newAlbumName)
                && Objects.equals(first.getAlbumArtist(), newAlbumArtist));
    }
}