package com.github.anrimian.simplemusicplayer.domain.utils;

/**
 * Created on 24.10.2017.
 */

public interface Visitor<T> {
    Visitor<T> visitTree(FileTree<T> tree);

    void visitData(FileTree<T> parent, T data);
}
