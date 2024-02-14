package com.github.anrimian.musicplayer.data.storage.source;

import android.net.Uri;
import android.os.Build;

import com.github.anrimian.musicplayer.data.models.composition.file.StorageCompositionSource;
import com.github.anrimian.musicplayer.data.storage.exceptions.GenreAlreadyPresentException;
import com.github.anrimian.musicplayer.data.storage.exceptions.IllegalInputException;
import com.github.anrimian.musicplayer.data.storage.exceptions.TagReaderException;
import com.github.anrimian.musicplayer.data.storage.providers.music.StorageMusicProvider;
import com.github.anrimian.musicplayer.data.utils.file.FileUtils;
import com.github.anrimian.musicplayer.domain.Constants;
import com.github.anrimian.musicplayer.domain.models.composition.content.CompositionContentSource;
import com.github.anrimian.musicplayer.domain.models.composition.tags.AudioFileInfo;
import com.github.anrimian.musicplayer.domain.models.composition.tags.CompositionSourceTags;
import com.github.anrimian.musicplayer.domain.models.image.ImageSource;
import com.github.anrimian.musicplayer.domain.models.utils.CompositionHelper;
import com.github.anrimian.musicplayer.domain.utils.ListUtils;
import com.github.anrimian.musicplayer.domain.utils.TextUtils;
import com.github.anrimian.musicplayer.domain.utils.functions.ThrowsCallback;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.id3.valuepair.ImageFormats;
import org.jaudiotagger.tag.images.AndroidArtwork;
import org.jaudiotagger.tag.images.Artwork;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.subjects.BehaviorSubject;
import kotlin.collections.ArraysKt;

public class CompositionSourceEditor {

    private static final char GENRE_DIVIDER_CHAR = Constants.GENRE_DIVIDER_CHAR;
    private static final String GENRE_DIVIDER = Constants.GENRE_DIVIDER;
    private static final int MAX_COVER_SIZE = 1000;
    private static final int NO_POSITION = -1;

    private final StorageMusicProvider storageMusicProvider;
    private final FileSourceProvider fileSourceProvider;
    private final ContentSourceHelper contentSourceHelper;

    public CompositionSourceEditor(StorageMusicProvider storageMusicProvider,
                                   FileSourceProvider fileSourceProvider,
                                   ContentSourceHelper contentSourceHelper) {
        this.storageMusicProvider = storageMusicProvider;
        this.fileSourceProvider = fileSourceProvider;
        this.contentSourceHelper = contentSourceHelper;
    }

    public Completable setCompositionTitle(CompositionContentSource source, String title) {
        return Completable.fromAction(() -> editFile(source, FieldKey.TITLE, title));
    }

    public Completable setCompositionAuthor(CompositionContentSource source, String author) {
        return Completable.fromAction(() -> editFile(source, FieldKey.ARTIST, author));
    }

    public Completable renameCompositionAuthor(CompositionContentSource source,
                                               String oldName,
                                               String author) {
        return Completable.fromAction(() -> editAudioFileTag(source, tag -> {
            if (TextUtils.isEmpty(oldName)) {
                throw new IllegalStateException("renaming empty name is not allowed");
            }
            String currentArtist = tag.getFirst(FieldKey.ARTIST);
            if (oldName.equals(currentArtist)) {
                tag.setField(FieldKey.ARTIST, author);
            }
            String currentAlbumArtist = tag.getFirst(FieldKey.ALBUM_ARTIST);
            if (oldName.equals(currentAlbumArtist)) {
                tag.setField(FieldKey.ALBUM_ARTIST, author);
            }
        }));
    }

    public Completable renameCompositionsAuthor(List<CompositionContentSource> sources,
                                                String oldName,
                                                String author,
                                                BehaviorSubject<Long> editingSubject) {
        return Observable.fromIterable(sources)
                .concatMapCompletable(source -> renameCompositionAuthor(source, oldName, author)
                        .doOnSubscribe(d -> editingSubject.onNext(0L))
                )
                .onErrorResumeNext(throwable ->
                        storageMusicProvider.processStorageException(
                                throwable,
                                ListUtils.mapList(sources, contentSourceHelper::createUri)
                        )
                );
    }

    public Completable setCompositionAlbum(CompositionContentSource source, String album) {
        return Completable.fromAction(() -> editFile(source, FieldKey.ALBUM, album));
    }

