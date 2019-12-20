package com.github.anrimian.musicplayer.data.utils.collections;

import androidx.collection.LongSparseArray;

import com.github.anrimian.musicplayer.domain.utils.ListUtils;
import com.github.anrimian.musicplayer.domain.utils.changes.map.ChangeInspector;
import com.github.anrimian.musicplayer.domain.utils.changes.map.EqualComparator;
import com.github.anrimian.musicplayer.domain.utils.java.BiCallback;
import com.github.anrimian.musicplayer.domain.utils.java.Callback;
import com.github.anrimian.musicplayer.domain.utils.java.Mapper;

import java.util.List;
import java.util.Map;
import java.util.Set;

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


    public static <V> boolean processChanges(LongSparseArray<V> oldMap,
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

    public static <Old, New, NewKey> boolean processChanges(Set<Old> currentSet,
                                                            Map<NewKey, New> newMap,
                                                            Mapper<Old, NewKey> newKeyFetcher,
                                                            Mapper<New, Old> oldValueFetcher,
                                                            EqualComparator<Old, New> equalComparator,
                                                            Callback<Old> onDeleteCallback,
                                                            Callback<New> onAddedCallback,
                                                            BiCallback<Old, New> onModifyCallback) {
        boolean hasChanges = false;

        for(Old existValue: currentSet ) {

            NewKey existKey = newKeyFetcher.map(existValue);
            New newValue = newMap.get(existKey);

            if (newValue == null) {
                onDeleteCallback.call(existValue);
                hasChanges = true;
            }
        }

        for (Map.Entry<NewKey, New> newEntry: newMap.entrySet()) {
            New newValue = newEntry.getValue();

            Old existValue = oldValueFetcher.map(newValue);
            if (!currentSet.contains(existValue)) {
                onAddedCallback.call(newValue);
                hasChanges = true;
            } else if (equalComparator.areItemsTheSame(existValue, newValue)) {
                onModifyCallback.call(existValue, newValue);
                hasChanges = true;
            }
        }
        return hasChanges;
    }
}
