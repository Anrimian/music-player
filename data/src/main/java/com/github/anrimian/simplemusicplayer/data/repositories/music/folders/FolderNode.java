package com.github.anrimian.simplemusicplayer.data.repositories.music.folders;

import com.github.anrimian.simplemusicplayer.data.utils.folders.NodeData;

public class FolderNode implements NodeData {

    private String fullPath;
    private int compositionsCount;

    FolderNode(String fullPath) {
        this.fullPath = fullPath;
    }

    @Override
    public void onNodeAdded(NodeData nodeData) {
        if (nodeData instanceof CompositionNode) {
            compositionsCount++;
        }
    }

    public String getFullPath() {
        return fullPath;
    }

    public int getCompositionsCount() {
        return compositionsCount;
    }

    @Override
    public String toString() {
        return "FolderNode{" +
                "fullPath='" + fullPath + '\'' +
                ", compositionsCount=" + compositionsCount +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FolderNode that = (FolderNode) o;

        return fullPath != null ? fullPath.equals(that.fullPath) : that.fullPath == null;
    }

    @Override
    public int hashCode() {
        return fullPath != null ? fullPath.hashCode() : 0;
    }
}