    public Completable setCompositionsAlbum(List<CompositionContentSource> sources,
                                            String album,
                                            BehaviorSubject<Long> editingSubject) {
        return Observable.fromIterable(sources)
                .concatMapCompletable(source -> setCompositionAlbum(source, album)
                        .doOnSubscribe(d -> editingSubject.onNext(0L))
                )
                .onErrorResumeNext(throwable ->
                        storageMusicProvider.processStorageException(
                                throwable,
                                ListUtils.mapList(sources, contentSourceHelper::createUri)
                        )
                );
    }

    public Completable setCompositionAlbumArtist(CompositionContentSource source, String artist) {
        return Completable.fromAction(() -> editFile(source, FieldKey.ALBUM_ARTIST, artist));
    }

    public Completable setCompositionsAlbumArtist(List<CompositionContentSource> sources,
                                                  String artist,
                                                  BehaviorSubject<Long> editingSubject) {
        return Observable.fromIterable(sources)
                .flatMapCompletable(source -> setCompositionAlbumArtist(source, artist)
                        .doOnSubscribe(d -> editingSubject.onNext(0L))
                )
                .onErrorResumeNext(throwable ->
                        storageMusicProvider.processStorageException(
                                throwable,
                                ListUtils.mapList(sources, contentSourceHelper::createUri)
                        )
                );
    }

    public Completable setCompositionTrackNumber(CompositionContentSource source, Long trackNumber) {
        return Completable.fromAction(() -> editFile(source, FieldKey.TRACK, TextUtils.toString(trackNumber)));
    }

    public Completable setCompositionDiscNumber(CompositionContentSource source, Long discNumber) {
        return Completable.fromAction(() -> editFile(source, FieldKey.DISC_NO, TextUtils.toString(discNumber)));
    }

    public Completable setCompositionComment(CompositionContentSource source, String text) {
        return Completable.fromAction(() -> editFile(source, FieldKey.COMMENT, text));
    }

    public Completable setCompositionRawGenre(CompositionContentSource source, String genreValue) {
        return Completable.fromAction(() -> editFile(source, FieldKey.GENRE, genreValue));
    }

    public Completable setCompositionsGenre(List<CompositionContentSource> sources,
                                            String oldName,
                                            String genre,
                                            BehaviorSubject<Long> editingSubject) {
        return Observable.fromIterable(sources)
                .flatMapCompletable(source -> changeCompositionGenre(source, oldName, genre)
                        .doOnSubscribe(d -> editingSubject.onNext(0L))
                )
                .onErrorResumeNext(throwable ->
                        storageMusicProvider.processStorageException(
                                throwable,
                                ListUtils.mapList(sources, contentSourceHelper::createUri)
                        )
                );
    }

    public Completable setCompositionLyrics(CompositionContentSource source, String text) {
        return Completable.fromAction(() -> editFile(source, FieldKey.LYRICS, text));
    }

    public Completable changeCompositionGenre(CompositionContentSource source,
                                              String oldGenre,
                                              String newGenre) {
        return Completable.fromAction(() -> editAudioFileTag(source, tag -> {
            if (newGenre.indexOf(GENRE_DIVIDER_CHAR) != -1) {
                throw new IllegalInputException(String.valueOf(GENRE_DIVIDER_CHAR));
            }
            String genres = tag.getFirst(FieldKey.GENRE);
            String[] genresArray = genres.split(GENRE_DIVIDER);
            if (ArraysKt.contains(genresArray, newGenre)) {
                throw new GenreAlreadyPresentException();
            }
            //replace first occurrence of old genre
            genres = genres.replaceFirst(Pattern.quote(oldGenre), Matcher.quoteReplacement(newGenre));
            tag.setField(FieldKey.GENRE, genres);
        }));
    }

    public Completable addCompositionGenre(CompositionContentSource source, String newGenre) {
        return addCompositionGenre(source, newGenre, NO_POSITION);
    }

