package com.github.anrimian.musicplayer.data.utils.collections;

import androidx.collection.LongSparseArray;

import com.github.anrimian.musicplayer.domain.utils.ListUtils;
import com.github.anrimian.musicplayer.domain.utils.changes.map.ChangeInspector;
import com.github.anrimian.musicplayer.domain.utils.java.Callback;

import java.util.List;

public class AndroidCollectionUtils {

    public static <E> LongSparseArray<E> mapToSparseArray(List<E> from,
                                                          ListUtils.MapperFunction<E, Long> keySelector) {
        LongSparseArray<E> to = new LongSparseArray<>();
        for (E t: from) {
            Long key = keySelector.map(t);
            if (key != null) {
                to.put(key, t);
            }
        }
        return to;
    }


    public static <K, V> boolean processChanges(LongSparseArray<V> oldMap,
                                                LongSparseArray<V> newMap,
                                                ChangeInspector<V> changeInspector,
                                                Callback<V> onDeleteCallback,
                                                Callback<V> onAddedCallback,
                                                Callback<V> onModifyCallback) {
        boolean hasChanges = false;

        for(int i = 0, size = oldMap.size(); i < size; i++) {
            V existValue = oldMap.valueAt(i);
            long existKey = oldMap.keyAt(i);
            V value = newMap.get(existKey);
            if (value == null) {
                onDeleteCallback.call(existValue);
                hasChanges = true;
            }
        }

        for(int i = 0, size = newMap.size(); i < size; i++) {
            long newKey = newMap.keyAt(i);
            V newValue = newMap.valueAt(i);

            V existValue = oldMap.get(newKey);
            if (existValue == null) {
                onAddedCallback.call(newValue);
                hasChanges = true;
            } else if (changeInspector.hasChanges(newValue, existValue)) {
                onModifyCallback.call(newValue);
                hasChanges = true;
            }
        }
        return hasChanges;
    }
}
