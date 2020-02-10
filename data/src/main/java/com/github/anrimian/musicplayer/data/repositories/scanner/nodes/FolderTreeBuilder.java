package com.github.anrimian.musicplayer.data.repositories.scanner.nodes;

import com.github.anrimian.musicplayer.domain.utils.ListUtils;
import com.github.anrimian.musicplayer.domain.utils.java.Mapper;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;

import static com.github.anrimian.musicplayer.domain.utils.TextUtils.isEmpty;

public class FolderTreeBuilder<M, V> {

    private final Mapper<M, String> pathFunc;
    private final Mapper<M, V> valueFunc;

    public FolderTreeBuilder(Mapper<M, String> pathFunc, Mapper<M, V> valueFunc) {
        this.pathFunc = pathFunc;
        this.valueFunc = valueFunc;
    }

    public Node<String, V> createFileTree(Observable<M> objectsObservable) {
        Node<String, V> rootNode = new Node<>(null, null);
        objectsObservable.groupBy(pathFunc::map)
                .doOnNext(group -> group.collect(ArrayList<M>::new, List::add)
                        .map(this::toNodeList)
                        .doOnSuccess(list -> addNodesToRoot(rootNode, group.getKey(), list))
                        .subscribe())
                .subscribe();
        return rootNode;
    }

    private List<Node<String, V>> toNodeList(List<M> list) {
        return ListUtils.mapList(list, obj -> new Node<>(null, valueFunc.map(obj)));
    }

    private void addNodesToRoot(Node<String, V> root,
                                String path,
                                List<Node<String, V>> nodes) {
        Node<String, V> parent = getNode(root, path);
        parent.addNodes(nodes);
    }

    private Node<String, V> getNode(Node<String, V> root, String path) {
        if (isEmpty(path)) {
            return root;
        }

        Node<String, V> target = root;
        String[] partialPaths = path.split("/");
        for (String partialPath : partialPaths) {
            Node<String, V> child = target.getChild(partialPath);
            if (child == null) {
                child = new Node<>(partialPath, null);
                target.addNode(child);
            }
            target = child;
        }
        return target;
    }

}
