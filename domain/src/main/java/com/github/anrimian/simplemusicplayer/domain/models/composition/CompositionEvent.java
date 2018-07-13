package com.github.anrimian.simplemusicplayer.domain.models.composition;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Created on 01.05.2018.
 */
public class CompositionEvent {

    @Nullable
    private Composition composition;

    private long playPosition;

    public CompositionEvent(@Nullable Composition composition, long playPosition) {
        this.composition = composition;
        this.playPosition = playPosition;
    }

    public CompositionEvent() {
    }

    public CompositionEvent(Composition composition) {
        this(composition, 0);
    }

    @Nullable
    public Composition getComposition() {
        return composition;
    }

    public long getPlayPosition() {
        return playPosition;
    }

    @Override
    public String toString() {
        return "CompositionEvent{" +
                "composition=" + composition +
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
