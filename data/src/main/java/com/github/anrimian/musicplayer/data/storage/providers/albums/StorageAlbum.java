package com.github.anrimian.musicplayer.data.storage.providers.albums;

import androidx.annotation.NonNull;

public class StorageAlbum {

    private final long id;
    private final String album;
    private final String albumKey;

    private final String artist;
    private final long artistId;

    private final long firstYear;
    private final long lastYear;

    public StorageAlbum(long id,
                        String album,
                        String albumKey,
                        String artist,
                        long artistId,
                        long firstYear,
                        long lastYear) {
        this.id = id;
        this.album = album;
        this.albumKey = albumKey;
        this.artist = artist;
        this.artistId = artistId;
        this.firstYear = firstYear;
        this.lastYear = lastYear;
    }

    public long getId() {
        return id;
    }

    public String getAlbum() {
        return album;
    }

    public String getAlbumKey() {
        return albumKey;
    }

    public String getArtist() {
        return artist;
    }

    public long getArtistId() {
        return artistId;
    }

    public long getFirstYear() {
        return firstYear;
    }

    public long getLastYear() {
        return lastYear;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        StorageAlbum that = (StorageAlbum) o;

        return id == that.id;
    }

    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }

    @NonNull
    @Override
    public String toString() {
        return "StorageAlbum{" +
                "id=" + id +
                ", album='" + album + '\'' +
                ", albumKey='" + albumKey + '\'' +
                ", artist='" + artist + '\'' +
                ", artistId=" + artistId +
                ", firstYear=" + firstYear +
                ", lastYear=" + lastYear +
                '}';
    }
}
