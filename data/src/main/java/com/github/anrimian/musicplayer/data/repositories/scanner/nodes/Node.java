package com.github.anrimian.musicplayer.data.repositories.scanner.nodes;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class Node<K, V> {

    private final LinkedHashMap<K, Node<K, V>> nodes = new LinkedHashMap<>();

    private K key;
    private V data;

    @Nullable
    private Node<K, V> parent;

    public Node(K key, V data) {
        this.key = key;
        this.data = data;
    }

    @Nullable
    public Node<K, V> getParent() {
        return parent;
    }

    @Nonnull
    public List<Node<K, V>> getNodes() {
        return new ArrayList<>(nodes.values());
    }

    @Nonnull
    public K getKey() {
        return key;
    }

    public V getData() {
        return data;
    }

    public void addNodes(List<Node<K, V>> newNodes) {
        for (Node<K, V> newNode : newNodes) {
            newNode.parent = this;
            nodes.put(newNode.getKey(), newNode);
        }
    }

    public void addNode(Node<K, V> node) {
        node.parent = this;
        Node<K, V> previous = nodes.put(node.getKey(), node);
    }

    public void removeNodes(List<K> keys) {
        List<Node<K, V>> removedNodes = new ArrayList<>();
        for (K key: keys) {
            Node<K, V> removedNode = nodes.remove(key);
            if (removedNode != null) {
                removedNodes.add(removedNode);
            }
        }
        if (!removedNodes.isEmpty()) {
            clearEmptyNodeIfNeed(this);
        }
    }

    public void removeNode(K key) {
        Node<K, V> removedNode = nodes.remove(key);
        if (removedNode != null) {
            clearEmptyNodeIfNeed(this);
        }
    }

    @Nullable
    public Node<K, V> getChild(K key) {
        return nodes.get(key);
    }

    public void updateNode(K key, V nodeData) {
        Node<K, V> node = nodes.get(key);
        if (node != null) {
            node.data = nodeData;
        } else {
            addNode(new Node<>(key, nodeData));
        }
    }

    public void updateKey(K key) {
        if (parent != null) {
            parent.updateChildKey(this, key);
        }
        this.key = key;
    }

    public void updateData(V nodeData) {
        this.data = nodeData;
    }

    private void updateChildKey(Node<K, V> node, K newKey) {
        nodes.remove(node.getKey());
        nodes.put(newKey, node);
    }

    private void clearEmptyNodeIfNeed(Node<K, V> node) {
        if (node.getNodes().isEmpty()) {
            Node<K, V> parent = node.getParent();
            if (parent != null) {
                parent.removeNode(node.getKey());
                clearEmptyNodeIfNeed(parent);
            }
        }
    }

    @NonNull
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

        Node<?, ?> node = (Node<?, ?>) o;

        return key.equals(node.key);
    }

    @Override
    public int hashCode() {
        return key.hashCode();
    }
}
