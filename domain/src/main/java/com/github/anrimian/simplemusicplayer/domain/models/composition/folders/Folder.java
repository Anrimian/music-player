package com.github.anrimian.simplemusicplayer.domain.models.composition.folders;

import com.github.anrimian.simplemusicplayer.domain.utils.changes.Change;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import io.reactivex.Observable;

public class Folder {

    private Observable<List<FileSource>> filesObservable;
    private Observable<FileSource> selfChangeObservable;
    private Observable<Object> selfDeleteObservable;

    public Folder(Observable<List<FileSource>> filesObservable,
                  Observable<FileSource> selfChangeObservable,
                  Observable<Object> selfDeleteObservable) {
        this.filesObservable = filesObservable;
        this.selfChangeObservable = selfChangeObservable;
        this.selfDeleteObservable = selfDeleteObservable;
    }

    public Observable<List<FileSource>> getFilesObservable() {
        return filesObservable;
    }

    public Observable<FileSource> getSelfChangeObservable() {
        return selfChangeObservable;
    }

    public Observable<Object> getSelfDeleteObservable() {
        return selfDeleteObservable;
    }

    @SuppressWarnings("Java8ListSort")//lets wait:)
    public void applyFileOrder(Comparator<FileSource> comparator) {
        filesObservable = filesObservable.doOnNext(files -> Collections.sort(files, comparator));
    }
}
