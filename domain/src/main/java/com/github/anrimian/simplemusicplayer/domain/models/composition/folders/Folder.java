package com.github.anrimian.simplemusicplayer.domain.models.composition.folders;

import com.github.anrimian.simplemusicplayer.domain.utils.changes.Change;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import io.reactivex.Observable;

public class Folder {

    private Observable<List<FileSource>> filesObservable;
    private Observable<Change<FileSource>> selfChangeObservable;

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

    @SuppressWarnings("Java8ListSort")//lets wait:)
    public void applyFileOrder(Comparator<FileSource> comparator) {
        filesObservable = filesObservable.doOnNext(files -> Collections.sort(files, comparator));
    }
}
