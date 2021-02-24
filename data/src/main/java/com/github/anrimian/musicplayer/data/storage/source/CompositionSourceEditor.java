package com.github.anrimian.musicplayer.data.storage.source;

import android.os.Build;

import com.github.anrimian.musicplayer.data.storage.providers.music.StorageMusicProvider;
import com.github.anrimian.musicplayer.data.utils.file.FileUtils;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.composition.FullComposition;
import com.github.anrimian.musicplayer.domain.models.composition.source.CompositionSourceTags;
import com.github.anrimian.musicplayer.domain.models.exceptions.EditorReadException;
import com.github.anrimian.musicplayer.domain.models.image.ImageSource;
import com.github.anrimian.musicplayer.domain.utils.functions.ThrowsCallback;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagOptionSingleton;
import org.jaudiotagger.tag.datatype.Artwork;
import org.jaudiotagger.tag.id3.ID3v24Tag;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import javax.annotation.Nullable;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;

import static com.github.anrimian.musicplayer.domain.utils.FileUtils.getFileName;

//albums editor - done
//files moving - done
//TODO remove empty folder after files had moved or folder renamed
//files moving/folder renaming - process error on not "well defined collection" - skip
//file rename - done
//folder rename - done
//TODO folder rename - check for duplicate?
//deny delete issue - done
//files/folder deleting - hide app confirm dialog possibility - done
//TODO content observer not called - seems working?

//TODO replace DATA with relative path
//TODO getFileSize() adapt
//TODO pathchnotes with explanation
/*
    Android 11 storage adaptation. From system version 11 editing and deleting files requires explicit
    user permission. So editing tags, moving compositions and folders, renaming folders action will require additional confirm.
    Deleting files and folders also will require additional dialog confirm, but with possibility not to show application delete dialog anymore.

 */
public class CompositionSourceEditor {

    private static final char GENRE_DIVIDER = '\u0000';
    private static final int MAX_COVER_SIZE = 1024;

    private final StorageMusicProvider storageMusicProvider;
    private final FileSourceProvider fileSourceProvider;

    public CompositionSourceEditor(StorageMusicProvider storageMusicProvider,
                                   FileSourceProvider fileSourceProvider) {
        this.storageMusicProvider = storageMusicProvider;
        this.fileSourceProvider = fileSourceProvider;

        TagOptionSingleton.getInstance().setAndroid(true);
    }

    public Completable setCompositionTitle(FullComposition composition, String title) {
        return getPath(composition)
                .flatMapCompletable(path -> setCompositionTitle(path, composition.getStorageId(), title));
    }

    public Completable setCompositionAuthor(FullComposition composition, String author) {
        return getPath(composition)
                .flatMapCompletable(path -> setCompositionAuthor(path, composition.getStorageId(), author));
    }

    public Completable setCompositionAuthor(Composition composition, String author) {
        return getPath(composition)
                .flatMapCompletable(path -> setCompositionAuthor(path, composition.getStorageId(), author));
    }

    public Completable setCompositionAlbum(FullComposition composition, String author) {
        return getPath(composition)
                .flatMapCompletable(path -> setCompositionAlbum(path, composition.getStorageId(), author));
    }

    public Completable setCompositionAlbum(Composition composition, String author) {
        return getPath(composition)
                .flatMapCompletable(path -> setCompositionAlbum(path, composition.getStorageId(), author));
    }

    public Single<List<Composition>> setCompositionsAlbum(List<Composition> compositions, String album) {
        return Observable.fromIterable(compositions)
                .flatMapCompletable(composition -> setCompositionAlbum(composition, album))
                .onErrorResumeNext(throwable -> storageMusicProvider.processStorageError(throwable, compositions))
                .toSingleDefault(compositions);
    }

    public Completable setCompositionAlbumArtist(FullComposition composition, String artist) {
        return getPath(composition)
                .flatMapCompletable(path -> setCompositionAlbumArtist(path, composition.getStorageId(), artist));
    }

    public Single<List<Composition>> setCompositionsAlbumArtist(List<Composition> compositions, String artist) {
        return Observable.fromIterable(compositions)
                .flatMapCompletable(composition -> setCompositionAlbumArtist(composition, artist))
                .onErrorResumeNext(throwable -> storageMusicProvider.processStorageError(throwable, compositions))
                .toSingleDefault(compositions);
    }

    public Completable setCompositionAlbumArtist(Composition composition, String artist) {
        return getPath(composition)
                .flatMapCompletable(path -> setCompositionAlbumArtist(path, composition.getStorageId(), artist));
    }

    public Completable setCompositionLyrics(FullComposition composition, String text) {
        return getPath(composition)
                .flatMapCompletable(path -> setCompositionLyrics(path, composition.getStorageId(), text));
    }

    public Completable changeCompositionGenre(FullComposition composition,
                                              String oldGenre,
                                              String newGenre) {
        return getPath(composition)
                .flatMapCompletable(path -> changeCompositionGenre(path, composition.getStorageId(), oldGenre, newGenre));
    }

