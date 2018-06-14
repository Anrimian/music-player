package com.github.anrimian.simplemusicplayer.data.repositories.music.folders;

import com.github.anrimian.simplemusicplayer.data.utils.folders.NodeData;
import com.github.anrimian.simplemusicplayer.domain.models.composition.Composition;

import javax.annotation.Nullable;

public class CompositionNode implements NodeData {

    private Composition composition;

    CompositionNode(Composition composition) {
        this.composition = composition;
    }

    public Composition getComposition() {
        return composition;
    }

    @Override
    public void onNodeAdded(NodeData nodeData) {

    }

    @Override
    public String toString() {
        return "CompositionNode{" +
                "composition=" + composition +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CompositionNode that = (CompositionNode) o;

        return composition != null ? composition.equals(that.composition) : that.composition == null;
    }

    @Override
    public int hashCode() {
        return composition != null ? composition.hashCode() : 0;
    }
}
