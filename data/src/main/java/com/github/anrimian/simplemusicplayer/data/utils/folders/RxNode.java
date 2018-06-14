package com.github.anrimian.simplemusicplayer.data.utils.folders;

import com.github.anrimian.simplemusicplayer.domain.utils.changes.Change;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;

import static com.github.anrimian.simplemusicplayer.domain.utils.changes.ChangeType.ADDED;
import static com.github.anrimian.simplemusicplayer.domain.utils.changes.ChangeType.DELETED;
import static com.github.anrimian.simplemusicplayer.domain.utils.changes.ChangeType.MODIFY;
import static io.reactivex.subjects.PublishSubject.create;
import static java.util.Collections.singletonList;

public class RxNode<K> {

    private final PublishSubject<Change<List<RxNode<K>>>> childChangeSubject = create();
//    private final PublishSubject<Change<RxNode<K>>> selfChangeSubject = create();

    private final LinkedHashMap<K, RxNode<K>> nodes = new LinkedHashMap<>();

    private K key;
    private NodeData data;

    @Nullable
    private RxNode<K> parent;

    public RxNode(K key, NodeData data) {
        this.key = key;
        this.data = data;
    }

    @Nullable
    public RxNode<K> getParent() {
        return parent;
    }

    @Nonnull
    public Collection<RxNode<K>> getNodes() {
        return nodes.values();
    }

    @Nonnull
    public K getKey() {
        return key;
    }

    public NodeData getData() {
        return data;
    }

    public Observable<Change<List<RxNode<K>>>> getChildChangeObservable() {
        return childChangeSubject;
    }

    public void addNode(RxNode<K> node) {
        node.parent = this;
        nodes.put(node.getKey(), node);
//        selfChangeSubject.onNext(new Change<>(MODIFYhis));
        childChangeSubject.onNext(new Change<>(ADDED, singletonList(node)));

        notifyNodeAdded(node.getData());
//        notifyNodeUpdated(this);
    }

    public void removeNode(K key) {
        RxNode<K> node = getChild(key);
        if (node != null) {
            removeNode(node);
        }
    }

    public void removeNode(RxNode<K> node) {
        nodes.remove(node.getKey());
        childChangeSubject.onNext(new Change<>(DELETED, singletonList(node)));
        notifyNodeUpdated(this);
    }

    @Nullable
    public RxNode<K> getChild(K key) {
        return nodes.get(key);
    }

    private void notifyNodeAdded(NodeData data) {
        if (this.data != null) {
            this.data.onNodeAdded(data);
        }

        RxNode<K> parent = getParent();
        if (parent != null) {
            parent.notifyNodeAdded(data);
        }
    }

    private void notifyNodeUpdated(RxNode<K> node) {
        childChangeSubject.onNext(new Change<>(MODIFY, singletonList(node)));
        RxNode<K> parent = getParent();
        if (parent != null) {
            parent.notifyNodeUpdated(this);
        }
    }

    @Override
    public String toString() {
        return "RxNode{" +
                "nodes=" + nodes +
                ", key=" + key +
                ", data=" + data +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RxNode<?> rxNode = (RxNode<?>) o;

        return key.equals(rxNode.key);
    }

    @Override
    public int hashCode() {
        return key.hashCode();
    }
}
