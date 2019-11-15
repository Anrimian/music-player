package com.github.anrimian.musicplayer.data.database.dao.albums;

import androidx.collection.LongSparseArray;

import com.github.anrimian.musicplayer.data.database.dao.artist.ArtistsDao;
import com.github.anrimian.musicplayer.data.database.entities.albums.AlbumEntity;
import com.github.anrimian.musicplayer.data.database.mappers.CompositionMapper;
import com.github.anrimian.musicplayer.data.storage.providers.albums.StorageAlbum;
import com.github.anrimian.musicplayer.data.utils.collections.AndroidCollectionUtils;
import com.github.anrimian.musicplayer.domain.models.albums.Album;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;

import java.util.List;

import io.reactivex.Observable;

import static com.github.anrimian.musicplayer.domain.utils.ListUtils.mapList;

public class AlbumsDaoWrapper {

    private final AlbumsDao albumsDao;
    private final ArtistsDao artistsDao;

    public AlbumsDaoWrapper(AlbumsDao albumsDao, ArtistsDao artistsDao) {
        this.albumsDao = albumsDao;
        this.artistsDao = artistsDao;
    }

    public void insertAll(List<StorageAlbum> albums) {
        albumsDao.insertAll(mapList(albums, this::toEntity));
    }

    public LongSparseArray<StorageAlbum> selectAllAsStorageAlbums() {
        return AndroidCollectionUtils.mapToSparseArray(
                albumsDao.selectAllAsStorageAlbums(),
                StorageAlbum::getId);
    }

    public Observable<List<Album>> getAllObservable() {
        return albumsDao.getAllObservable();
    }

    public Observable<List<Composition>> getCompositionsInAlbum(long albumId) {
        return albumsDao.getCompositionsInAlbum(albumId)
                .map(list -> mapList(list, CompositionMapper::toComposition));
    }

    public Observable<Album> getAlbumObservable(long albumId) {
        return albumsDao.getAlbumObservable(albumId)
                .takeWhile(list -> !list.isEmpty())
                .map(list -> list.get(0));
    }

    private AlbumEntity toEntity(StorageAlbum album) {
        Long artistId = artistsDao.selectIdByStorageId(album.getArtistId());
        return new AlbumEntity(
                artistId,
                album.getId(),
                album.getAlbum(),
                album.getAlbumKey(),
                album.getFirstYear(),
                album.getLastYear()
        );
    }

}