    public Completable addCompositionGenre(CompositionContentSource source,
                                           String newGenre,
                                           int position) {
        return Completable.fromAction(() -> editAudioFileTag(source, tag -> {
            if (newGenre.indexOf(GENRE_DIVIDER_CHAR) != -1) {
                throw new IllegalInputException(String.valueOf(GENRE_DIVIDER_CHAR));
            }
            String newFormattedGenre = newGenre.trim();
            String genres = tag.getFirst(FieldKey.GENRE);
            String[] genresArray = genres.split(GENRE_DIVIDER);
            if (ArraysKt.contains(genresArray, newFormattedGenre)) {
                throw new GenreAlreadyPresentException();
            }
            String updatedGenres;
            if (genres.isEmpty()) {
                updatedGenres = newFormattedGenre;
            } else {
                if (position == NO_POSITION) {
                    updatedGenres = genres + GENRE_DIVIDER + newFormattedGenre;
                } else {
                    if (position == genresArray.length) {
                        updatedGenres = genres + GENRE_DIVIDER + newFormattedGenre;
                    } else {
                        StringBuilder sb = new StringBuilder();
                        for (int i = 0; i < genresArray.length; i++) {
                            String genre = genresArray[i];
                            if (position == i) {
                                sb.append(newFormattedGenre);
                                sb.append(GENRE_DIVIDER);
                            }
                            sb.append(genre);
                            if (i < genresArray.length - 1) {
                                sb.append(GENRE_DIVIDER);
                            }
                        }
                        updatedGenres = sb.toString();
                    }
                }
            }
            tag.setField(FieldKey.GENRE, updatedGenres);
        }));
    }

    public Completable moveGenre(CompositionContentSource source, int from, int to) {
        return Completable.fromAction(() -> editAudioFileTag(source, tag -> {
            String genres = tag.getFirst(FieldKey.GENRE);
            String[] genresArray = splitGenres(genres);
            if (from < to) {
                for (int i = from; i < to; i++) {
                    ListUtils.swap(genresArray, i, i + 1);
                }
            } else {
                for (int i = from; i > to; i--) {
                    ListUtils.swap(genresArray, i, i - 1);
                }
            }

            StringBuilder sb = new StringBuilder();
            //can be joinToString after refactoring to kt
            for (int i = 0; i < genresArray.length; i++) {
                sb.append(genresArray[i]);
                if (i < genresArray.length - 1) {
                    sb.append(GENRE_DIVIDER);
                }
            }
            tag.setField(FieldKey.GENRE, sb.toString());
        }));
    }

    public Completable removeCompositionGenre(CompositionContentSource source, String genre) {
        return Completable.fromAction(() -> editAudioFileTag(source, tag -> {
            String genres = tag.getFirst(FieldKey.GENRE);
            String[] genresArray = splitGenres(genres);
            List<String> genresList = new ArrayList<>(Arrays.asList(genresArray));
            int removedGenresCount = 0;
            for (int i = 0; i < genresArray.length; i++) {
                String genreEntry = genresArray[i];
                if (genreEntry.equals(genre)) {
                    genresList.remove(i - removedGenresCount);
                    removedGenresCount++;
                }
            }
            String resultGenres = String.join(GENRE_DIVIDER, genresList);
            tag.setField(FieldKey.GENRE, resultGenres);
        }));
    }

    public Single<Long> changeCompositionAlbumArt(CompositionContentSource source,
                                                  ImageSource imageSource) {
        return Single.fromCallable(() -> editAudioFileTag(source,
                tag -> {
                    try (InputStream stream = fileSourceProvider.getImageStream(imageSource)) {
                        if (stream == null) {
                            return;
                        }
                        byte[] data = FileUtils.getScaledBitmapByteArray(stream, MAX_COVER_SIZE);
                        Artwork artwork = new AndroidArtwork();
                        artwork.setBinaryData(data);
                        artwork.setMimeType(ImageFormats.getMimeTypeForBinarySignature(data));
                        tag.deleteArtworkField();
                        tag.setField(artwork);
                    }
                }
        ));
    }

    public Single<Long> removeCompositionAlbumArt(CompositionContentSource source) {
        return Single.fromCallable(() -> editAudioFileTag(source, Tag::deleteArtworkField));
    }

