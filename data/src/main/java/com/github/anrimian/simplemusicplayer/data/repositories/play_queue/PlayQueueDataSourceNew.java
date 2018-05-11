package com.github.anrimian.simplemusicplayer.data.repositories.play_queue;

import com.github.anrimian.simplemusicplayer.data.database.dao.PlayQueueDao;
import com.github.anrimian.simplemusicplayer.data.models.compositions.CompositionsMapper;
import com.github.anrimian.simplemusicplayer.data.preferences.SettingsPreferences;
import com.github.anrimian.simplemusicplayer.data.storage.StorageMusicDataSource;
import com.github.anrimian.simplemusicplayer.domain.models.composition.Composition;

import org.mapstruct.factory.Mappers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Scheduler;
import io.reactivex.Single;

/**
 * Created on 16.04.2018.
 */
public class PlayQueueDataSourceNew {

    private final CompositionsMapper compositionsMapper = Mappers.getMapper(CompositionsMapper.class);

    private final PlayQueueDao playQueueDao;
    private final StorageMusicDataSource storageMusicDataSource;
    private final SettingsPreferences settingsPreferences;
    private final Scheduler scheduler;

    private List<Composition> initialPlayList;
    private List<Composition> shuffledPlayList;

    public PlayQueueDataSourceNew(PlayQueueDao playQueueDao,
                                  StorageMusicDataSource storageMusicDataSource,
                                  SettingsPreferences settingsPreferences,
                                  Scheduler scheduler) {
        this.playQueueDao = playQueueDao;
        this.storageMusicDataSource = storageMusicDataSource;
        this.settingsPreferences = settingsPreferences;
        this.scheduler = scheduler;
    }

    public Completable setPlayQueue(List<Composition> compositions) {
        return Single.fromCallable(() -> new PlayQueue(compositions))
                .flatMapCompletable(this::savePlayQueue)
                .subscribeOn(scheduler);
    }

    public Single<List<Composition>> getPlayQueue() {
        return Single.fromCallable(() -> {
            if (initialPlayList == null) {
                loadPlayQueue();
            }

            if (settingsPreferences.isRandomPlayingEnabled()) {
                return shuffledPlayList;
            }
            return initialPlayList;
        }).subscribeOn(scheduler);
    }

    /**
     *
     * @return new position of current composition
     */
    public int setRandomPlayingEnabled(boolean enabled, Composition currentComposition) {
        if (initialPlayList == null) {
            throw new IllegalStateException("change play mode before initialization");
        }

        settingsPreferences.setRandomPlayingEnabled(enabled);
        if (enabled) {
            shuffledPlayList.remove(currentComposition);
            shuffledPlayList.add(0, currentComposition);
//            savePlayQueue();//TODO optimize in next refactoring wave
            return 0;
        }
        return initialPlayList.indexOf(currentComposition);
    }

    private Completable savePlayQueue(PlayQueue playQueue) {
        return Completable.fromRunnable(() -> {

        });


//        Completable.fromRunnable(() -> {
//            playQueueDao.deleteCurrentPlayList();
//
//            List<CompositionItemEntity> itemEntities = new ArrayList<>();
//            for (int i = 0; i < initialPlayList.size(); i++) {
//                Composition composition = initialPlayList.get(i);
//                CompositionEntity compositionEntity = compositionsMapper.toCompositionEntity(composition);
//                CompositionItemEntity compositionItemEntity = new CompositionItemEntity();
//                compositionItemEntity.setComposition(compositionEntity);
//                compositionItemEntity.setInitialPosition(initialPlayList.indexOf(composition));
//                compositionItemEntity.setShuffledPosition(shuffledPlayList.indexOf(composition));
//                itemEntities.add(compositionItemEntity);
//            }
//            playQueueDao.setCurrentPlayList(itemEntities);
//        }).subscribeOn(scheduler)
//                .subscribe();

    }

    @SuppressWarnings("unchecked")
    private void loadPlayQueue() {
//        List<CompositionItemEntity> compositionItemEntities = playQueueDao.getCurrentPlayList();
//        Composition[] initialPlayListArray = new Composition[compositionItemEntities.size()];
//        Composition[] currentPlayListArray = new Composition[compositionItemEntities.size()];
//        for (CompositionItemEntity compositionItem: compositionItemEntities) {
//            Composition composition = compositionsMapper.toComposition(compositionItem.getComposition());
//            initialPlayListArray[compositionItem.getInitialPosition()] = composition;
//            currentPlayListArray[compositionItem.getShuffledPosition()] = composition;
//        }
//        initialPlayList = new ArrayList(asList(initialPlayListArray));
//        shuffledPlayList = new ArrayList(asList(currentPlayListArray));
    }
}
