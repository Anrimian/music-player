package com.github.anrimian.musicplayer.domain.utils.changes.map;

public interface EqualComparator<T, K> {

    boolean areItemsTheSame(T first, K second);
}
