package com.github.anrimian.musicplayer.domain.utils.java;

public interface ChangeInspector<T> {

    boolean hasChanges(T first, T second);
}
