package com.github.anrimian.musicplayer.data.repositories.library.folders;

import com.github.anrimian.musicplayer.data.utils.folders.NodeData;
import com.github.anrimian.musicplayer.data.utils.folders.RxNode;
import com.github.anrimian.musicplayer.domain.models.composition.folders.FileSource;
import com.github.anrimian.musicplayer.domain.models.composition.folders.Folder;
import com.github.anrimian.musicplayer.domain.models.composition.folders.FolderFileSource;
import com.github.anrimian.musicplayer.domain.models.composition.folders.MusicFileSource;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

/**
 * Created on 13.08.2018.
 */
class FolderMapper {

    @Nonnull
    Folder toFolder(@Nonnull RxNode<String> node) {
        return new Folder(
                node.getChildObservable().map(this::toFileSources),
                node.getSelfChangeObservable().map(this::toFileSource),
                node.getSelfDeleteObservable()
        );
    }

    private List<FileSource> toFileSources(List<RxNode<String>> nodes) {
        List<FileSource> fileSources = new ArrayList<>(nodes.size());
        for (RxNode<String> changedNode : nodes) {
            fileSources.add(toFileSource(changedNode.getData()));
        }
        return fileSources;
    }

    private FileSource toFileSource(NodeData nodeData) {
        if (nodeData instanceof CompositionNode) {
            return new MusicFileSource(((CompositionNode) nodeData).getComposition());
        } else if (nodeData instanceof FolderNode){
            FolderNode node = (FolderNode) nodeData;
            return new FolderFileSource(node.getFullPath(),
                    node.getCompositionsCount(),
                    node.getLatestCreateDate(),
                    node.getEarliestCreateDate());
        }
        throw new IllegalStateException("unexpected type of node: " + nodeData);
    }
}
