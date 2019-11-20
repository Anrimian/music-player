package com.github.anrimian.musicplayer.data.database.mappers;

import com.github.anrimian.musicplayer.data.database.entities.composition.CompositionEntity;
import com.github.anrimian.musicplayer.data.storage.providers.music.StorageComposition;

import javax.annotation.Nullable;

public class CompositionMapper {

    public static CompositionEntity toEntity(StorageComposition composition,
                                             @Nullable Long artistId,
                                             @Nullable Long albumId) {
        return new CompositionEntity(artistId,
                albumId,
                composition.getTitle(),
                composition.getFilePath(),
                composition.getDuration(),
                composition.getSize(),
                composition.getId(),
                composition.getDateAdded(),
                composition.getDateModified(),
                CompositionCorruptionDetector.getCorruptionType(composition));
    }
}
