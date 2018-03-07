package com.github.anrimian.simplemusicplayer.data.utils.folders;

import javax.annotation.Nonnull;

@SuppressWarnings("NullableProblems")
public class FolderNode {

    @Nonnull
    private Folder folder;

    private boolean hasChildren;

    @Nonnull
    public Folder getFolder() {
        return folder;
    }

    public void setFolder(@Nonnull Folder folder) {
        this.folder = folder;
    }

    public boolean isHasChildren() {
        return hasChildren;
    }

    public void setHasChildren(boolean hasChildren) {
        this.hasChildren = hasChildren;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FolderNode that = (FolderNode) o;

        return folder.equals(that.folder);
    }

    @Override
    public int hashCode() {
        return folder.hashCode();
    }
}
