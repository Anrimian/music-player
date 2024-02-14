package com.github.anrimian.musicplayer.domain.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class FileUtilsTest {

    @Test
    public void getChangedFilePath() {
        String result = FileUtils.replaceFileName("1/2/3/same/4/5/same", "new");
        assertEquals("1/2/3/same/4/5/new", result);
    }
}