package com.github.anrimian.simplemusicplayer.domain.utils.tree;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class FileTree<T> implements Visitable<T> {

    private final LinkedList<FileTree<T>> children = new LinkedList<>();
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

    public int getChildCount() {
        return children.size();
    }

    public FileTree<T> getFirstChild() {
        return children.getFirst();
    }

    public List<T> getDirectDataList() {
        List<T> dataList = new ArrayList<>();
        for (FileTree<T> tree : children) {
            dataList.add(tree.getData());
        }
        return dataList;
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

    @Nullable
    public FileTree<T> findNodeByPath(@Nonnull String fullPath) {
        FileTree<T> target = this;
        for (String partialPath: fullPath.split("/")) {
            target = findChildNode(partialPath, target);
            if (target == null) {
                return null;
            }
        }
        return target;
    }

//    @SuppressWarnings("unchecked")
    @Nullable
    private FileTree<T> findChildNode(String partialPath, FileTree<T> target) {
        for (FileTree<T> child: target.children ) {
            if (child.path.equals(partialPath)) {
                return child;
            }
        }
        return null;
    }

    private FileTree<T> findNodeToInsert(String partialPath, FileTree<T> target) {
        FileTree<T> child = findChildNode(partialPath, target);
        if (child == null) {
            return target.addChild(new FileTree<>(partialPath));
        }
        return child;
    }

    private FileTree<T> addChild(FileTree<T> child) {
        children.add(child);
        return child;
    }

    @Override
    public String toString() {
        return "FileTree{" +
                "children=" + children +
                ", path='" + path + '\'' +
                '}';
    }
}
