package com.github.anrimian.musicplayer.ui.utils.views.recycler_view.touch_helper.drag_and_drop;

import javax.annotation.Nonnull;

public class ItemSwap<T> {

    @Nonnull
    private final T item;
    private final int position;

    public ItemSwap(@Nonnull T item, int position) {
        this.item = item;
        this.position = position;
    }

    @Nonnull
    public T getItem() {
        return item;
    }

    public int getPosition() {
        return position;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ItemSwap<?> itemSwap = (ItemSwap<?>) o;

        if (position != itemSwap.position) return false;
        return item.equals(itemSwap.item);
    }

    @Override
    public int hashCode() {
        int result = item.hashCode();
        result = 31 * result + position;
        return result;
    }

    @Override
    public String toString() {
        return "ItemSwap{" +
                "item=" + item +
                ", position=" + position +
                '}';
    }
}
