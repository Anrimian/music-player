package com.github.anrimian.simplemusicplayer.data.utils.folders;

import java.util.List;

import javax.annotation.Nullable;

public abstract class NodeData {

    /**
     *
     * @param addedNodes added data
     * @return was updated or not
     */
    public boolean onNodesAdded(List<NodeData> addedNodes) {
        return false;
    }

    /**
     *
     * @param removedNodes added data
     * @param allNodes all data
     * @return was updated or not
     */
    public boolean onNodesRemoved(List<NodeData> removedNodes, List<NodeData> allNodes) {
        return false;
    }

    /**
     *
     * @param removedNodes added data
     * @param allNodes all data
     * @return was updated or not
     */
    public boolean onNodesChanged(List<NodeData> removedNodes, List<NodeData> allNodes) {
        return false;
    }
}
