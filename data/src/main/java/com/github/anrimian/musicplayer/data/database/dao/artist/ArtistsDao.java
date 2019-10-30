package com.github.anrimian.musicplayer.data.database.dao.artist;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.github.anrimian.musicplayer.data.database.entities.artist.ArtistEntity;
import com.github.anrimian.musicplayer.data.storage.providers.artist.StorageArtist;

import java.util.List;

@Dao
public interface ArtistsDao {

    @Query("SELECT id FROM artists WHERE storageId = :storageId")
    Long selectIdByStorageId(long storageId);

    @Query("SELECT storageId as id," +
            "artistName as artist," +
            "artistKey as artistKey " +
            "FROM artists")
    List<StorageArtist> selectAllAsStorageArtists();

    @Insert
    void insertAll(List<ArtistEntity> artists);
}
