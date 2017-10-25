package com.github.anrimian.simplemusicplayer.domain.utils;

import com.github.anrimian.simplemusicplayer.domain.models.MusicFileSource;

/**
 * Created on 25.10.2017.
 */

public class PrintIndentedVisitor implements Visitor<MusicFileSource> {

    private final int indent;

    public PrintIndentedVisitor(int indent) {
        this.indent = indent;
    }

    public Visitor<MusicFileSource> visitTree(FileTree<MusicFileSource> tree) {
        return new PrintIndentedVisitor(indent + 2);
    }

    public void visitData(FileTree<MusicFileSource> parent, MusicFileSource data) {
        for (int i = 0; i < indent; i++) { // TODO: naive implementation
            System.out.print(" ");
        }

        System.out.println(parent.getPath());
    }
}
