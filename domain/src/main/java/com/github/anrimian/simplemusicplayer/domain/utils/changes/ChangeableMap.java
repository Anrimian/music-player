package com.github.anrimian.simplemusicplayer.domain.utils.changes;

import java.util.Map;

import io.reactivex.Observable;

public class ChangeableMap<K, T> {

    private final Map<K, T> hashMap;

    private final Observable<Change<T>> changeObservable;

    public ChangeableMap(Map<K, T> hashMap, Observable<Change<T>> changeObservable) {
        this.hashMap = hashMap;
        this.changeObservable = changeObservable;
    }

    public Map<K, T> getHashMap() {
        return hashMap;
    }

    public Observable<Change<T>> getChangeObservable() {
        return changeObservable;
    }

    @Override
    public String toString() {
        return "ChangeableList{" +
                "hashMap=" + hashMap +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ChangeableMap<?, ?> that = (ChangeableMap<?, ?>) o;

        return hashMap != null ? hashMap.equals(that.hashMap) : that.hashMap == null;
    }

    @Override
    public int hashCode() {
        return hashMap != null ? hashMap.hashCode() : 0;
    }


}
