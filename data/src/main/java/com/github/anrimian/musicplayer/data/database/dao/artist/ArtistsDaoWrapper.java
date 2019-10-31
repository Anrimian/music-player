package com.github.anrimian.musicplayer.data.database.dao.artist;

import androidx.collection.LongSparseArray;

import com.github.anrimian.musicplayer.data.database.entities.artist.ArtistEntity;
import com.github.anrimian.musicplayer.data.storage.providers.artist.StorageArtist;
import com.github.anrimian.musicplayer.data.utils.collections.AndroidCollectionUtils;
import com.github.anrimian.musicplayer.domain.models.artist.Artist;

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

    private ArtistEntity toEntity(StorageArtist artist) {
        return new ArtistEntity(
                artist.getId(),
                artist.getArtist(),
                artist.getArtistKey()
        );
    }
}
