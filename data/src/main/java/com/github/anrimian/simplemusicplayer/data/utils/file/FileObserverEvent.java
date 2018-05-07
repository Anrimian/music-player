package com.github.anrimian.simplemusicplayer.data.utils.file;

import javax.annotation.Nonnull;

public class FileObserverEvent {

    @Nonnull
    private EventType action;

    @Nonnull
    private String path;

    public FileObserverEvent(@Nonnull EventType action, @Nonnull String path) {
        this.action = action;
        this.path = path;
    }

    @Nonnull
    public EventType getAction() {
        return action;
    }

    @Nonnull
    public String getPath() {
        return path;
    }
}
