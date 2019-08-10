package com.github.anrimian.musicplayer.domain.utils.changes;

public class ModifiedData<T> {
    private final T oldData;
    private final T newData;

    public ModifiedData(T oldData, T newData) {
        this.oldData = oldData;
        this.newData = newData;
    }

    public T getOldData() {
        return oldData;
    }

    public T getNewData() {
        return newData;
    }
}
