package com.github.anrimian.musicplayer.data.repositories.music.edit;

import com.github.anrimian.musicplayer.domain.models.composition.source.CompositionSourceTags;

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
import org.jaudiotagger.tag.id3.ID3v24Tag;

import java.io.File;
import java.io.IOException;

import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Single;

public class CompositionSourceEditor {

    private static final char GENRE_DIVIDER = '\u0000';

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

    //TODO genre not found case
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

    public Completable removeCompositionGenre(String filePath, String genre) {
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

            editFile(filePath, FieldKey.GENRE, sb.toString());
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

    public Single<CompositionSourceTags> getFullTags(String filePath) {
        return Single.fromCallable(() -> {
            Tag tag = getFileTag(filePath);
            return new CompositionSourceTags(tag.getFirst(FieldKey.TITLE),
                    tag.getFirst(FieldKey.ARTIST),
                    tag.getFirst(FieldKey.ALBUM),
                    tag.getFirst(FieldKey.ALBUM_ARTIST));
        });
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
            tag = new ID3v24Tag();
            file.setTag(tag);
        }
        tag.setField(genericKey, value == null? "" : value);
        AudioFileIO.write(file);
    }
}