    public Completable changeCompositionGenre(Composition composition,
                                              String oldGenre,
                                              String newGenre) {
        return getPath(composition)
                .flatMapCompletable(path -> changeCompositionGenre(path, composition.getStorageId(), oldGenre, newGenre));
    }

    public Completable addCompositionGenre(FullComposition composition,
                                           String newGenre) {
        return getPath(composition)
                .flatMapCompletable(path -> addCompositionGenre(path, composition.getStorageId(), newGenre));
    }

    public Completable removeCompositionGenre(FullComposition composition, String genre) {
        return getPath(composition)
                .flatMapCompletable(path -> removeCompositionGenre(path, composition.getStorageId(), genre));
    }

    public Single<String[]> getCompositionGenres(FullComposition composition) {
        return getPath(composition)
                .flatMap(this::getCompositionGenres);
    }

    public Completable changeCompositionAlbumArt(FullComposition composition,
                                                 ImageSource imageSource) {
        return getPath(composition)
                .flatMapCompletable(path -> changeCompositionAlbumArt(path, composition.getStorageId(), imageSource));
    }

    public Completable removeCompositionAlbumArt(FullComposition composition) {
        return getPath(composition)
                .flatMapCompletable(path -> removeCompositionAlbumArt(path, composition.getStorageId()));
    }

    public Maybe<CompositionSourceTags> getFullTags(FullComposition composition) {
        return getPath(composition)
                .flatMapMaybe(this::getFullTags);
    }

    //genre not found case
    Completable changeCompositionGenre(String filePath,
                                       Long storageId,
                                       String oldGenre,
                                       String newGenre) {
        return Completable.fromAction(() -> {
            String genres = getFileTag(filePath).getFirst(FieldKey.GENRE);
            genres = genres.replace(oldGenre, newGenre);
            editFile(filePath, storageId, FieldKey.GENRE, genres);
        });
    }

    Completable setCompositionAlbumArtist(String filePath,
                                          Long storageId,
                                          String artist) {
        return Completable.fromAction(() -> editFile(filePath, storageId, FieldKey.ALBUM_ARTIST, artist));
    }

    Completable setCompositionAlbum(String filePath,
                                    Long storageId,
                                    String author) {
        return Completable.fromAction(() -> editFile(filePath, storageId, FieldKey.ALBUM, author));
    }


    Completable setCompositionTitle(String filePath,
                                    Long storageId,
                                    String title) {
        return Completable.fromAction(() -> editFile(filePath, storageId, FieldKey.TITLE, title));
    }

    Completable setCompositionLyrics(String filePath,
                                     Long storageId,
                                     String text) {
        return Completable.fromAction(() -> editFile(filePath, storageId, FieldKey.LYRICS, text));
    }

    Completable addCompositionGenre(String filePath,
                                    Long storageId,
                                    String newGenre) {
        return Completable.fromAction(() -> {
            AudioFile file = AudioFileIO.read(new File(filePath));
            Tag tag = file.getTag();
            if (tag == null) {
                tag = new ID3v24Tag();
                file.setTag(tag);
            }
            tag.addField(FieldKey.GENRE, newGenre);
            AudioFileIO.write(file);
//            String genres = getFileTag(filePath).getFirst(FieldKey.GENRE);
//            StringBuilder sb = new StringBuilder(genres);
//            if (sb.length() != 0) {
//                sb.append(GENRE_DIVIDER);
//            }
//            sb.append(newGenre);
//            sb.append(GENRE_DIVIDER);
//            editFile(filePath, FieldKey.GENRE, sb.toString());
        });
    }

    Completable removeCompositionGenre(String filePath,
                                       Long storageId,
                                       String genre) {
        return Completable.fromAction(() -> {
            String genres = getFileTag(filePath).getFirst(FieldKey.GENRE);
            int startIndex = genres.indexOf(genre);
            if (startIndex == -1) {
                return;
            }
            int endIndex = startIndex + genre.length();
            StringBuilder sb = new StringBuilder(genres);

            //clear divider at start
            if (startIndex == 1 && sb.charAt(0) == GENRE_DIVIDER) {
                startIndex = 0;
            }
            //clear divider at end or next if genre is at start or has divider before
            if ((endIndex == sb.length() - 2 || startIndex == 0 || sb.charAt(startIndex - 1) == GENRE_DIVIDER)
                    && (endIndex < sb.length() && sb.charAt(endIndex) == GENRE_DIVIDER)) {
                endIndex++;
            }

            sb.delete(startIndex, endIndex);

            editFile(filePath, storageId, FieldKey.GENRE, sb.toString());
        });
    }

    Maybe<String> getCompositionTitle(String filePath) {
        return Maybe.fromCallable(() -> getFileTag(filePath).getFirst(FieldKey.TITLE));
    }

    Maybe<String> getCompositionAuthor(String filePath) {
        return Maybe.fromCallable(() -> getFileTag(filePath).getFirst(FieldKey.ARTIST));
    }

