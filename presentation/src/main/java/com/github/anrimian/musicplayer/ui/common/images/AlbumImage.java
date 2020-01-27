package com.github.anrimian.musicplayer.ui.common.images;

import com.github.anrimian.musicplayer.domain.models.albums.Album;

public class AlbumImage implements ImageMetaData {

    private final Album album;

    AlbumImage(Album album) {
        this.album = album;
    }

    public Album getAlbum() {
        return album;
    }

    @Override
    public String getKey() {
        return "album-" + album.getId();
    }
}
