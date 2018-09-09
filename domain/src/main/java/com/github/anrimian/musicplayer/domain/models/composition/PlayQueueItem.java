package com.github.anrimian.musicplayer.domain.models.composition;

public class PlayQueueItem {

    private long id;
    private Composition composition;

    public PlayQueueItem(long id, Composition composition) {
        this.id = id;
        this.composition = composition;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Composition getComposition() {
        return composition;
    }

    public void setComposition(Composition composition) {
        this.composition = composition;
    }
}
