package com.github.anrimian.simplemusicplayer.domain.utils.changes;

import javax.annotation.Nonnull;

@SuppressWarnings("NullableProblems")
public class Change<T> {

    @Nonnull
    private ChangeType changeType;

    @Nonnull
    private T data;

    public Change(@Nonnull ChangeType changeType, @Nonnull T data) {
        this.changeType = changeType;
        this.data = data;
    }

    public Change() {
    }

    @Nonnull
    public ChangeType getChangeType() {
        return changeType;
    }

    public void setChangeType(@Nonnull ChangeType changeType) {
        this.changeType = changeType;
    }

    @Nonnull
    public T getData() {
        return data;
    }

    public void setData(@Nonnull T data) {
        this.data = data;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Change<?> change = (Change<?>) o;

        if (changeType != change.changeType) return false;
        return data.equals(change.data);
    }

    @Override
    public int hashCode() {
        int result = changeType.hashCode();
        result = 31 * result + data.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Change{" +
                "changeType=" + changeType +
                ", data=" + data +
                '}';
    }
}
