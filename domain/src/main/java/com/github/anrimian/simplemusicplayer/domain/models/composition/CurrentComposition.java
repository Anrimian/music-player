package com.github.anrimian.simplemusicplayer.domain.models.composition;

import javax.annotation.Nonnull;

/**
 * Created on 01.05.2018.
 */
public class CurrentComposition {

    @Nonnull
    private Composition composition;

    private int queuePosition;
    private long playPosition;

    public CurrentComposition(@Nonnull Composition composition, int queuePosition, long playPosition) {
        this.composition = composition;
        this.queuePosition = queuePosition;
        this.playPosition = playPosition;
    }

    @Nonnull
    public Composition getComposition() {
        return composition;
    }

    public int getQueuePosition() {
        return queuePosition;
    }

    public long getPlayPosition() {
        return playPosition;
    }

    @Override
    public String toString() {
        return "CurrentComposition{" +
                "composition=" + composition +
                ", queuePosition=" + queuePosition +
                ", playPosition=" + playPosition +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CurrentComposition)) return false;

        CurrentComposition that = (CurrentComposition) o;

        return composition.equals(that.composition);
    }

    @Override
    public int hashCode() {
        return composition.hashCode();
    }
}
