package com.github.anrimian.musicplayer.domain.models.composition;

import javax.annotation.Nullable;

/**
 * Created on 01.05.2018.
 */
public class CompositionEvent {

    @Nullable
    private Composition composition;

    private long trackPosition;

    public CompositionEvent(@Nullable Composition composition, long trackPosition) {
        this.composition = composition;
        this.trackPosition = trackPosition;
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

    public long getTrackPosition() {
        return trackPosition;
    }

    @Override
    public String toString() {
        return "CompositionEvent{" +
                "composition=" + composition +
                ", trackPosition=" + trackPosition +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CompositionEvent that = (CompositionEvent) o;

        return composition != null ? composition.equals(that.composition) : that.composition == null;
    }

    @Override
    public int hashCode() {
        return composition != null ? composition.hashCode() : 0;
    }
}