    public Single<AudioFileInfo> getAudioFileInfo(CompositionContentSource source) {
        return Single.fromCallable(() -> {
            try {
                File file = contentSourceHelper.getAsFile(source);
                AudioFile audioFile = readFile(file);
                Tag tag = audioFile.getTagOrCreateDefault();
                long fileSize = file.length();
                int durationSeconds = audioFile.getAudioHeader().getTrackLength();
                CompositionSourceTags tags = new CompositionSourceTags(tag.getFirst(FieldKey.TITLE),
                        tag.getFirst(FieldKey.ARTIST),
                        tag.getFirst(FieldKey.ALBUM),
                        tag.getFirst(FieldKey.ALBUM_ARTIST),
                        durationSeconds,
                        TextUtils.safeParseLong(tag.getFirst(FieldKey.TRACK), null),
                        TextUtils.safeParseLong(tag.getFirst(FieldKey.DISC_NO), null),
                        tag.getFirst(FieldKey.COMMENT),
                        tag.getFirst(FieldKey.LYRICS),
                        splitGenres(tag.getFirst(FieldKey.GENRE)));
                return new AudioFileInfo(fileSize, tags);
            } catch (FileNotFoundException e) {
                throw e;
            } catch (Exception e) {
                throw new TagReaderException(e.getMessage(), e);
            }
        });
    }

    public Maybe<byte[]> getCompositionArtworkBinaryData(CompositionContentSource source) {
        return Maybe.fromCallable(() -> {
            Tag tag = getFileTag(source);
            if (tag == null) {
                return null;
            }
            Artwork artwork = tag.getFirstArtwork();
            if (artwork == null) {
                return null;
            }
            return artwork.getBinaryData();
        });
    }

    String getCompositionTitle(CompositionContentSource source) {
        return getFileTag(source).getFirst(FieldKey.TITLE);
    }

    String getCompositionAuthor(CompositionContentSource source) {
        return getFileTag(source).getFirst(FieldKey.ARTIST);
    }

    String getCompositionAlbum(CompositionContentSource source) {
        return getFileTag(source).getFirst(FieldKey.ALBUM);
    }

    String getCompositionAlbumArtist(CompositionContentSource source) {
        return getFileTag(source).getFirst(FieldKey.ALBUM_ARTIST);
    }

    String getCompositionRawGenre(CompositionContentSource source) {
        return getFileTag(source).getFirst(FieldKey.GENRE);
    }

    String[] getCompositionGenres(CompositionContentSource source) {
        return splitGenres(getCompositionRawGenre(source));
    }

    String getCompositionLyrics(CompositionContentSource source) {
        return getFileTag(source).getFirst(FieldKey.LYRICS);
    }

    private String[] splitGenres(String genres) {
        return CompositionHelper.splitGenres(genres);
    }

    private Tag getFileTag(CompositionContentSource source) {
        return getFileTag(contentSourceHelper.getAsFile(source));
    }

    private Tag getFileTag(File file) {
        return getAudioFile(file).getTagOrCreateDefault();
    }

    private AudioFile getAudioFile(File file) {
        try {
            return readFile(file);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void editFile(CompositionContentSource source,
                          FieldKey genericKey,
                          @Nullable String value) throws Exception {
        editAudioFileTag(source, tag -> {
            if (value == null) {
                tag.deleteField(genericKey);
            } else {
                tag.setField(genericKey, value);
            }
        });
    }

    private long editAudioFileTag(CompositionContentSource source,
                                  ThrowsCallback<Tag> callback) throws Exception {
        File fileToEdit = contentSourceHelper.getAsFile(source);
        if (source instanceof StorageCompositionSource
                && (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R || !fileToEdit.canWrite())
        ) {
            StorageCompositionSource storageSource = (StorageCompositionSource) source;
            return fileSourceProvider.useTempFile(fileToEdit.getName(), tempFile -> {
                copyFileUsingStream(fileToEdit, tempFile);
                runFileAction(tempFile, callback);
                copyFileToMediaStore(tempFile, storageSource.getUri());
            });
        } else {
            runFileAction(fileToEdit, callback);
            return fileToEdit.length();
        }
    }

    private void runFileAction(File file, ThrowsCallback<Tag> callback) throws Exception {
        AudioFile audioFile = readFile(file);
        Tag tag = audioFile.getTagOrCreateAndSetDefault();
        callback.call(tag);
        AudioFileIO.write(audioFile);
    }

    private AudioFile readFile(File file) throws Exception {
        try {
            return AudioFileIO.read(file);
        } catch (CannotReadException e) {
            throw new TagReaderException(e.getMessage(), e);
        }
    }

    private void copyFileToMediaStore(File source, Uri uri) throws IOException {
        try (InputStream is = new FileInputStream(source);
             OutputStream os = storageMusicProvider.openCompositionOutputStream(uri)
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
