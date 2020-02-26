package com.github.anrimian.musicplayer.domain.repositories;

import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.composition.FullComposition;
import com.github.anrimian.musicplayer.domain.models.composition.folders.FileSource2;
import com.github.anrimian.musicplayer.domain.models.composition.folders.FolderFileSource2;
import com.github.anrimian.musicplayer.domain.models.composition.source.CompositionSourceTags;
import com.github.anrimian.musicplayer.domain.models.genres.ShortGenre;

import java.util.Collection;
import java.util.List;

import javax.annotation.Nullable;

import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Single;

public interface EditorRepository {

    Completable changeCompositionGenre(FullComposition composition,
                                       ShortGenre oldGenre,
                                       String newGenre);

    Completable addCompositionGenre(FullComposition composition,
                                    String newGenre);

    Completable removeCompositionGenre(FullComposition composition, ShortGenre genre);

    Completable changeCompositionAuthor(FullComposition composition, String newAuthor);

    Completable changeCompositionAlbumArtist(FullComposition composition, String newAuthor);

    Completable changeCompositionAlbum(FullComposition composition, String newAlbum);

    Completable changeCompositionTitle(FullComposition composition, String title);

    Completable changeCompositionFileName(FullComposition composition, String fileName);

    Completable changeCompositionsFilePath(List<Composition> compositions);

    Completable changeFolderName(long folderId, String folderName);

    Single<String> moveFile(String filePath, String oldPath, String newPath);

    Completable moveFiles(Collection<FileSource2> files,
                          @Nullable Long fromFolderId,
                          @Nullable Long toFolderId);

    Completable moveFilesToNewDirectory(Collection<FileSource2> files,
                                        @Nullable Long fromFolderId,
                                        String directoryName);

    Completable createDirectory(String path);

    Completable updateAlbumName(String name, long id);

    Completable updateAlbumArtist(String name, long albumId);

    Completable updateArtistName(String name, long artistId);

    Completable updateGenreName(String name, long genreId);

    Maybe<CompositionSourceTags> getCompositionFileTags(FullComposition composition);

    Single<String[]> getCompositionFileGenres(FullComposition composition);
}
