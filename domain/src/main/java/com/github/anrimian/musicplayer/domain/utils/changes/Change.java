package com.github.anrimian.musicplayer.domain.utils.changes;

import java.util.List;

import javax.annotation.Nonnull;

public abstract class Change<T> {

    public static class AddChange<T> extends Change<T> {

        @Nonnull
        protected final List<T> data;

        public AddChange(@Nonnull List<T> data) {
            this.data = data;
        }

        @Nonnull
        public List<T> getData() {
            return data;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            AddChange<?> addChange = (AddChange<?>) o;

            return data.equals(addChange.data);
        }

        @Override
        public int hashCode() {
            return data.hashCode();
        }

        @Override
        public String toString() {
            return "Change{" +
                    "data=" + data +
                    '}';
        }
    }

    public static class DeleteChange<T> extends Change<T> {

        @Nonnull
        protected final List<T> data;

        public DeleteChange(@Nonnull List<T> data) {
            this.data = data;
        }

        @Nonnull
        public List<T> getData() {
            return data;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            DeleteChange<?> that = (DeleteChange<?>) o;

            return data.equals(that.data);
        }

        @Override
        public int hashCode() {
            return data.hashCode();
        }

        @Override
        public String toString() {
            return "Change{" +
                    "data=" + data +
                    '}';
        }
    }

    public static class ModifyChange<T> extends Change<T> {

        @Nonnull
        private final List<ModifiedData<T>> data;

        public ModifyChange(@Nonnull List<ModifiedData<T>> data) {
            this.data = data;
        }

        @Nonnull
        public List<ModifiedData<T>> getData() {
            return data;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ModifyChange<?> that = (ModifyChange<?>) o;

            return data.equals(that.data);
        }

        @Override
        public int hashCode() {
            return data.hashCode();
        }

        @Override
        public String toString() {
            return "ModifyChange{" +
                    "data=" + data +
                    '}';
        }
    }
}
