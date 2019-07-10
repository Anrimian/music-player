package com.github.anrimian.musicplayer.data.repositories.music.edit;

import com.github.anrimian.musicplayer.domain.utils.java.Callback;
import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.ID3v24Tag;
import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.NotSupportedException;
import com.mpatric.mp3agic.UnsupportedTagException;

import java.io.IOException;

import io.reactivex.Completable;
import io.reactivex.Single;

public class CompositionSourceEditor {

    public Completable setCompositionTitle(String filePath, String title) {
        return Completable.fromAction(() -> editFile(filePath, tag -> tag.setTitle(title)));
    }

    public Completable setCompositionAuthor(String filePath, String author) {
        return Completable.fromAction(() -> editFile(filePath, tag -> tag.setArtist(author)));
    }

    public Single<String> getCompositionTitle(String filePath) {
        return Single.fromCallable(() -> {
            Mp3File mp3file = new Mp3File(filePath);
            if (mp3file.hasId3v2Tag()) {
                ID3v2 id3v2Tag = mp3file.getId3v2Tag();
                return id3v2Tag.getTitle();
            }
            return null;
        });
    }

    public Single<String> getCompositionAuthor(String filePath) {
        return Single.fromCallable(() -> {
            Mp3File mp3file = new Mp3File(filePath);
            if (mp3file.hasId3v2Tag()) {
                ID3v2 id3v2Tag = mp3file.getId3v2Tag();
                return id3v2Tag.getArtist();//can be in v1 or v2 or null, interesting
            }
            return null;
        });
    }

    private void editFile(String filePath, Callback<ID3v2> onTagReadyCallback)
            throws InvalidDataException, IOException, UnsupportedTagException, NotSupportedException {
        Mp3File mp3file = new Mp3File(filePath);
        ID3v2 id3v2Tag;
        if (mp3file.hasId3v2Tag()) {
            id3v2Tag = mp3file.getId3v2Tag();
        } else {
            // mp3 does not have an ID3v2 tag, let's create one..
            id3v2Tag = new ID3v24Tag();
            mp3file.setId3v2Tag(id3v2Tag);
        }
        onTagReadyCallback.call(id3v2Tag);
        mp3file.save(filePath);
    }
}
