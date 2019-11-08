package com.github.anrimian.musicplayer.data.database.mappers;

import com.github.anrimian.musicplayer.data.storage.providers.music.StorageComposition;
import com.github.anrimian.musicplayer.domain.models.composition.CorruptionType;

public class CompositionCorruptionDetector {

    public static CorruptionType getCorruptionType(StorageComposition composition) {
        CorruptionType corruptionType = null;
        if (composition.getDuration() == 0) {
            corruptionType = CorruptionType.UNKNOWN;
        }
        return corruptionType;
    }
}
