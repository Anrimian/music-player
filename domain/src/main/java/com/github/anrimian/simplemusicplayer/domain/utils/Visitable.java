package com.github.anrimian.simplemusicplayer.domain.utils;

/**
 * Created on 24.10.2017.
 */

public interface Visitable<T> {

    void accept(Visitor<T> visitor);
}
