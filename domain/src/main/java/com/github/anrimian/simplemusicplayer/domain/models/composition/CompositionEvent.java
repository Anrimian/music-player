package com.github.anrimian.simplemusicplayer.domain.models.composition;

import javax.annotation.Nullable;

/**
 * Created on 24.06.2018.
 */
public class CompositionEvent {

    @Nullable
    private CurrentComposition currentComposition;

    public CompositionEvent(@Nullable CurrentComposition currentComposition) {
        this.currentComposition = currentComposition;
    }

    @Nullable
    public CurrentComposition getCurrentComposition() {
        return currentComposition;
    }

    @Override
    public String toString() {
        return "CompositionEvent{" +
                "currentComposition=" + currentComposition +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CompositionEvent)) return false;

        CompositionEvent that = (CompositionEvent) o;

        return currentComposition != null ? currentComposition.equals(that.currentComposition) : that.currentComposition == null;
    }

    @Override
    public int hashCode() {
        return currentComposition != null ? currentComposition.hashCode() : 0;
    }
}
