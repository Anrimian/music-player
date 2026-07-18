package com.github.anrimian.musicplayer.domain.models.composition;


import com.github.anrimian.musicplayer.domain.models.play_queue.PlayQueueEvent;

import javax.annotation.Nullable;

import io.reactivex.rxjava3.annotations.NonNull;

public class CurrentComposition {

    @Nullable
    private final Composition composition;
    private final boolean isPlaying;

    public CurrentComposition(PlayQueueEvent playQueueEvent, boolean isPlaying) {
        this.composition = playQueueEvent.getPlayQueueItem();
        this.isPlaying = isPlaying;
    }

    public CurrentComposition(@Nullable Composition composition, boolean isPlaying) {
        this.composition = composition;
        this.isPlaying = isPlaying;
    }

    @Nullable
    public Composition getComposition() {
        return composition;
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CurrentComposition that = (CurrentComposition) o;

        if (isPlaying != that.isPlaying) return false;
        return composition != null ? composition.equals(that.composition) : that.composition == null;
    }

    @Override
    public int hashCode() {
        int result = composition != null ? composition.hashCode() : 0;
        result = 31 * result + (isPlaying ? 1 : 0);
        return result;
    }

    @NonNull
    @Override
    public String toString() {
        return "CurrentComposition{" +
                "isPlaying=" + isPlaying +
                ", composition=" + composition +
                '}';
    }
}
