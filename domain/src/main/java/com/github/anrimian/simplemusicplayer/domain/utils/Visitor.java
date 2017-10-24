package com.github.anrimian.simplemusicplayer.domain.utils;

/**
 * Created on 24.10.2017.
 */

public interface Visitor<T> {
    Visitor<T> visitTree(Tree<T> tree);

    void visitData(Tree<T> parent, T data);
}
