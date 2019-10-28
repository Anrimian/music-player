package com.github.anrimian.musicplayer.data.storage.providers.artist;

import androidx.annotation.NonNull;

public class StorageArtist {

    private final long id;
    private final String artist;
    private final String artistKey;

    public StorageArtist(long id, String artist, String artistKey) {
        this.id = id;
        this.artist = artist;
        this.artistKey = artistKey;
    }

    public long getId() {
        return id;
    }

    public String getArtist() {
        return artist;
    }

    public String getArtistKey() {
        return artistKey;
    }

    @NonNull
    @Override
    public String toString() {
        return "StorageArtist{" +
                "id=" + id +
                ", artist='" + artist + '\'' +
                ", artistKey='" + artistKey + '\'' +
                '}';
    }
}
