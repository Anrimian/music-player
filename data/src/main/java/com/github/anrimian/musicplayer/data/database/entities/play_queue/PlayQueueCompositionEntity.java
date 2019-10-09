package com.github.anrimian.musicplayer.data.database.entities.play_queue;

import androidx.annotation.NonNull;
import androidx.room.Embedded;

import com.github.anrimian.musicplayer.data.database.entities.composition.CompositionEntity;

public class PlayQueueCompositionEntity {

    private final long itemId;
    private final int position;
    private final int shuffledPosition;

    @Embedded
    private final CompositionEntity composition;

    public PlayQueueCompositionEntity(long itemId,
                                      int position,
                                      int shuffledPosition,
                                      CompositionEntity composition) {
        this.itemId = itemId;
        this.position = position;
        this.shuffledPosition = shuffledPosition;
        this.composition = composition;
    }

    public int getPosition() {
        return position;
    }

    public long getItemId() {
        return itemId;
    }

    public CompositionEntity getComposition() {
        return composition;
    }

    public int getShuffledPosition() {
        return shuffledPosition;
    }

    @NonNull
    @Override
    public String toString() {
        return "PlayQueueCompositionEntity{" +
                "itemId=" + itemId +
                ", composition=" + composition +
                '}';
    }
}
