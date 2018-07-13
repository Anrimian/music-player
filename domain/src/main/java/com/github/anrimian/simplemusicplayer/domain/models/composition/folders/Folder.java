package com.github.anrimian.simplemusicplayer.domain.models.composition.folders;

import com.github.anrimian.simplemusicplayer.domain.utils.changes.Change;

import java.util.List;

import io.reactivex.Observable;

public class Folder {

    private final Observable<List<FileSource>> filesObservable;
    private final Observable<Change<FileSource>> selfChangeObservable;

    public Folder(Observable<List<FileSource>> filesObservable,
                  Observable<Change<FileSource>> selfChangeObservable) {
        this.filesObservable = filesObservable;
        this.selfChangeObservable = selfChangeObservable;
    }

    public Observable<List<FileSource>> getFilesObservable() {
        return filesObservable;
    }

    public Observable<Change<FileSource>> getSelfChangeObservable() {
        return selfChangeObservable;
    }
}
