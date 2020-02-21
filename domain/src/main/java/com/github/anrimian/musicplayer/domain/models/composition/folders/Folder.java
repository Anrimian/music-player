package com.github.anrimian.musicplayer.domain.models.composition.folders;

import com.github.anrimian.musicplayer.domain.utils.search.ListSearchFilter;
import com.github.anrimian.musicplayer.domain.utils.search.SearchFilter;

import java.util.ArrayList;
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

    @SuppressWarnings("Java8ListSort")//lets wait:)
    public void applyFileOrder(Observable<Comparator<FileSource>> orderObservable) {
        filesObservable = Observable.combineLatest(filesObservable, orderObservable, (fileSources, fileSourceComparator) -> {
            try {
                Collections.sort(fileSources, fileSourceComparator);//temporary ignored, class will be removed after folders remake
            } catch (Exception ignored) {}
            return new ArrayList<>(fileSources);
        });
    }

    public void applySearchFilter(@Nullable String text, SearchFilter<FileSource> searchFilter) {
        filesObservable = filesObservable.map(list -> ListSearchFilter.filterList(list, text, searchFilter));
    }

    public interface OrderProvider {
        Comparator<FileSource> getComparator();
    }
}
