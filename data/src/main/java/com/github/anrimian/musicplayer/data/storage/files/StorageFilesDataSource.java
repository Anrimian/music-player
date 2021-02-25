package com.github.anrimian.musicplayer.data.storage.files;

import androidx.core.util.Pair;

import com.github.anrimian.musicplayer.data.storage.providers.music.FilePathComposition;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.composition.FullComposition;

import java.util.List;

public interface StorageFilesDataSource {
    String renameCompositionsFolder(List<Composition> compositions,
                                    String oldPath,
                                    String newName,
                                    List<FilePathComposition> updatedCompositions);

    Pair<String, String> renameCompositionFile(FullComposition composition, String fileName);

    List<FilePathComposition> moveCompositionsToFolder(List<Composition> compositions,
                                                       String fromPath,
                                                       String toPath);

    String moveCompositionsToNewFolder(List<Composition> compositions,
                                       String fromPath,
                                       String toParentPath,
                                       String directoryName,
                                       List<FilePathComposition> updatedCompositions);

    List<Composition> deleteCompositionFiles(List<Composition> compositions, Object tokenForDelete);

    void deleteCompositionFile(Composition composition);

    void clearDeleteData();
}
