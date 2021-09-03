package com.github.anrimian.musicplayer.data.database.mappers;

import com.github.anrimian.musicplayer.data.database.entities.composition.CompositionEntity;
import com.github.anrimian.musicplayer.data.storage.providers.music.StorageFullComposition;

import java.util.Date;

import javax.annotation.Nullable;

public class CompositionMapper {

    public static CompositionEntity toEntity(StorageFullComposition composition,
                                             @Nullable Long artistId,
                                             @Nullable Long albumId,
                                             @Nullable Long folderId) {
        return new CompositionEntity(artistId,
                albumId,
                folderId,
                composition.getTitle(),
                null,
                composition.getFileName(),
                composition.getRelativePath(),
                composition.getDuration(),
                composition.getSize(),
                composition.getId(),
                composition.getDateAdded(),
                composition.getDateModified(),
                new Date(0),
                CompositionCorruptionDetector.getCorruptionType(composition));
    }
}
