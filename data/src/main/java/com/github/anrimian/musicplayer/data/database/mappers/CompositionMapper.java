package com.github.anrimian.musicplayer.data.database.mappers;

import com.github.anrimian.musicplayer.data.database.entities.composition.CompositionEntity;
import com.github.anrimian.musicplayer.data.models.composition.ExternalComposition;
import com.github.anrimian.musicplayer.data.storage.providers.music.StorageFullComposition;
import com.github.anrimian.musicplayer.domain.models.composition.InitialSource;

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
                null,
                null,
                null,
                composition.getFileName(),
                composition.getDuration(),
                composition.getSize(),
                composition.getStorageId(),
                composition.getDateAdded(),
                composition.getDateModified(),
                new Date(0),
                new Date(0),
                CompositionCorruptionDetector.getCorruptionType(composition),
                InitialSource.LOCAL);
    }

    public static CompositionEntity toEntity(ExternalComposition composition,
                                             @Nullable Long artistId,
                                             @Nullable Long albumId,
                                             @Nullable Long folderId) {
        return new CompositionEntity(artistId,
                albumId,
                folderId,
                composition.getTitle(),
                composition.getTrackNumber(),
                composition.getDiscNumber(),
                composition.getComment(),
                composition.getLyrics(),
                composition.getFileName(),
                composition.getDuration(),
                composition.getSize(),
                null,
                new Date(composition.getDateAdded()),
                new Date(composition.getDateModified()),
                new Date(0),
                new Date(0),
                null,
                InitialSource.REMOTE);
    }
}
