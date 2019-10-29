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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        StorageArtist that = (StorageArtist) o;

        return id == that.id;
    }

    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
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
