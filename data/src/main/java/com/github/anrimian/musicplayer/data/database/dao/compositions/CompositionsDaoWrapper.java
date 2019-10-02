package com.github.anrimian.musicplayer.data.database.dao.compositions;

import com.github.anrimian.musicplayer.data.database.AppDatabase;
import com.github.anrimian.musicplayer.data.database.entities.composition.CompositionEntity;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Flowable;

import static com.github.anrimian.musicplayer.domain.utils.ListUtils.mapList;
import static com.github.anrimian.musicplayer.domain.utils.ListUtils.mapToMap;

public class CompositionsDaoWrapper {

    private final AppDatabase appDatabase;
    private final CompositionsDao compositionsDao;

    public CompositionsDaoWrapper(AppDatabase appDatabase,
                                  CompositionsDao compositionsDao) {
        this.appDatabase = appDatabase;
        this.compositionsDao = compositionsDao;
    }

    public Flowable<List<Composition>> getAllObservable() {
        return compositionsDao.getAllObservable()
                .map(list -> mapList(list, this::toComposition));
    }

    public List<Composition> getAll() {
        return mapList(compositionsDao.getAll(), this::toComposition);
    }

    public Map<Long, Composition> getAllMap() {
        return mapToMap(getAll(), new HashMap<>(), Composition::getId);
    }

    public long insert(Composition compositionEntity) {
        return compositionsDao.insert(toEntity(compositionEntity));
    }

    public void insert(List<Composition> compositions) {
        compositionsDao.insert(mapList(compositions, this::toEntity));
    }

    public void delete(long id) {
        compositionsDao.delete(id);
    }

    public void deleteAll() {
        compositionsDao.deleteAll();
    }

    public void updateFilePath(long id, String filePath) {
        compositionsDao.updateFilePath(id, filePath);
    }

    public void updateArtist(long id, String artist) {
        compositionsDao.updateArtist(id, artist);
    }

    public void updateTitle(long id, String title) {
        compositionsDao.updateTitle(id, title);
    }

    public void applyChanges(List<Composition> addedCompositions,
                             List<Composition> deletedCompositions,
                             List<Composition> changedCompositions) {
        appDatabase.runInTransaction(() -> {
            compositionsDao.insert(mapList(addedCompositions, this::toEntity));
            compositionsDao.delete(mapList(deletedCompositions, Composition::getId));
            compositionsDao.update(mapList(changedCompositions, this::toEntity));
        });
    }

    private Composition toComposition(CompositionEntity entity) {
        return new Composition(entity.getArtist(),
                entity.getTitle(),
                entity.getAlbum(),
                entity.getFilePath(),
                entity.getDuration(),
                entity.getSize(),
                entity.getId(),
                entity.getDateAdded(),
                entity.getDateModified(),
                entity.getCorruptionType());
    }

    private CompositionEntity toEntity(Composition composition) {
        return new CompositionEntity(composition.getArtist(),
                composition.getTitle(),
                composition.getAlbum(),
                composition.getFilePath(),
                composition.getDuration(),
                composition.getSize(),
                composition.getId(),
                composition.getDateAdded(),
                composition.getDateModified(),
                composition.getCorruptionType());
    }
}
