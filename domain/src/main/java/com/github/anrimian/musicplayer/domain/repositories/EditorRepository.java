package com.github.anrimian.musicplayer.domain.repositories;

import com.github.anrimian.musicplayer.domain.models.composition.Composition;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Single;

public interface EditorRepository {

    Completable changeCompositionAuthor(Composition composition, String newAuthor);

    Completable changeCompositionTitle(Composition composition, String title);

    Completable changeCompositionFileName(Composition composition, String fileName);

    Completable changeCompositionsFilePath(List<Composition> compositions);

    Single<String> changeFolderName(String filePath, String folderName);
}
