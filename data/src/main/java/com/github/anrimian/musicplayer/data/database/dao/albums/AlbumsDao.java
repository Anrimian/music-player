package com.github.anrimian.musicplayer.data.database.dao.albums;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.github.anrimian.musicplayer.data.database.entities.albums.AlbumEntity;
import com.github.anrimian.musicplayer.data.storage.providers.albums.StorageAlbum;
import com.github.anrimian.musicplayer.domain.models.albums.Album;

import java.util.List;

import io.reactivex.Observable;

@Dao
public interface AlbumsDao {

    @Query("SELECT id FROM albums WHERE storageId = :storageId")
    Long selectIdByStorageId(long storageId);

    @Query("SELECT storageId as id," +
            "albumName as album," +
            "albumKey as albumKey," +
            "(SELECT artistName FROM artists WHERE artists.id = artistId) as artist," +
            "(SELECT storageId FROM artists WHERE artists.id = artistId) as artistId," +
            "firstYear as firstYear," +
            "lastYear as lastYear " +
            "FROM albums")
    List<StorageAlbum> selectAllAsStorageAlbums();

    @Insert
    void insertAll(List<AlbumEntity> artists);

    @Query("SELECT id as id," +
            "storageId as storageId, " +
            "albumName as name, " +
            "(SELECT artistName FROM artists WHERE artists.id = albums.artistId) as artist, " +
            "(SELECT count() FROM compositions WHERE albumId = albums.id) as compositionsCount " +
            "FROM albums")
    Observable<List<Album>> getAllObservable();
}
