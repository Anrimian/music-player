package com.github.anrimian.musicplayer.data.database.entities.albums;

public class ShortAlbum {

    private final String name;
    private final String artist;

    public ShortAlbum(String name, String artist) {
        this.name = name;
        this.artist = artist;
    }

    public String getName() {
        return name;
    }

    public String getArtist() {
        return artist;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ShortAlbum that = (ShortAlbum) o;

        if (!name.equals(that.name)) return false;
        return artist != null ? artist.equals(that.artist) : that.artist == null;
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + (artist != null ? artist.hashCode() : 0);
        return result;
    }
}
