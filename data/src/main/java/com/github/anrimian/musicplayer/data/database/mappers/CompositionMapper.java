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
                composition.getFileName(),
                composition.getDuration(),
                composition.getSize(),
                composition.getStorageId(),
                composition.getDateAdded(),
                composition.getDateModified(),
                new Date(0),
                CompositionCorruptionDetector.getCorruptionType(composition),
                composition.getAudioFileType(),
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
                composition.getLyrics(),
                composition.getFileName(),
                composition.getDuration(),
                composition.getSize(),
                null,
                new Date(composition.getDateAdded()),
                new Date(composition.getDateModified()),
                new Date(composition.getLastScanDate()),
                CompositionCorruptionDetector.getCorruptionType(composition.getDuration()),
                composition.getAudioFileType(),
                InitialSource.REMOTE);
    }
}
