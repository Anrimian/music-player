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

public class RxNode<K, T> {

    private final PublishSubject<Change<List<RxNode<K, T>>>> childChangeSubject = create();
//    private final PublishSubject<Change<RxNode<K, T>>> selfChangeSubject = create();

    private final LinkedHashMap<K, RxNode<K, T>> nodes = new LinkedHashMap<>();

    @Nonnull
    private K key;
    private T data;

    @Nullable
    private RxNode<K, T> parent;

    public RxNode(@Nonnull K key, T data) {
        this.key = key;
        this.data = data;
    }

    @Nullable
    public RxNode<K, T> getParent() {
        return parent;
    }

    @Nonnull
    public Collection<RxNode<K, T>> getNodes() {
        return nodes.values();
    }

    @Nonnull
    public K getKey() {
        return key;
    }

    public T getData() {
        return data;
    }

    public Observable<Change<List<RxNode<K, T>>>> getChildChangeObservable() {
        return childChangeSubject;
    }

    public void addNode(RxNode<K, T> node) {
        node.parent = this;
        nodes.put(node.getKey(), node);
//        selfChangeSubject.onNext(new Change<>(MODIFY, this));
        childChangeSubject.onNext(new Change<>(ADDED, singletonList(node)));
//        notifyNodeUpdated(this);
    }

    public void setData(T data) {
        this.data = data;
        notifyNodeUpdated(this);
    }

    public void removeNode(K key) {
        RxNode<K, T> node = getChild(key);
        if (node != null) {
            removeNode(node);
        }
    }

    public void removeNode(RxNode<K, T> node) {
        nodes.remove(node.getKey());
        childChangeSubject.onNext(new Change<>(DELETED, singletonList(node)));
        notifyNodeUpdated(this);
    }

    @Nullable
    public RxNode<K, T> getChild(K key) {
        return nodes.get(key);
    }

    private void notifyNodeUpdated(RxNode<K, T> node) {
        childChangeSubject.onNext(new Change<>(MODIFY, singletonList(node)));
        RxNode<K, T> parent = getParent();
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

        RxNode<?, ?> rxNode = (RxNode<?, ?>) o;

        return key.equals(rxNode.key);
    }

    @Override
    public int hashCode() {
        return key.hashCode();
    }
}
