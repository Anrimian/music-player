package com.github.anrimian.simplemusicplayer.domain.utils.tree;

/**
 * Created on 24.10.2017.
 */

public interface Visitable<T> {

    void accept(Visitor<T> visitor);
}
