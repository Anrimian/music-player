package com.github.anrimian.musicplayer.data.database;

import com.github.anrimian.musicplayer.data.database.entities.composition.CompositionEntity;

import java.util.Date;

public class DataProvider {

    public static CompositionEntity composition(Long artistId, Long albumId, String title) {
        return new CompositionEntity(
                artistId,
                albumId,
                0L,
                title,
                "test file path",
                100L,
                100L,
                null,
                new Date(),
                new Date(),
                null);
    }
}
