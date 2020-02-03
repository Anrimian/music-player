package com.github.anrimian.musicplayer.domain.models.composition.folders;

public class FolderFileSource2 implements FileSource2 {

    private final long id;
    private final String name;

    public FolderFileSource2(long id, String name) {
        this.id = id;
        this.name = name;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FolderFileSource2 that = (FolderFileSource2) o;

        return id == that.id;
    }

    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }
}
