package com.github.anrimian.musicplayer.data.database.dao.compositions;

import androidx.sqlite.db.SimpleSQLiteQuery;

import com.github.anrimian.musicplayer.data.database.AppDatabase;
import com.github.anrimian.musicplayer.data.database.entities.composition.CompositionEntity;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.composition.order.Order;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import io.reactivex.Observable;

import static com.github.anrimian.musicplayer.domain.utils.ListUtils.mapList;
import static com.github.anrimian.musicplayer.domain.utils.ListUtils.mapToMap;
import static com.github.anrimian.musicplayer.domain.utils.TextUtils.isEmpty;

public class CompositionsDaoWrapper {

    private final AppDatabase appDatabase;
    private final CompositionsDao compositionsDao;

    public CompositionsDaoWrapper(AppDatabase appDatabase,
                                  CompositionsDao compositionsDao) {
        this.appDatabase = appDatabase;
        this.compositionsDao = compositionsDao;
    }

    public Observable<List<Composition>> getAllObservable() {
        return compositionsDao.getAllObservable()
                .map(list -> mapList(list, this::toComposition));
    }

    public Observable<Composition> getCompoisitionObservable(long id) {
        return compositionsDao.getCompoisitionObservable(id)
                .map(this::toComposition);
    }

    public Observable<List<Composition>> getAllObservable(Order order,
                                                          @Nullable String searchText) {
        String query =  "SELECT * FROM compositions";
        query += getSearchQuery(searchText);
        query += getOrderQuery(order);
        SimpleSQLiteQuery sqlQuery = new SimpleSQLiteQuery(query);
        return compositionsDao.getAllObservable(sqlQuery)
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

    private String getOrderQuery(Order order) {
        StringBuilder orderQuery = new StringBuilder(" ORDER BY ");
        switch (order.getOrderType()) {
            case ALPHABETICAL: {
                orderQuery.append("title");
                break;
            }
            case ADD_TIME: {
                orderQuery.append("dateAdded");
                break;
            }
            default: throw new IllegalStateException("unknown order type" + order);
        }
        orderQuery.append(" ");
        orderQuery.append(order.isReversed()? "DESC" : "ASC");
        return orderQuery.toString();
    }

    private String getSearchQuery(String searchText) {
        if (isEmpty(searchText)) {
            return "";
        }
        StringBuilder sb = new StringBuilder(" WHERE ");
        sb.append("title LIKE '%");
        sb.append(searchText);
        sb.append("%'");
        sb.append(" OR artist NOTNULL AND artist LIKE '%");
        sb.append(searchText);
        sb.append("%'");

        return sb.toString();
    }
}
