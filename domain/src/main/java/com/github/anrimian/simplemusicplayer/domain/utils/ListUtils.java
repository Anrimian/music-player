package com.github.anrimian.simplemusicplayer.domain.utils;

import java.util.ArrayList;
import java.util.List;

public class ListUtils {

    public static <T> List<T> asList(T object) {
        List<T> list = new ArrayList<>();
        list.add(object);
        return list;
    }

    public static <T, E> List<E> mapList(List<T> from, List<E> to, MapperFunction<T, E> mapper) {
        for (T t: from) {
            to.add(mapper.map(t));
        }
        return to;
    }

    public static <T, E> List<E> mapList(List<T> from, MapperFunction<T, E> mapper) {
        List<E> to = new ArrayList<>(from.size());
        for (T t: from) {
            to.add(mapper.map(t));
        }
        return to;
    }

    public interface MapperFunction<T, E> {

        E map(T t);
    }
}
