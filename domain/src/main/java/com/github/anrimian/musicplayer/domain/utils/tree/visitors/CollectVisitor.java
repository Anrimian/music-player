package com.github.anrimian.musicplayer.domain.utils.tree.visitors;

import com.github.anrimian.musicplayer.domain.utils.tree.FileTree;
import com.github.anrimian.musicplayer.domain.utils.tree.Visitor;

import java.util.List;

/**
 * Created on 26.10.2017.
 */

public class CollectVisitor<T> implements Visitor<T> {

    private List<T> collection;

    public CollectVisitor(List<T> collection) {
        this.collection = collection;
    }

    @Override
    public Visitor<T> visitTree(FileTree<T> tree) {
        return new CollectVisitor<>(collection);
    }

    @Override
    public void visitData(FileTree<T> parent, T data) {
        if (data != null) {
            collection.add(data);
        }
    }
}
