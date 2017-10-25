package com.github.anrimian.simplemusicplayer.domain.utils;

import java.util.LinkedHashSet;
import java.util.Set;

import javax.annotation.Nullable;

public class FileTree<T> implements Visitable<T> {

    private final Set<FileTree> children = new LinkedHashSet<>();
    private T data;
    private final String path;

    public FileTree(String path) {
        this.path = path;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getPath() {
        return path;
    }

    @SuppressWarnings("unchecked")
    public void accept(Visitor<T> visitor) {
        visitor.visitData(this, data);

        for (FileTree child : children) {
            Visitor<T> childVisitor = visitor.visitTree(child);
            child.accept(childVisitor);
        }
    }

    public void addFile(T data, String fullPath) {
        FileTree<T> target = this;
        for (String partialPath: fullPath.split("/")) {
            target = findNodeToInsert(partialPath, target);
        }
        target.setData(data);
    }

    @SuppressWarnings("unchecked")
    private FileTree<T> findNodeToInsert(String partialPath, FileTree<T> target) {
        for (FileTree child: target.children ) {
            if (child.path.equals(partialPath)) {
                return child;
            }
        }
        return target.addChild(new FileTree<>(partialPath));
    }

    private FileTree addChild(FileTree<T> child) {
        children.add(child);
        return child;
    }


}
