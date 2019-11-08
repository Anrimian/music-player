package com.github.anrimian.musicplayer.domain.utils.changes.map;

public interface ChangeInspector<T> {

    boolean hasChanges(T first, T second);
}
