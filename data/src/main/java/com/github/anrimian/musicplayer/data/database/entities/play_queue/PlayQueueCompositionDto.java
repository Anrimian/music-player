package com.github.anrimian.musicplayer.data.database.entities.play_queue;

import androidx.annotation.NonNull;
import androidx.room.Embedded;

import com.github.anrimian.musicplayer.data.database.entities.composition.CompositionEntity;

@Deprecated
public class PlayQueueCompositionDto {

    private final long itemId;
    private final int position;
    private final int shuffledPosition;

    @Embedded
    private final CompositionEntity composition;

    public PlayQueueCompositionDto(long itemId,
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
        return "PlayQueueCompositionDto{" +
                "itemId=" + itemId +
                ", composition=" + composition +
                '}';
    }
}
