package com.github.anrimian.musicplayer.domain.utils.tree;

/**
 * Created on 24.10.2017.
 */

public interface Visitor<T> {
    Visitor<T> visitTree(FileTree<T> tree);

    void visitData(FileTree<T> parent, T data);
}
