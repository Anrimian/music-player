package com.github.anrimian.simplemusicplayer.data.database.models;

import android.arch.persistence.room.Embedded;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import static com.github.anrimian.simplemusicplayer.data.database.AppDatabase.CURRENT_PLAY_LIST;

/**
 * Created on 23.11.2017.
 */

@Entity(tableName = CURRENT_PLAY_LIST)
public class CompositionItemEntity {

    @PrimaryKey(autoGenerate = true)
    private long id;

    @Embedded(prefix = "composition_")
    private CompositionEntity composition;

    private int initialPosition;
    private int shuffledPosition;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setComposition(CompositionEntity composition) {
        this.composition = composition;
    }

    public CompositionEntity getComposition() {
        return composition;
    }

    public int getInitialPosition() {
        return initialPosition;
    }

    public void setInitialPosition(int initialPosition) {
        this.initialPosition = initialPosition;
    }

    public int getShuffledPosition() {
        return shuffledPosition;
    }

    public void setShuffledPosition(int shuffledPosition) {
        this.shuffledPosition = shuffledPosition;
    }
}
