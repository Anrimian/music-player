package com.github.anrimian.musicplayer.data.repositories.library.comparators;

import java.util.Comparator;

public class DescComparator<T> implements Comparator<T> {

    private final Comparator<T> comparator;

    public DescComparator(Comparator<T> comparator) {
        this.comparator = comparator;
    }

    @Override
    public int compare(T o1, T o2) {
        return comparator.compare(o2, o1);
    }
}
