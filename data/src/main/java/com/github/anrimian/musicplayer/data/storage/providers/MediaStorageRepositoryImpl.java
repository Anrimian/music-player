package com.github.anrimian.musicplayer.data.storage.providers;

import com.github.anrimian.musicplayer.data.database.dao.compositions.CompositionsDaoWrapper;
import com.github.anrimian.musicplayer.data.database.dao.play_list.PlayListsDaoWrapper;
import com.github.anrimian.musicplayer.data.storage.providers.music.StorageMusicProvider;
import com.github.anrimian.musicplayer.data.storage.providers.playlists.StoragePlayList;
import com.github.anrimian.musicplayer.data.storage.providers.playlists.StoragePlayListsProvider;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.utils.CompositionHelper;
import com.github.anrimian.musicplayer.domain.repositories.MediaStorageRepository;
import com.github.anrimian.musicplayer.domain.utils.ListUtils;
import com.github.anrimian.musicplayer.domain.utils.Objects;
import com.github.anrimian.musicplayer.domain.utils.changes.map.MapChangeProcessor;
import com.github.anrimian.musicplayer.domain.utils.validation.DateUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import io.reactivex.Scheduler;
import io.reactivex.disposables.Disposable;

public class MediaStorageRepositoryImpl implements MediaStorageRepository {

    private final StorageMusicProvider musicProvider;
    private final StoragePlayListsProvider playListsProvider;
    private final CompositionsDaoWrapper compositionsDao;
    private final PlayListsDaoWrapper playListsDao;
    private final Scheduler scheduler;

    private Disposable compositionsDisposable;
    private Disposable playListsDisposable;

    public MediaStorageRepositoryImpl(StorageMusicProvider musicProvider,
                                      StoragePlayListsProvider playListsProvider,
                                      CompositionsDaoWrapper compositionsDao,
                                      PlayListsDaoWrapper playListsDao,
                                      Scheduler scheduler) {
        this.musicProvider = musicProvider;
        this.playListsProvider = playListsProvider;
        this.compositionsDao = compositionsDao;
        this.playListsDao = playListsDao;
        this.scheduler = scheduler;
    }

    @Override
    public void initialize() {
        compositionsDisposable = musicProvider.getCompositionsObservable()
                .startWith(musicProvider.getCompositions())
                .subscribeOn(scheduler)
                .subscribe(this::onNewCompositionsFromMediaStorageReceived);
    }

    @Override
    public void rescanStorage() {
        onNewCompositionsFromMediaStorageReceived(musicProvider.getCompositions());
    }

    private void onNewCompositionsFromMediaStorageReceived(Map<Long, Composition> newCompositions) {
        Map<Long, Composition> currentCompositions = compositionsDao.getAllMap();

        List<Composition> addedCompositions = new ArrayList<>();
        List<Composition> deletedCompositions = new ArrayList<>();
        List<Composition> changedCompositions = new ArrayList<>();
        boolean hasChanges = MapChangeProcessor.processChanges2(currentCompositions,
                newCompositions,
                this::hasActualChanges,
                deletedCompositions::add,
                addedCompositions::add,
                changedCompositions::add);

        if (hasChanges) {
            compositionsDao.applyChanges(addedCompositions, deletedCompositions, changedCompositions);
        }
        if (playListsDisposable == null) {
            playListsDisposable = playListsProvider.getPlayListsObservable()
                    .startWith(playListsProvider.getPlayLists())
                    .subscribeOn(scheduler)
                    .subscribe(this::onStoragePlayListReceived);
        }

    }

    private void onStoragePlayListReceived(Map<Long, StoragePlayList> newPlayLists) {
        List<StoragePlayList> currentPlayLists = playListsDao.getAllAsStoragePlayLists();
        Map<Long, StoragePlayList> currentPlayListsMap = ListUtils.mapToMap(currentPlayLists,
                new HashMap<>(),
                StoragePlayList::getId);

        List<StoragePlayList> addedPlayLists = new ArrayList<>();
        List<StoragePlayList> changedPlayLists = new ArrayList<>();
        boolean hasChanges = MapChangeProcessor.processChanges2(currentPlayListsMap,
                newPlayLists,
                this::hasActualChanges,
                playList -> {
                },
                addedPlayLists::add,
                changedPlayLists::add);

        if (hasChanges) {
            playListsDao.applyChanges(addedPlayLists, changedPlayLists);
        }
    }

    private boolean hasActualChanges(StoragePlayList first, StoragePlayList second) {
        return (!Objects.equals(first.getName(), second.getName())
                || !Objects.equals(first.getDateAdded(), second.getDateAdded())
                || !Objects.equals(first.getDateModified(), second.getDateModified()))
                && DateUtils.isAfter(first.getDateModified(), second.getDateModified());
    }

    private boolean hasActualChanges(@Nonnull Composition first, @Nonnull Composition second) {
        return !CompositionHelper.areSourcesTheSame(first, second)
                && DateUtils.isAfter(first.getDateModified(), second.getDateModified());
    }
}