    Maybe<String> getCompositionAlbum(String filePath) {
        return Maybe.fromCallable(() -> getFileTag(filePath).getFirst(FieldKey.ALBUM));
    }

    Maybe<String> getCompositionAlbumArtist(String filePath) {
        return Maybe.fromCallable(() -> getFileTag(filePath).getFirst(FieldKey.ALBUM_ARTIST));
    }

    Maybe<String> getCompositionGenre(String filePath) {
        return Maybe.fromCallable(() -> getFileTag(filePath).getFirst(FieldKey.GENRE));
    }

    Maybe<String> getCompositionLyrics(String filePath) {
        return Maybe.fromCallable(() -> getFileTag(filePath).getFirst(FieldKey.LYRICS));
    }

    private Completable setCompositionAuthor(String filePath, Long storageId, String author) {
        return Completable.fromAction(() -> editFile(filePath, storageId, FieldKey.ARTIST, author));
    }

    private Single<String[]> getCompositionGenres(String filePath) {
        return Single.fromCallable(() -> {
            String genres =  getFileTag(filePath).getFirst(FieldKey.GENRE);
            if (genres == null) {
                return new String[0];
            }
            return genres.split(String.valueOf(GENRE_DIVIDER));
        });
    }

    private Maybe<CompositionSourceTags> getFullTags(String filePath) {
        return Maybe.fromCallable(() -> {
            Tag tag = getFileTag(filePath);
            if (tag == null) {
                return null;
            }
            return new CompositionSourceTags(tag.getFirst(FieldKey.TITLE),
                    tag.getFirst(FieldKey.ARTIST),
                    tag.getFirst(FieldKey.ALBUM),
                    tag.getFirst(FieldKey.ALBUM_ARTIST),
                    tag.getFirst(FieldKey.LYRICS));
        });
    }

    private Single<String> getPath(Composition composition) {
        return getPath(composition.getStorageId());
    }

    private Single<String> getPath(FullComposition composition) {
        return getPath(composition.getStorageId());
    }

    private Single<String> getPath(@Nullable Long storageId) {
        return Single.fromCallable(() -> {
            if (storageId == null) {
                throw new RuntimeException("composition not found");
            }
            String path = storageMusicProvider.getCompositionFilePath(storageId);
            if (path == null) {
                throw new RuntimeException("composition path not found in system media store");
            }
            return storageMusicProvider.getCompositionFilePath(storageId);
        });
    }

    private Tag getFileTag(String filePath) throws Exception {
        AudioFile file = readFile(new File(filePath));
        return file.getTag();
    }

    private Completable changeCompositionAlbumArt(String filePath, Long id, ImageSource imageSource) {
        return Completable.fromAction(() -> editAudioFileTag(filePath,
                id,
                tag -> {
                    try (InputStream stream = fileSourceProvider.getImageStream(imageSource)) {
                        if (stream == null) {
                            return;
                        }
                        byte[] data = FileUtils.getScaledBitmapByteArray(stream, MAX_COVER_SIZE);
                        Artwork artwork = new Artwork();
                        artwork.setBinaryData(data);
                        tag.deleteArtworkField();
                        tag.setField(artwork);
                    }
                }
        ));
    }

    private Completable removeCompositionAlbumArt(String filePath, Long id) {
        return Completable.fromAction(() -> editAudioFileTag(filePath, id, Tag::deleteArtworkField));
    }

    private void editFile(String filePath,
                          long id,
                          FieldKey genericKey,
                          String value) throws Exception {
        editAudioFileTag(filePath,
                id,
                tag -> tag.setField(genericKey, value == null? "" : value)
        );
    }

    private void editAudioFileTag(String filePath, Long id, ThrowsCallback<Tag> callback)
            throws Exception {
        File fileToEdit = new File(filePath);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            fileSourceProvider.useTempFile(getFileName(filePath), tempFile -> {
                copyFileUsingStream(fileToEdit, tempFile);
                runFileAction(tempFile, callback);
                copyFileToMediaStore(tempFile, id);
            });
        } else {
            runFileAction(fileToEdit, callback);
        }
    }

    private void runFileAction(File file, ThrowsCallback<Tag> callback) throws Exception {
        AudioFile audioFile = readFile(file);
        Tag tag = audioFile.getTag();
        if (tag == null) {
            tag = new ID3v24Tag();
            audioFile.setTag(tag);
        }
        callback.call(tag);
        AudioFileIO.write(audioFile);
    }

    private AudioFile readFile(File file) throws Exception {
        try {
            return AudioFileIO.read(file);
        } catch (CannotReadException e) {
            throw new EditorReadException(e.getMessage());
        }
    }

    private void copyFileToMediaStore(File source, Long id) throws IOException {
        try (InputStream is = new FileInputStream(source);
             OutputStream os = storageMusicProvider.openCompositionOutputStream(id)
        ) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
        }
    }

    private static void copyFileUsingStream(File source, File dest) throws IOException {
        try (InputStream is = new FileInputStream(source);
             OutputStream os = new FileOutputStream(dest)
        ) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
        }
    }
}
