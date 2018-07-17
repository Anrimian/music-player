package com.github.anrimian.simplemusicplayer.data.utils.folders;

import com.github.anrimian.simplemusicplayer.domain.utils.changes.Change;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.PublishSubject;

import static com.github.anrimian.simplemusicplayer.data.utils.Lists.mapList;
import static com.github.anrimian.simplemusicplayer.data.utils.rx.RxUtils.withDefaultValue;
import static com.github.anrimian.simplemusicplayer.domain.utils.changes.ChangeType.DELETED;
import static com.github.anrimian.simplemusicplayer.domain.utils.changes.ChangeType.MODIFY;
import static io.reactivex.subjects.PublishSubject.create;
import static java.util.Collections.singletonList;

public class RxNode<K> {

    private final BehaviorSubject<List<RxNode<K>>> childSubject = BehaviorSubject.create();
    private final PublishSubject<Change<NodeData>> selfChangeSubject = PublishSubject.create();

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
    public List<RxNode<K>> getNodes() {
        return new ArrayList<>(nodes.values());
    }

    @Nonnull
    public K getKey() {
        return key;
    }

    public NodeData getData() {
        return data;
    }

    public Observable<List<RxNode<K>>> getChildObservable() {
        return withDefaultValue(childSubject, this::getNodes);
    }

    public PublishSubject<Change<NodeData>> getSelfChangeObservable() {
        return selfChangeSubject;
    }

    public void addNodes(List<RxNode<K>> newNodes) {
        List<RxNode<K>> addedNodes = new ArrayList<>();
        List<RxNode<K>> modifiedNodes = new ArrayList<>();
        for (RxNode<K> newNode : newNodes) {
            newNode.parent = this;
            RxNode<K> previous = nodes.put(newNode.getKey(), newNode);
            if (previous == null) {
                addedNodes.add(newNode);
            } else {
                modifiedNodes.add(newNode);
            }
        }

        if (!addedNodes.isEmpty()) {
            notifyNodesAdded(mapList(addedNodes, new ArrayList<>(), RxNode::getData));
        }
        if (!modifiedNodes.isEmpty() || !addedNodes.isEmpty()) {
            notifyChildrenChanged();
        }
    }

    public void addNode(RxNode<K> node) {
        node.parent = this;
        RxNode<K> previous = nodes.put(node.getKey(), node);

        if (previous == null) {
            notifyNodesAdded(singletonList(node.getData()));
        }

        notifyChildrenChanged();
    }

    public void removeNodes(List<K> keys) {
        List<RxNode<K>> removedNodes = new ArrayList<>();
        for (K key: keys) {
            RxNode<K> removedNode = nodes.remove(key);
            if (removedNode != null) {
                removedNodes.add(removedNode);
            }
        }
        if (!removedNodes.isEmpty()) {
            for (RxNode<K> removedNode: removedNodes){
                removedNode.notifySelfRemoved();
            }

            notifyNodesRemoved(mapList(removedNodes, RxNode::getData));
            notifyChildrenChanged();
        }
    }

    public void removeNode(K key) {
        RxNode<K> removedNode = nodes.remove(key);
        if (removedNode != null) {
            removedNode.notifySelfRemoved();
            notifyChildrenChanged();
            notifyNodesRemoved(singletonList(removedNode.getData()));
        }
    }

    @Nullable
    public RxNode<K> getChild(K key) {
        return nodes.get(key);
    }

    public void updateNode(K key, NodeData nodeData) {
        RxNode<K> node = nodes.get(key);
        if (node != null) {
            node.data = nodeData;
        } else {
            addNode(new RxNode<>(key, nodeData));
        }

        notifyChildrenChanged();
    }

    private void notifySelfRemoved() {
        selfChangeSubject.onNext(new Change<>(DELETED, data));
    }

    private void notifyNodesRemoved(List<NodeData> data) {
        if (this.data != null) {
            boolean updated = this.data.onNodesRemoved(data, mapList(getNodes(), RxNode::getData));
            if (updated) {
                selfChangeSubject.onNext(new Change<>(MODIFY, this.data));

                RxNode<K> parent = getParent();
                if (parent != null) {
                    parent.notifyNodesRemoved(data);
                }
            }
        }
    }

    private void notifyNodesAdded(List<NodeData> data) {
        if (this.data != null) {
            boolean updated = this.data.onNodesAdded(data);
            if (updated) {
                selfChangeSubject.onNext(new Change<>(MODIFY, this.data));

                RxNode<K> parent = getParent();
                if (parent != null) {
                    parent.notifyNodesAdded(data);
                    parent.notifyChildrenChanged();
                }
            }
        }
    }

    private void notifyChildrenChanged() {
        childSubject.onNext(getNodes());
    }

    @Override
    public String toString() {
        return "RxNode{" +
                "key=" + key +
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
