package com.github.anrimian.musicplayer.domain.models.player.events;

import com.github.anrimian.musicplayer.domain.models.composition.Composition;

public class FinishedEvent implements PlayerEvent {

    private final Composition composition;

    public FinishedEvent(Composition composition) {
        this.composition = composition;
    }

    public Composition getComposition() {
        return composition;
    }

    @Override
    public String toString() {
        return "FinishedEvent{" +
                "composition=" + composition +
                '}';
    }
}
