package com.github.anrimian.musicplayer.domain.repositories;

import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.composition.FullComposition;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Single;

public interface EditorRepository {

    Completable changeCompositionGenre(FullComposition composition, String newGenre);

    Completable changeCompositionAuthor(FullComposition composition, String newAuthor);

    Completable changeCompositionAlbumArtist(FullComposition composition, String newAuthor);

    Completable changeCompositionAlbum(FullComposition composition, String newAlbum);

    Completable changeCompositionTitle(FullComposition composition, String title);

    Completable changeCompositionFileName(FullComposition composition, String fileName);

    Completable changeCompositionsFilePath(List<Composition> compositions);

    Single<String> changeFolderName(String filePath, String folderName);

    Single<String> moveFile(String filePath, String oldPath, String newPath);

    Completable createFile(String path);

    Completable updateAlbumName(String name, long id);

    Completable updateArtistName(String name, long artistId);

    Completable updateGenreName(String name, long genreId);
}
