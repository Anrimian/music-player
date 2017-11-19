package com.github.anrimian.simplemusicplayer.data.repositories.music;

import com.github.anrimian.simplemusicplayer.data.database.AppDatabase;
import com.github.anrimian.simplemusicplayer.data.database.dao.CompositionsDao;
import com.github.anrimian.simplemusicplayer.data.mappers.compositions.CompositionsMapper;
import com.github.anrimian.simplemusicplayer.domain.models.Composition;
import com.github.anrimian.simplemusicplayer.domain.repositories.PlayListRepository;

import org.mapstruct.factory.Mappers;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Scheduler;
import io.reactivex.Single;

/**
 * Created on 18.11.2017.
 */

public class PlayListRepositoryImpl implements PlayListRepository {

    private CompositionsDao compositionsDao;
    private Scheduler dbScheduler;

    private CompositionsMapper compositionsMapper = Mappers.getMapper(CompositionsMapper.class);

    public PlayListRepositoryImpl(AppDatabase appDatabase, Scheduler dbScheduler) {
        this.compositionsDao = appDatabase.compositionsDao();
        this.dbScheduler = dbScheduler;
    }

    @Override
    public Completable setCurrentPlayList(List<Composition> playList) {
        return Completable.fromRunnable(() -> {
            compositionsDao.deleteCurrentPlayList();
            compositionsDao.insertAll(compositionsMapper.toCompositionEntityList(playList));
        }).subscribeOn(dbScheduler);

    }

    @Override
    public Single<List<Composition>> getCurrentPlayList() {
        return Single.fromCallable(() -> compositionsDao.getCurrentPlayList())
                .map(compositionsMapper::toCompositions)
                .subscribeOn(dbScheduler);
    }
}
