package com.github.anrimian.musicplayer.ui.common.serealization;

import android.os.Bundle;

import com.github.anrimian.musicplayer.domain.models.albums.Album;

public interface AlbumSerializer {

    String ID = "id";
    String STORAGE_ID = "storage_id";
    String NAME = "name";
    String ARTIST = "artist";
    String COMPOSITION_COUNT = "composition_count";

    static Bundle serialize(Album album) {
        Bundle bundle = new Bundle();
        bundle.putLong(ID, album.getId());
        Long storageId = album.getStorageId();
        bundle.putLong(STORAGE_ID, storageId == null? -1: storageId);
        bundle.putString(NAME, album.getName());
        bundle.putString(ARTIST, album.getArtist());
        bundle.putInt(COMPOSITION_COUNT, album.getCompositionsCount());
        return bundle;
    }

    static Album deserialize(Bundle bundle) {
        long storageId = bundle.getLong(STORAGE_ID);
        return new Album(bundle.getLong(ID),
                storageId == -1? null: storageId,
                bundle.getString(NAME),
                bundle.getString(ARTIST),
                bundle.getInt(COMPOSITION_COUNT)
        );
    }
}
