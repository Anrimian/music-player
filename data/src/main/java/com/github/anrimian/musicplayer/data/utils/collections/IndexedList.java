package com.github.anrimian.musicplayer.data.utils.collections;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IndexedList<T> {

    private final List<T> list;
    private final Map<T, Integer> map;

    public IndexedList(List<T> list) {
        this.list = list;
        map = new HashMap<>();
        for (int i = 0; i < list.size(); i++) {
            map.put(list.get(i), i);
        }
    }

    public Integer indexOf(T item) {
        return map.get(item);
    }

    public int size() {
        return list.size();
    }

    public T get(int index) {
        return list.get(index);
    }

    public boolean isEmpty() {
        return list.isEmpty();
    }

    public boolean contains(T item) {
        return map.containsKey(item);
    }
}
