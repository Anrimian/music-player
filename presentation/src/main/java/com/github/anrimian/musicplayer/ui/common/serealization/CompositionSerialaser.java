package com.github.anrimian.musicplayer.ui.common.serealization;

import android.os.Bundle;

import com.github.anrimian.musicplayer.domain.models.composition.Composition;

import java.util.Date;

public interface CompositionSerialaser {

    String ARTIST = "artist";
    String TITLE = "title";
    String ALBUM = "album";
    String FILE_PATH = "file_path";
    String DURATION = "duration";
    String SIZE = "size";
    String ID = "id";
    String DATE_ADDED = "date_added";
    String DATE_MODIFIED = "date_modified";
    String IS_CORRUPTED = "is_corrupted";

    static Bundle serialize(Composition composition) {
        Bundle bundle = new Bundle();
        bundle.putString(ARTIST, composition.getArtist());
        bundle.putString(TITLE, composition.getTitle());
        bundle.putString(ALBUM, composition.getAlbum());
        bundle.putString(FILE_PATH, composition.getFilePath());
        bundle.putLong(SIZE, composition.getSize());
        bundle.putLong(ID, composition.getId());
        bundle.putLong(DATE_ADDED, composition.getDateAdded().getTime());
        bundle.putLong(DATE_MODIFIED, composition.getDateModified().getTime());
        bundle.putBoolean(IS_CORRUPTED, composition.isCorrupted());
        return bundle;
    }

    static Composition deserialize(Bundle bundle) {
        return new Composition(
                bundle.getString(ARTIST),
                bundle.getString(TITLE),
                bundle.getString(ALBUM),
                bundle.getString(FILE_PATH),
                bundle.getLong(DURATION),
                bundle.getLong(SIZE),
                bundle.getLong(ID),
                new Date(bundle.getLong(DATE_ADDED)),
                new Date(bundle.getLong(DATE_MODIFIED)),
                bundle.getBoolean(IS_CORRUPTED)
        );
    }
}
