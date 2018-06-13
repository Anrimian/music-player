package com.github.anrimian.simplemusicplayer.data.utils.folders;

import java.util.ArrayList;
import java.util.List;


public class FolderTreeMapper {

//    public FolderTreeInfo toTreeInfo(RxNode<Long, Folder> node) {
//        List<FolderNode> folderNodes = new ArrayList<>();
//        for (RxNode<Long, Folder> child: node.getNodes()) {
//            folderNodes.add(toFolderNode(child));
//        }
//        FolderTreeInfo folderTreeInfo = new FolderTreeInfo();
//        folderTreeInfo.setNodes(folderNodes);
//        folderTreeInfo.setChangeObservable(node.getChildChangeObservable().map(this::toFolderNode));
//        return folderTreeInfo;
//    }
//
//    private Change<FolderNode> toFolderNode(Change<RxNode<Long, Folder>> rxChange) {
//        return new Change<>(rxChange.getChangeType(), toFolderNode(rxChange.getData()));
//    }
//
//    private FolderNode toFolderNode(RxNode<Long, Folder> rxNode) {
//        FolderNode folderNode = new FolderNode();
//        folderNode.setFolder(rxNode.getData());
//        folderNode.setHasChildren(!rxNode.getNodes().isEmpty());
//        return folderNode;
//    }
}
