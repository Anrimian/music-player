package com.github.anrimian.simplemusicplayer.domain.utils.changes;

import java.util.List;

import io.reactivex.Observable;

public class ChangeableList<T> {

    private List<T> list;

    private Observable<Change<T>> changeObservable;

    public ChangeableList(List<T> list, Observable<Change<T>> changeObservable) {
        this.list = list;
        this.changeObservable = changeObservable;
    }

    public List<T> getList() {
        return list;
    }

    public Observable<Change<T>> getChangeObservable() {
        return changeObservable;
    }

    @Override
    public String toString() {
        return "ChangeableList{" +
                "list=" + list +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ChangeableList<?> that = (ChangeableList<?>) o;

        return list != null ? list.equals(that.list) : that.list == null;
    }

    @Override
    public int hashCode() {
        return list != null ? list.hashCode() : 0;
    }
}
