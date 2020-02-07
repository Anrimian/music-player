package com.github.anrimian.musicplayer.data.repositories.scanner.nodes;

import androidx.annotation.NonNull;

public class AddedNode {

    private final Long folderDbId;
    private final Node<String, Long> node;

    public AddedNode(Long folderDbId, Node<String, Long> node) {
        this.folderDbId = folderDbId;
        this.node = node;
    }

    public Long getFolderDbId() {
        return folderDbId;
    }

    public Node<String, Long> getNode() {
        return node;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AddedNode node1 = (AddedNode) o;

        if (folderDbId != null ? !folderDbId.equals(node1.folderDbId) : node1.folderDbId != null)
            return false;
        return node.equals(node1.node);
    }

    @Override
    public int hashCode() {
        int result = folderDbId != null ? folderDbId.hashCode() : 0;
        result = 31 * result + node.hashCode();
        return result;
    }

    @NonNull
    @Override
    public String toString() {
        return "AddedNode{" +
                "folderDbId=" + folderDbId +
                ", node=" + node +
                '}';
    }
}