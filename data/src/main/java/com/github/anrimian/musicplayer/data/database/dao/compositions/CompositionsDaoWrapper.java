package com.github.anrimian.musicplayer.data.database.dao.compositions;

import androidx.collection.LongSparseArray;
import androidx.sqlite.db.SimpleSQLiteQuery;

import com.github.anrimian.musicplayer.data.database.AppDatabase;
import com.github.anrimian.musicplayer.data.database.mappers.CompositionMapper;
import com.github.anrimian.musicplayer.data.models.exceptions.CompositionNotFoundException;
import com.github.anrimian.musicplayer.data.storage.providers.music.StorageComposition;
import com.github.anrimian.musicplayer.data.utils.collections.AndroidCollectionUtils;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.composition.CorruptionType;
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
                .map(list -> mapList(list, CompositionMapper::toComposition));
    }

    public Observable<Composition> getCompositionObservable(long id) {
        return compositionsDao.getCompositionObservable(id)//TODO takeUntil()
                .map(CompositionMapper::toComposition);
    }

    public Observable<List<Composition>> getAllObservable(Order order,
                                                          @Nullable String searchText) {
        String query =  "SELECT * FROM compositions";
        query += getSearchQuery(searchText);
        query += getOrderQuery(order);
        SimpleSQLiteQuery sqlQuery = new SimpleSQLiteQuery(query);
        return compositionsDao.getAllObservable(sqlQuery)
                .map(list -> mapList(list, CompositionMapper::toComposition));
    }

    public List<Composition> getAll() {
        return mapList(compositionsDao.getAll(), CompositionMapper::toComposition);
    }

    public Map<Long, Composition> getAllMap() {
        return mapToMap(getAll(), new HashMap<>(), Composition::getId);
    }

    public LongSparseArray<StorageComposition> selectAllAsStorageCompositions() {
        return AndroidCollectionUtils.mapToSparseArray(compositionsDao.selectAllAsStorageCompositions(),
                StorageComposition::getId);
    }

    public void delete(long id) {
        compositionsDao.delete(id);
    }

    public void deleteAll(List<Long> ids) {
        compositionsDao.delete(ids);
    }

    public void deleteAll() {
        compositionsDao.deleteAll();
    }

    public void updateFilePath(long id, String filePath) {
        compositionsDao.updateFilePath(id, filePath);
    }

    public void updateFilesPath(List<Composition> compositions) {
        appDatabase.runInTransaction(() -> {
            for (Composition composition: compositions) {
                compositionsDao.updateFilePath(composition.getId(), composition.getFilePath());
            }
        });
    }

    public void updateArtist(long id, String artist) {
        compositionsDao.updateArtist(id, artist);
    }

    public void updateTitle(long id, String title) {
        compositionsDao.updateTitle(id, title);
    }

    public void applyChanges(List<StorageComposition> addedCompositions,
                             List<StorageComposition> deletedCompositions,
                             List<StorageComposition> changedCompositions) {
        appDatabase.runInTransaction(() -> {
            compositionsDao.insert(mapList(addedCompositions, CompositionMapper::toEntity));
            for (StorageComposition composition: deletedCompositions) {
                compositionsDao.deleteByStorageId(composition.getId());//TODO delete by id instead
            }
            for (StorageComposition composition: changedCompositions) {
                compositionsDao.update(
                        composition.getArtist(),
                        composition.getTitle(),
                        composition.getAlbum(),
                        composition.getFilePath(),
                        composition.getDuration(),
                        composition.getSize(),
                        composition.getDateAdded(),
                        composition.getDateModified(),
                        composition.getId()
                );
            }
        });
    }

    public void setCorruptionType(CorruptionType corruptionType, long id) {
        compositionsDao.setCorruptionType(corruptionType, id);
    }

    public long selectIdByStorageId(long compositionId) {
        return compositionsDao.selectIdByStorageId(compositionId);
    }

    public long getStorageId(long compositionId) {
        Long storageId = compositionsDao.getStorageId(compositionId);
        if (storageId == null) {
            throw new CompositionNotFoundException();
        }
        return storageId;
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
