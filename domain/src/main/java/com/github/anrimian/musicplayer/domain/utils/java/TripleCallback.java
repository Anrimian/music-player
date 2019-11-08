package com.github.anrimian.musicplayer.domain.utils.java;

public interface TripleCallback<T, P, M> {

    void call(T obj1, P obj2, M obj3);
}
