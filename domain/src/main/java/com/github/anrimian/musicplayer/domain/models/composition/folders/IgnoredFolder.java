package com.github.anrimian.musicplayer.domain.models.composition.folders;

import java.util.Date;

public class IgnoredFolder {

    private String relativePath;
    private Date addDate;

    public IgnoredFolder(String relativePath, Date addDate) {
        this.relativePath = relativePath;
        this.addDate = addDate;
    }

    public String getRelativePath() {
        return relativePath;
    }

    public Date getAddDate() {
        return addDate;
    }
}
