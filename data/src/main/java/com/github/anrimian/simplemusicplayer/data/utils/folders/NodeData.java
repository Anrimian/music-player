package com.github.anrimian.simplemusicplayer.data.utils.folders;

import java.util.List;

import javax.annotation.Nullable;

public abstract class NodeData {

    /**
     *
     * @param nodes added data
     * @return was updated or not
     */
    public boolean onNodesAdded(List<NodeData> nodes) {
        return false;
    }

    public boolean onNodesRemoved(List<NodeData> nodes) {
        return false;
    }
}
