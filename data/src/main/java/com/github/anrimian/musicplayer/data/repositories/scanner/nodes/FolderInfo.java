package com.github.anrimian.musicplayer.data.repositories.scanner.nodes;

import java.util.List;

public class FolderInfo {

    private final String name;
    private final String parentName;
    private final List<Long> compositionsId;

    public FolderInfo(String name, String parentName, List<Long> compositionsId) {
        this.name = name;
        this.parentName = parentName;
        this.compositionsId = compositionsId;
    }

    public List<Long> getCompositionsId() {
        return compositionsId;
    }

    public String getName() {
        return name;
    }

    public String getParentName() {
        return parentName;
    }
}
