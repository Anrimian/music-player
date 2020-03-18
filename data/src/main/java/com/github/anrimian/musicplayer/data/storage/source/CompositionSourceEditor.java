package com.github.anrimian.musicplayer.data.storage.source;

import com.github.anrimian.musicplayer.data.storage.providers.music.StorageMusicProvider;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.composition.FullComposition;
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

    private final StorageMusicProvider storageMusicProvider;

    public CompositionSourceEditor(StorageMusicProvider storageMusicProvider) {
        this.storageMusicProvider = storageMusicProvider;

        TagOptionSingleton.getInstance().setAndroid(true);
    }

    public Completable setCompositionTitle(FullComposition composition, String title) {
        return setCompositionTitle(getPath(composition), title);
    }

    public Completable setCompositionAuthor(FullComposition composition, String author) {
        return setCompositionAuthor(getPath(composition), author);
    }

    public Completable setCompositionAuthor(Composition composition, String author) {
        return setCompositionAuthor(getPath(composition), author);
    }

    public Completable setCompositionAlbum(FullComposition composition, String author) {
        return setCompositionAlbum(getPath(composition), author);
    }

    public Completable setCompositionAlbum(Composition composition, String author) {
        return setCompositionAlbum(getPath(composition), author);
    }

    public Completable setCompositionAlbumArtist(FullComposition composition, String artist) {
        return setCompositionAlbumArtist(getPath(composition), artist);
    }

    public Completable setCompositionAlbumArtist(Composition composition, String artist) {
        return setCompositionAlbumArtist(getPath(composition), artist);
    }

    public Completable changeCompositionGenre(FullComposition composition,
                                              String oldGenre,
                                              String newGenre) {
        return changeCompositionGenre(getPath(composition), oldGenre, newGenre);
    }

    public Completable changeCompositionGenre(Composition composition,
                                              String oldGenre,
                                              String newGenre) {
        return changeCompositionGenre(getPath(composition), oldGenre, newGenre);
    }

    public Completable addCompositionGenre(FullComposition fullComposition,
                                           String newGenre) {
        return addCompositionGenre(getPath(fullComposition), newGenre);
    }

    public Completable removeCompositionGenre(FullComposition fullComposition, String genre) {
        return removeCompositionGenre(getPath(fullComposition), genre);
    }

    public Single<String[]> getCompositionGenres(FullComposition fullComposition) {
        return getCompositionGenres(getPath(fullComposition));
    }

    public Maybe<CompositionSourceTags> getFullTags(FullComposition fullComposition) {
        return getFullTags(getPath(fullComposition));
    }

    //TODO genre not found case
    Completable changeCompositionGenre(String filePath,
                                       String oldGenre,
                                       String newGenre) {
        return Completable.fromAction(() -> {
            String genres = getFileTag(filePath).getFirst(FieldKey.GENRE);
            genres = genres.replace(oldGenre, newGenre);
            editFile(filePath, FieldKey.GENRE, genres);
        });
    }

    Completable setCompositionAlbumArtist(String filePath, String artist) {
        return Completable.fromAction(() -> editFile(filePath, FieldKey.ALBUM_ARTIST, artist));
    }

    Completable setCompositionAlbum(String filePath, String author) {
        return Completable.fromAction(() -> editFile(filePath, FieldKey.ALBUM, author));
    }

    Completable setCompositionTitle(String filePath, String title) {
        return Completable.fromAction(() -> editFile(filePath, FieldKey.TITLE, title));
    }

    Completable addCompositionGenre(String filePath,
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

    Completable removeCompositionGenre(String filePath, String genre) {
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

    private Completable setCompositionAuthor(String filePath, String author) {
        return Completable.fromAction(() -> editFile(filePath, FieldKey.ARTIST, author));
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
                    tag.getFirst(FieldKey.ALBUM_ARTIST));
        });
    }

    private String getPath(Composition composition) {
        Long storageId = composition.getStorageId();
        if (storageId == null) {
            throw new RuntimeException("composition not found");
        }
        return storageMusicProvider.getCompositionFilePath(storageId);
    }

    private String getPath(FullComposition composition) {
        Long storageId = composition.getStorageId();
        if (storageId == null) {
            throw new RuntimeException("composition not found");
        }
        return storageMusicProvider.getCompositionFilePath(storageId);
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
