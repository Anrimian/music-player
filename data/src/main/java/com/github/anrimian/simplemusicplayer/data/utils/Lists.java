package com.github.anrimian.simplemusicplayer.data.utils;

import java.util.ArrayList;
import java.util.List;

public class Lists {

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
