package com.github.anrimian.simplemusicplayer.domain.utils.tree;

import java.util.LinkedList;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class FileTree<T> implements Visitable<T> {

    private final LinkedList<FileTree<T>> children = new LinkedList<>();
    private T data;
    private final String path;
    private final String fullPath;

    public FileTree(String path, String fullPath) {
        this.path = path;
        this.fullPath = fullPath;
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

    public LinkedList<FileTree<T>> getChildren() {
        return children;
    }

    public String getFullPath() {
        return fullPath;
    }

    public boolean isEmpty() {
        return children.isEmpty();
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
    public FileTree<T> findNodeByPath(@Nullable String fullPath) {
        if (fullPath == null) {
            return this;
        }
        FileTree<T> target = this;
        FileTree<T> found = null;
        for (String partialPath: fullPath.split("/")) {
            found = findChildNode(partialPath, target);
            if (found != null) {
                target = found;
            }
        }
        return found;
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
            StringBuilder sbFullPath = new StringBuilder();
            String parentPath = target.getFullPath();
            if (parentPath != null) {
                sbFullPath.append(parentPath);
                sbFullPath.append("/");
            }
            sbFullPath.append(partialPath);
            return target.addChild(new FileTree<>(partialPath, sbFullPath.toString()));
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
                "path='" + path + '\'' +
                "fullPath='" + fullPath + '\'' +
                "\nchildren=" + children +
                '}';
    }

}
