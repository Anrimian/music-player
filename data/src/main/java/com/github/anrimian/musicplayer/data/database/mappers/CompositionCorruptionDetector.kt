package com.github.anrimian.musicplayer.data.database.mappers;

import com.github.anrimian.musicplayer.data.storage.providers.music.StorageFullComposition;
import com.github.anrimian.musicplayer.domain.models.composition.CorruptionType;

public class CompositionCorruptionDetector {

    public static CorruptionType getCorruptionType(StorageFullComposition composition) {
        return getCorruptionType(composition.getDuration());
    }

    public static CorruptionType getCorruptionType(long duration) {
        CorruptionType corruptionType = null;
        if (duration == 0) {
            corruptionType = CorruptionType.UNKNOWN;
        }
        return corruptionType;
    }
}
