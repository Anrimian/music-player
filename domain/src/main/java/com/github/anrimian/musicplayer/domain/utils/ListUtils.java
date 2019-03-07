package com.github.anrimian.musicplayer.domain.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ListUtils {

    public static <T> List<T> asList(T object) {
        List<T> list = new ArrayList<>();
        list.add(object);
        return list;
    }

    public static <T> List<T> asList(T... objects) {
        return new ArrayList<>(Arrays.asList(objects));
    }

    public static <T, E> List<E> mapList(List<T> from, List<E> to, MapperFunction<T, E> mapper) {
        for (T t: from) {
            to.add(mapper.map(t));
        }
        return to;
    }

    public static <K, E, T> List<T> mapToList(Map<K, E> from, List<T> to, MapperFunction<E, T> mapper) {
        for (E t: from.values()) {
            to.add(mapper.map(t));
        }
        return to;
    }

    public static <K, E, T> List<T> mapToList(Map<K, E> from, MapperFunction<E, T> mapper) {
        return mapToList(from, new ArrayList<>(from.size()), mapper);
    }

    public static <T, E> List<E> mapList(List<T> from, MapperFunction<T, E> mapper) {
        return mapList(from, new ArrayList<>(from.size()), mapper);
    }

    public static <K, V> void update(Map<K, V> map, K key, V value) {
        if (map.containsKey(key)) {
            map.put(key, value);
        }
    }

    public static void safeSwap(List<?> list, int i, int j) {
        if (i >= list.size() || j >= list.size()) {
            return;
        }
        Collections.swap(list, i, j);
    }

    public interface MapperFunction<T, E> {

        E map(T t);
    }
}
