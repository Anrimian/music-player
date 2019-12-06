package com.github.anrimian.musicplayer.data.database.dao.artist;

import androidx.collection.LongSparseArray;

import com.github.anrimian.musicplayer.data.database.entities.artist.ArtistEntity;
import com.github.anrimian.musicplayer.data.storage.providers.artist.StorageArtist;
import com.github.anrimian.musicplayer.data.utils.collections.AndroidCollectionUtils;
import com.github.anrimian.musicplayer.domain.models.artist.Artist;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;

import java.util.List;

import io.reactivex.Observable;

import static com.github.anrimian.musicplayer.domain.utils.ListUtils.mapList;

public class ArtistsDaoWrapper {

    private final ArtistsDao artistsDao;

    public ArtistsDaoWrapper(ArtistsDao artistsDao) {
        this.artistsDao = artistsDao;
    }

    public void insertArtists(List<StorageArtist> artists) {
        artistsDao.insertAll(mapList(artists, this::toEntity));
    }

    public LongSparseArray<StorageArtist> selectAllAsStorageArtists() {
        return AndroidCollectionUtils.mapToSparseArray(
                artistsDao.selectAllAsStorageArtists(),
                StorageArtist::getId);
    }

    public Observable<List<Artist>> getAllObservable() {
        return artistsDao.getAllObservable();
    }

    public Observable<List<Composition>> getCompositionsByArtistObservable(long artistId) {
        return artistsDao.getCompositionsByArtistObservable(artistId);
    }

    public List<Composition> getCompositionsByArtist(long artistId) {
        return artistsDao.getCompositionsByArtist(artistId);
    }

    public Observable<Artist> getArtistObservable(long artistId) {
        return artistsDao.getArtistObservable(artistId)
                .takeWhile(list -> !list.isEmpty())
                .map(list -> list.get(0));
    }

    public String[] getAuthorNames() {
        return artistsDao.getAuthorNames();
    }

    public void updateArtistName(String name, long id) {
        artistsDao.updateArtistName(name, id);
    }

    private ArtistEntity toEntity(StorageArtist artist) {
        return new ArtistEntity(
                artist.getId(),
                artist.getArtist()
        );
    }
}
