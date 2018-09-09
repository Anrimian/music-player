package com.github.anrimian.musicplayer.data.repositories.music.folders;

import com.github.anrimian.musicplayer.data.utils.folders.NodeData;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.utils.Objects;

import java.util.Date;
import java.util.List;

import javax.annotation.Nullable;

public class FolderNode extends NodeData {

    private final String fullPath;
    private int compositionsCount;

    @Nullable
    private Date latestCreateDate;

    @Nullable
    private Date earliestCreateDate;

    FolderNode(String fullPath) {
        this.fullPath = fullPath;
    }

    @Override
    public boolean onNodesAdded(List<NodeData> nodes) {
        for (NodeData nodeData: nodes) {
            if (nodeData instanceof CompositionNode) {
                compositionsCount++;
            }
            recalculateDate(nodeData);
        }
        return true;
    }

    @Override
    public boolean onNodesRemoved(List<NodeData> nodes, List<NodeData> allNodes) {
        boolean updated = false;
        boolean dateChanged = false;

        for (NodeData nodeData: nodes) {
            if (nodeData instanceof CompositionNode) {
                compositionsCount--;
                updated = true;
            }

            if (!allNodes.isEmpty()) {
                Date date = getDateFromNode(nodeData, true);
                if (Objects.equals(latestCreateDate, date)) {
                    dateChanged = true;
                    latestCreateDate = null;
                }
                date = getDateFromNode(nodeData, true);
                if (Objects.equals(earliestCreateDate, date)) {
                    dateChanged = true;
                    earliestCreateDate = null;
                }
            }
        }
        if (dateChanged) {
            for (NodeData nodeData: allNodes) {
                recalculateDate(nodeData);
            }
        }

        return updated || dateChanged;
    }

    @Override
    public boolean onNodesChanged(List<NodeData> nodes, List<NodeData> allNodes) {
        if (allNodes.isEmpty()) {
            return false;
        } else {
            latestCreateDate = null;
            earliestCreateDate = null;
            for (NodeData nodeData : allNodes) {
                recalculateDate(nodeData);
            }
            return true;
        }
    }

    public String getFullPath() {
        return fullPath;
    }

    public int getCompositionsCount() {
        return compositionsCount;
    }

    @Nullable
    public Date getLatestCreateDate() {
        return latestCreateDate;
    }

    @Nullable
    public Date getEarliestCreateDate() {
        return earliestCreateDate;
    }

    @Override
    public String toString() {
        return "FolderNode{" +
                "fullPath='" + fullPath + '\'' +
                ", compositionsCount=" + compositionsCount +
                ", latestCreateDate=" + latestCreateDate +
                ", earliestCreateDate=" + earliestCreateDate +
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

    private void recalculateDate(NodeData nodeData) {
        Date date = getDateFromNode(nodeData, true);
        if (date != null) {
            if (latestCreateDate == null || date.compareTo(latestCreateDate) > 0) {
                latestCreateDate = date;
            }
        }
        date = getDateFromNode(nodeData, false);
        if (date != null) {
            if (earliestCreateDate == null || date.compareTo(earliestCreateDate) < 0) {
                earliestCreateDate = date;
            }
        }
    }

    private Date getDateFromNode(NodeData nodeData, boolean latestDate) {
        if (nodeData instanceof CompositionNode) {
            Composition composition = ((CompositionNode) nodeData).getComposition();
            return composition.getDateAdded();
        } else if (nodeData instanceof FolderNode) {
            FolderNode folderNode = (FolderNode) nodeData;
            return latestDate? folderNode.getLatestCreateDate(): folderNode.getEarliestCreateDate();
        }
        return null;
    }
}
