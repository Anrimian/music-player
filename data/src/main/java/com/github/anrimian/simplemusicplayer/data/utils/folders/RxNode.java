package com.github.anrimian.simplemusicplayer.data.utils.folders;

import java.util.LinkedList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import by.mrsoft.mrdoc.domain.models.common.change.Change;
import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;

import static by.mrsoft.mrdoc.domain.models.common.change.ChangeType.DELETED;
import static by.mrsoft.mrdoc.domain.models.common.change.ChangeType.MODIFY;
import static by.mrsoft.mrdoc.domain.models.common.change.ChangeType.NEW;

public class RxNode<K, T> {

    private final PublishSubject<Change<RxNode<K, T>>> changeSubject = PublishSubject.create();
    private final List<RxNode<K, T>> nodes = new LinkedList<>();

    @Nonnull
    private K key;
    private T data;

    @Nullable
    private RxNode<K, T> parent;

    public RxNode(@Nonnull K key, T data, @Nullable RxNode<K, T> parent) {
        this.key = key;
        this.data = data;
        this.parent = parent;
    }

    @Nullable
    public RxNode<K, T> getParent() {
        return parent;
    }

    @Nonnull
    public List<RxNode<K, T>> getNodes() {
        return nodes;
    }

    @Nonnull
    public K getKey() {
        return key;
    }

    public T getData() {
        return data;
    }

    public Observable<Change<RxNode<K, T>>> getChangeObservable() {
        return changeSubject;
    }

    public void addNode(RxNode<K, T> node) {
        nodes.add(node);
        changeSubject.onNext(new Change<>(NEW, node));
        updateBranch();
    }

    public void setData(T data) {
        this.data = data;
        notifyNodeUpdated(this);
    }

    public void removeNode(K key) {
        RxNode<K, T> node = findChild(key);
        if (node != null) {
            removeNode(node);
        }
    }

    public void removeNode(RxNode<K, T> node) {
        nodes.remove(node);
        changeSubject.onNext(new Change<>(DELETED, node));
        updateBranch();
    }

    @Nullable
    public RxNode<K, T> findChild(K key) {
        for (RxNode<K, T> node: nodes) {
            if (node.getKey().equals(key)) {
                return node;
            }
        }
        return null;
    }

    private void notifyNodeUpdated(RxNode<K, T> node) {
        changeSubject.onNext(new Change<>(MODIFY, node));
        RxNode<K, T> parent = getParent();
        if (parent != null) {
            parent.notifyNodeUpdated(this);
        }
    }

    private void updateBranch() {
        changeSubject.onNext(new Change<>(MODIFY, this));
        RxNode<K, T> parent = getParent();
        if (parent != null) {
            parent.updateBranch();
        }
    }


}
