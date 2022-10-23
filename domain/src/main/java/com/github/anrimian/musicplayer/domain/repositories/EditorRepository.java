package com.github.anrimian.musicplayer.domain.repositories;

import com.github.anrimian.musicplayer.domain.models.composition.FullComposition;
import com.github.anrimian.musicplayer.domain.models.composition.content.CompositionContentSource;
import com.github.anrimian.musicplayer.domain.models.folders.FileSource;
import com.github.anrimian.musicplayer.domain.models.genres.ShortGenre;
import com.github.anrimian.musicplayer.domain.models.image.ImageSource;

import java.util.Collection;
import java.util.List;

import javax.annotation.Nullable;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.subjects.BehaviorSubject;

public interface EditorRepository {

    Completable changeCompositionGenre(long compositionId,
                                       CompositionContentSource source,
                                       ShortGenre oldGenre,
                                       String newGenre);

    Completable addCompositionGenre(long compositionId,
                                    CompositionContentSource source,
                                    String newGenre);

    Completable removeCompositionGenre(long compositionId,
                                       CompositionContentSource source,
                                       ShortGenre genre);

    Completable changeCompositionAuthor(long compositionId,
                                        CompositionContentSource source,
                                        String newAuthor);

    Completable changeCompositionAlbumArtist(long compositionId,
                                             CompositionContentSource source,
                                             String newAuthor);

    Completable changeCompositionAlbum(long compositionId,
                                       CompositionContentSource source,
                                       String newAlbum);

    Completable changeCompositionTitle(long compositionId, CompositionContentSource source, String title);

    Completable changeCompositionLyrics(long compositionId,
                                        CompositionContentSource source,
                                        String text);

    Completable changeCompositionFileName(FullComposition composition, String fileName);

    Completable changeFolderName(long folderId, String folderName);

    Completable moveFiles(Collection<FileSource> files,
                          @Nullable Long fromFolderId,
                          @Nullable Long toFolderId);

    Completable moveFilesToNewDirectory(Collection<FileSource> files,
                                        @Nullable Long fromFolderId,
                                        @Nullable Long targetParentFolderId,
                                        String directoryName);

    Completable updateAlbumName(String name,
                                List<Long> compositionIds,
                                List<CompositionContentSource> sources,
                                long albumId,
                                BehaviorSubject<Long> editingSubject);

    Completable updateAlbumArtist(String artist,
                                  List<Long> compositionIds,
                                  List<CompositionContentSource> sources,
                                  long albumId,
                                  BehaviorSubject<Long> editingSubject);

    Completable updateArtistName(String name,
                                 List<Long> compositionIds,
                                 List<CompositionContentSource> sources,
                                 long artistId,
                                 BehaviorSubject<Long> editingSubject);

    Completable updateGenreName(String name,
                                List<Long> compositionIds,
                                List<CompositionContentSource> sources,
                                long genreId,
                                BehaviorSubject<Long> editingSubject);

    Completable changeCompositionAlbumArt(long compositionId,
                                          CompositionContentSource source,
                                          ImageSource imageSource);

    Completable removeCompositionAlbumArt(long compositionId, CompositionContentSource source);

    Completable updateTagsFromSource(CompositionContentSource source, FullComposition fullComposition);
}
