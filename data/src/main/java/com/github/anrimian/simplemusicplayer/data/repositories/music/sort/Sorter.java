package com.github.anrimian.simplemusicplayer.data.repositories.music.sort;

import com.github.anrimian.simplemusicplayer.domain.models.composition.folders.FileSource;
import com.github.anrimian.simplemusicplayer.domain.models.composition.folders.Folder;

import java.util.List;

public interface Sorter<T> {

    void applyOrder(T data);
}
