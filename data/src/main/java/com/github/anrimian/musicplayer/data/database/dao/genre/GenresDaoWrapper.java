package com.github.anrimian.musicplayer.data.database.dao.genre;

import androidx.collection.LongSparseArray;

import com.github.anrimian.musicplayer.data.database.dao.compositions.CompositionsDao;
import com.github.anrimian.musicplayer.data.database.entities.IdPair;
import com.github.anrimian.musicplayer.data.database.entities.genres.GenreEntity;
import com.github.anrimian.musicplayer.data.database.entities.genres.GenreEntryEntity;
import com.github.anrimian.musicplayer.data.database.mappers.CompositionMapper;
import com.github.anrimian.musicplayer.data.storage.providers.genres.StorageGenre;
import com.github.anrimian.musicplayer.data.storage.providers.genres.StorageGenreItem;
import com.github.anrimian.musicplayer.data.utils.collections.AndroidCollectionUtils;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.genres.Genre;

import java.util.List;

import io.reactivex.Observable;

import static com.github.anrimian.musicplayer.domain.utils.ListUtils.mapList;

public class GenresDaoWrapper {

    private final GenreDao genreDao;
    private final CompositionsDao compositionsDao;

    public GenresDaoWrapper(GenreDao genreDao, CompositionsDao compositionsDao) {
        this.genreDao = genreDao;
        this.compositionsDao = compositionsDao;
    }

    public void applyChanges(List<StorageGenre> addedGenres) {
        genreDao.insertAll(mapList(addedGenres, this::toEntity));
    }

    public void applyChanges(List<StorageGenreItem> addedGenres, long genreId) {
        genreDao.insertGenreEntities(mapList(addedGenres, genre -> toEntity(genre, genreId)));
    }

    public LongSparseArray<StorageGenre> selectAllAsStorageGenre() {
        return AndroidCollectionUtils.mapToSparseArray(
                genreDao.selectAllAsStorageGenres(),
                StorageGenre::getId);
    }

    public LongSparseArray<StorageGenreItem> selectAllAsStorageGenreItems(long genreId) {
        return AndroidCollectionUtils.mapToSparseArray(
                genreDao.selectAllAsStorageGenreItems(genreId),
                StorageGenreItem::getId);
    }

    public Observable<List<Genre>> getAllObservable() {
        return genreDao.getAllObservable();
    }

    public Observable<Genre> getGenreObservable(long genreId) {
        return genreDao.getGenreObservable(genreId)
                .takeWhile(list -> !list.isEmpty())
                .map(list -> list.get(0));
    }

    public Observable<List<Composition>> getCompositionsInGenre(long genreId) {
        return genreDao.getCompositionsInGenre(genreId)
                .map(list -> mapList(list, CompositionMapper::toComposition));
    }

    public List<IdPair> getGenresIds() {
        return genreDao.getGenresIds();
    }

    private GenreEntity toEntity(StorageGenre genre) {
        return new GenreEntity(genre.getId(), genre.getName());
    }

    private GenreEntryEntity toEntity(StorageGenreItem genre, long genreId) {
        long audioId = compositionsDao.selectIdByStorageId(genre.getAudioId());
        return new GenreEntryEntity(audioId, genreId, genre.getId());
    }
}
