package com.github.anrimian.simplemusicplayer.data.utils.folders;

public class FolderTree {

    /*private static final Long ROOT_NODE_ID = 0L;

    private final FolderTreeMapper folderTreeMapper = new FolderTreeMapper();
    private final FolderDataSource folderDataSource;

    private RxNode<Long, Folder> folderTree;

    public FolderTree(FolderDataSource folderDataSource) {
        this.folderDataSource = folderDataSource;
    }

    public Single<FolderTreeInfo> getFolders(@Nullable Folder rootFolder) {
        return getFolderTree()
                .map(tree -> findNodeToFolder(rootFolder))
                .map(folderTreeMapper::toTreeInfo);
    }

    private Single<RxNode<Long, Folder>> getFolderTree() {
        if (folderTree == null) {
            return createFolderTree().doOnSuccess(folderTree -> this.folderTree = folderTree);
        } else {
            return Single.just(folderTree);
        }
    }

    private Single<RxNode<Long, Folder>> createFolderTree() {
        return Single.fromCallable(folderDataSource::getAllFolders)
                .map(this::createTree);
    }

    private RxNode<Long, Folder> createTree(List<Folder> folders) {
        RxNode<Long, Folder> rootNode = new RxNode<>(ROOT_NODE_ID,null, null);
        fillNode(rootNode, folders);
        subscribeOnFolderChanging();
        return rootNode;
    }

    private void subscribeOnFolderChanging() {
        folderDataSource.getFolderChangeObservable()
                .subscribe(this::onFolderChanged);
    }

    private void onFolderChanged(Change<Folder> change) {
        switch (change.getChangeType()) {
            case ADDED: {
                addNewNode(change.getData());
                break;
            }
            case MODIFY: {
                modifyNode(change.getData());
                break;
            }
            case DELETED: {
                deleteNode(change.getData());
                break;
            }
        }
    }

    private void deleteNode(Folder folder) {
        RxNode<Long, Folder> rootNode = findNode(folderTree, folder.getParentLocalId());
        if (rootNode == null) {
            throw new IllegalStateException("can not find node for folder: " + folder.getParentLocalId());
        }
        rootNode.removeNode(folder.getLocalId());
    }

    private void modifyNode(Folder folder) {
        RxNode<Long, Folder> node = findNode(folderTree, folder.getLocalId());
        if (node == null) {
            throw new IllegalStateException("can not find node for folder: " + folder.getParentLocalId());
        }
        node.setData(folder);
    }

    private void addNewNode(Folder folder) {
        RxNode<Long, Folder> rootNode = findNode(folderTree, folder.getParentLocalId());
        if (rootNode == null) {
            throw new IllegalStateException("can not find node for folder: " + folder.getParentLocalId());
        }
        RxNode<Long, Folder> node = new RxNode<>(folder.getLocalId(), folder, rootNode);
        rootNode.addNode(node);
    }

    @Nullable
    private RxNode<Long, Folder> findNode(RxNode<Long, Folder> rootNode, @Nullable Long key) {
        Long rootKey = rootNode.getKey();
        if ((key == null && rootNode.getKey().equals(ROOT_NODE_ID)) || rootKey.equals(key)) {
            return rootNode;
        } else {
            for (RxNode<Long, Folder> node: rootNode.getNodes()) {
                RxNode<Long, Folder> foundNode = findNode(node, key);
                if (foundNode != null) {
                    return foundNode;
                }
            }
        }
        return null;
    }

    private void fillNode(RxNode<Long, Folder> rootNode, List<Folder> folders) {
        Long key = rootNode.getKey();
        for (Folder folder: folders) {
            Long parentKey = folder.getParentLocalId();
            if ((parentKey == null && key.equals(ROOT_NODE_ID)) || key.equals(parentKey)) {
                RxNode<Long, Folder> node = new RxNode<>(folder.getLocalId(), folder, rootNode);
                rootNode.addNode(node);
                fillNode(node, folders);
            }
        }
    }

    private RxNode<Long, Folder> findNodeToFolder(@Nullable Folder folder) {
        Long key = ROOT_NODE_ID;
        if (folder != null) {
            key = folder.getLocalId();
        }
        RxNode<Long, Folder> node = findNodeToFolder(folderTree, key);
        if (node == null) {
            throw new IllegalStateException("can not find node for key: " + key);
        }
        return node;
    }

    @Nullable
    private RxNode<Long, Folder> findNodeToFolder(RxNode<Long, Folder> rootNode, @Nonnull Long parentKey) {
        Long key = rootNode.getKey();
        if (key.equals(parentKey)) {
            return rootNode;
        }
        for (RxNode<Long, Folder> node: rootNode.getNodes()) {
            RxNode<Long, Folder> foundNode = findNode(node, parentKey);
            if (foundNode != null) {
                return foundNode;
            }
        }
        return null;
    }*/
}
