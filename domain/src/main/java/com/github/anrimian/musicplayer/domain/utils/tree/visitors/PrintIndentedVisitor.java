package com.github.anrimian.musicplayer.domain.utils.tree.visitors;

import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.utils.tree.FileTree;
import com.github.anrimian.musicplayer.domain.utils.tree.Visitor;

/**
 * Created on 25.10.2017.
 */

public class PrintIndentedVisitor implements Visitor<Composition> {

    private final int indent;

    public PrintIndentedVisitor(int indent) {
        this.indent = indent;
    }

    public Visitor<Composition> visitTree(FileTree<Composition> tree) {
        return new PrintIndentedVisitor(indent + 2);
    }

    public void visitData(FileTree<Composition> parent, Composition data) {
        for (int i = 0; i < indent; i++) {
            System.out.print(" ");
        }

        System.out.println(parent.getPath());
    }
}
