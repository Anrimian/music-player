package com.github.anrimian.musicplayer.domain.models.composition.folders;

import com.github.anrimian.musicplayer.domain.utils.changes.Change;
import com.github.anrimian.musicplayer.domain.utils.search.ListSearchFilter;
import com.github.anrimian.musicplayer.domain.utils.search.SearchFilter;

import java.io.File;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.annotation.Nullable;

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
    public void applyFileOrder(OrderProvider orderProvider) {
        filesObservable = filesObservable.doOnNext(files -> Collections.sort(files, orderProvider.getComparator()));
    }

    public void applySearchFilter(@Nullable String text, SearchFilter<FileSource, String> searchFilter) {
        filesObservable = filesObservable.map(list -> ListSearchFilter.filterList(list, text, searchFilter));
    }

    public interface OrderProvider {
        Comparator<FileSource> getComparator();
    }
}
