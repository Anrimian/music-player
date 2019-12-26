package com.github.anrimian.musicplayer.data.repositories.music.edit;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.CannotWriteException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagException;
import org.jaudiotagger.tag.TagOptionSingleton;
import org.jaudiotagger.tag.id3.ID3v23Tag;

import java.io.File;
import java.io.IOException;

import io.reactivex.Completable;
import io.reactivex.Maybe;

public class CompositionSourceEditor {

    private static final String GENRE_DIVIDER = ";";

    public CompositionSourceEditor() {
        TagOptionSingleton.getInstance().setAndroid(true);
    }

    public Completable setCompositionTitle(String filePath, String title) {
        return Completable.fromAction(() -> editFile(filePath, FieldKey.TITLE, title));
    }

    public Completable setCompositionAuthor(String filePath, String author) {
        return Completable.fromAction(() -> editFile(filePath, FieldKey.ARTIST, author));
    }

    public Completable setCompositionAlbum(String filePath, String author) {
        return Completable.fromAction(() -> editFile(filePath, FieldKey.ALBUM, author));
    }

    public Completable setCompositionAlbumArtist(String filePath, String artist) {
        return Completable.fromAction(() -> editFile(filePath, FieldKey.ALBUM_ARTIST, artist));
    }

    public Completable setCompositionGenre(String filePath, String genre) {
        return Completable.fromAction(() -> editFile(filePath, FieldKey.GENRE, genre));
    }

    public Completable changeCompositionGenre(String filePath,
                                              String oldGenre,
                                              String newGenre) {
        return Completable.fromAction(() -> {
            String genres = getFileTag(filePath).getFirst(FieldKey.GENRE);
            genres = genres.replace(oldGenre, newGenre);
            editFile(filePath, FieldKey.GENRE, genres);
        });
    }

    public Completable addCompositionGenre(String filePath,
                                           String newGenre) {
        return Completable.fromAction(() -> {
            String genres = getFileTag(filePath).getFirst(FieldKey.GENRE);
            StringBuilder sb = new StringBuilder(genres);
            if (sb.length() != 0) {
                sb.append(GENRE_DIVIDER);
                sb.append(" ");
            }
            sb.append(newGenre);
            editFile(filePath, FieldKey.GENRE, sb.toString());
        });
    }

    public Completable removeCompositionGenre(String filePath, String genre) {
        return Completable.fromAction(() -> {
            String genres = getFileTag(filePath).getFirst(FieldKey.GENRE);
            int lastIndexOfGenre = genres.lastIndexOf(genre);
            if (lastIndexOfGenre == -1) {
                return;
            }
            String textToDelete = genre;
            if (lastIndexOfGenre != genres.length()) {
                textToDelete += GENRE_DIVIDER + " ";
            }
            String newGenres = genres.replace(textToDelete, "");
            editFile(filePath, FieldKey.GENRE, newGenres);
        });
    }

    public Maybe<String> getCompositionTitle(String filePath) {
        return Maybe.fromCallable(() -> getFileTag(filePath).getFirst(FieldKey.TITLE));
    }

    public Maybe<String> getCompositionAuthor(String filePath) {
        return Maybe.fromCallable(() -> getFileTag(filePath).getFirst(FieldKey.ARTIST));
    }

    public Maybe<String> getCompositionAlbum(String filePath) {
        return Maybe.fromCallable(() -> getFileTag(filePath).getFirst(FieldKey.ALBUM));
    }

    public Maybe<String> getCompositionAlbumArtist(String filePath) {
        return Maybe.fromCallable(() -> getFileTag(filePath).getFirst(FieldKey.ALBUM_ARTIST));
    }

    public Maybe<String> getCompositionGenre(String filePath) {
        return Maybe.fromCallable(() -> getFileTag(filePath).getFirst(FieldKey.GENRE));
    }

    private Tag getFileTag(String filePath) throws TagException, ReadOnlyFileException,
            CannotReadException, InvalidAudioFrameException, IOException {
        AudioFile file = AudioFileIO.read(new File(filePath));
        return file.getTag();
    }

    private void editFile(String filePath, FieldKey genericKey, String value) throws IOException,
            TagException, ReadOnlyFileException, CannotReadException, InvalidAudioFrameException,
            CannotWriteException {
        AudioFile file = AudioFileIO.read(new File(filePath));
        Tag tag = file.getTag();
        if (tag == null) {
            tag = new ID3v23Tag();
            file.setTag(tag);
        }
        tag.setField(genericKey, value == null? "" : value);
        AudioFileIO.write(file);
    }
}
