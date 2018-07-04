package com.github.anrimian.simplemusicplayer.domain.models.composition;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Created on 01.05.2018.
 */
public class CompositionEvent {

    @Nullable
    private Composition composition;

    private int queuePosition;
    private long playPosition;

    public CompositionEvent(@Nullable Composition composition, int queuePosition, long playPosition) {
        this.composition = composition;
        this.queuePosition = queuePosition;
        this.playPosition = playPosition;
    }

    public CompositionEvent() {
    }

    public CompositionEvent(Composition changedComposition, int position) {
        this(changedComposition, position, 0);
    }

    @Nullable
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
        return "CompositionEvent{" +
                "composition=" + composition +
                ", queuePosition=" + queuePosition +
                ", playPosition=" + playPosition +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CompositionEvent)) return false;

        CompositionEvent that = (CompositionEvent) o;

        return composition.equals(that.composition);
    }

    @Override
    public int hashCode() {
        return composition.hashCode();
    }
}
