package com.github.anrimian.simplemusicplayer.data.utils.folders;

import java.util.List;

import javax.annotation.Nonnull;

import by.mrsoft.mrdoc.domain.models.common.change.Change;
import io.reactivex.Observable;

@SuppressWarnings("NullableProblems")
public class FolderTreeInfo {

    @Nonnull
    private Observable<Change<FolderNode>> changeObservable;

    @Nonnull
    private List<FolderNode> nodes;

    @Nonnull
    public Observable<Change<FolderNode>> getChangeObservable() {
        return changeObservable;
    }

    public void setChangeObservable(@Nonnull Observable<Change<FolderNode>> changeObservable) {
        this.changeObservable = changeObservable;
    }

    @Nonnull
    public List<FolderNode> getNodes() {
        return nodes;
    }

    public void setNodes(@Nonnull List<FolderNode> nodes) {
        this.nodes = nodes;
    }
}
