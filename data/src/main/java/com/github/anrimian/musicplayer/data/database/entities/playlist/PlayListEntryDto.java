package com.github.anrimian.musicplayer.data.database.entities.playlist;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Embedded;

import com.github.anrimian.musicplayer.domain.models.composition.Composition;

public class PlayListEntryDto {

    private final long itemId;

    @Nullable
    private final Long storageItemId;

    @Embedded
    private final Composition composition;

    public PlayListEntryDto(long itemId,
                            @Nullable Long storageItemId,
                            Composition composition) {
        this.itemId = itemId;
        this.storageItemId = storageItemId;
        this.composition = composition;
    }

    @Nullable
    public Long getStorageItemId() {
        return storageItemId;
    }

    public long getItemId() {
        return itemId;
    }

    public Composition getComposition() {
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
