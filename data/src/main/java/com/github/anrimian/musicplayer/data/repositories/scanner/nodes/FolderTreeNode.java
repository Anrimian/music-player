package com.github.anrimian.musicplayer.data.repositories.scanner.nodes;

import androidx.annotation.NonNull;

import com.github.anrimian.musicplayer.domain.utils.ListUtils;
import com.github.anrimian.musicplayer.domain.utils.java.Mapper;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;

public class FolderTreeNode<T> extends Node<String, T> {

    public FolderTreeNode(String key, T data) {
        super(key, data);
    }

    //for debug purposes
    @NonNull
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getKey());
        Node<String, T> parent = getParent();
        while (parent != null) {
            sb.append("/");
            sb.append(parent.getKey());
            parent = parent.getParent();
        }
        return sb.toString();
    }

    public static class Builder<M, V> {

        private final Mapper<M, String> pathFunc;
        private final Mapper<M, V> valueFunc;

        public Builder(Mapper<M, String> pathFunc, Mapper<M, V> valueFunc) {
            this.pathFunc = pathFunc;
            this.valueFunc = valueFunc;
        }

        public Node<String, V> createFileTree(Observable<M> objectsObservable) {
            FolderTreeNode<V> rootNode = new FolderTreeNode<>(null, null);
            objectsObservable.groupBy(pathFunc::map)
                    .doOnNext(group -> group.collect(ArrayList<M>::new, List::add)
                            .map(this::toNodeList)
                            .doOnSuccess(list -> addNodesToRoot(rootNode, group.getKey(), list))
                            .subscribe())
                    .subscribe();
            return rootNode;
        }

        private List<Node<String, V>> toNodeList(List<M> list) {
            return ListUtils.mapList(list, obj -> new FolderTreeNode<>(null, valueFunc.map(obj)));
        }

        private void addNodesToRoot(FolderTreeNode<V> root,
                                    String path,
                                    List<Node<String, V>> nodes) {
            Node<String, V> parent = getNode(root, path);
            parent.addNodes(nodes);
        }

        private Node<String, V> getNode(FolderTreeNode<V> root, String path) {
            Node<String, V> target = root;

            String[] partialPaths = path.split("/");
            for (String partialPath : partialPaths) {
                Node<String, V> child = target.getChild(partialPath);
                if (child == null) {
                    child = new FolderTreeNode<>(partialPath, null);
                    target.addNode(child);
                }
                target = child;
            }
            return target;
        }

    }

}
