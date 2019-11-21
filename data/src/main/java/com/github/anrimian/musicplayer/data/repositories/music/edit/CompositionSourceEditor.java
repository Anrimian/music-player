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

    public Maybe<String> getCompositionTitle(String filePath) {
        return Maybe.fromCallable(() -> getFileTag(filePath).getFirst(FieldKey.TITLE));
    }

    public Maybe<String> getCompositionAuthor(String filePath) {
        return Maybe.fromCallable(() -> getFileTag(filePath).getFirst(FieldKey.ARTIST));
    }

    public Maybe<String> getCompositionAlbum(String filePath) {
        return Maybe.fromCallable(() -> getFileTag(filePath).getFirst(FieldKey.ALBUM));
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
        tag.addField(genericKey, value);
        AudioFileIO.write(file);
    }
}
