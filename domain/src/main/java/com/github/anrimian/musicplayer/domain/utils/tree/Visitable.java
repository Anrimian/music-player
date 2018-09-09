package com.github.anrimian.musicplayer.domain.utils.tree;

/**
 * Created on 24.10.2017.
 */

public interface Visitable<T> {

    void accept(Visitor<T> visitor);
}
