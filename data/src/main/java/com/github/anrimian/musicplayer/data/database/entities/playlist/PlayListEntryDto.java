package com.github.anrimian.musicplayer.data.database.entities.playlist;

import androidx.annotation.NonNull;
import androidx.room.Embedded;

import com.github.anrimian.musicplayer.data.database.entities.composition.CompositionEntity;

public class PlayListEntryDto {

    private final long itemId;

    @Embedded
    private final CompositionEntity composition;

    public PlayListEntryDto(long itemId,
                            CompositionEntity composition) {
        this.itemId = itemId;
        this.composition = composition;
    }

    public long getItemId() {
        return itemId;
    }

    public CompositionEntity getComposition() {
        return composition;
    }

    @NonNull
    @Override
    public String toString() {
        return "PlayListEntryDto{" +
                "itemId=" + itemId +
                ", composition=" + composition +
                '}';
    }
}
