package com.github.anrimian.musicplayer.domain.utils.changes.map;

import com.github.anrimian.musicplayer.domain.utils.java.Callback;

import java.util.HashMap;
import java.util.Map;

public class MapChangeProcessor {

    @Deprecated
    public static <K, V> boolean processChangesOld(Map<K, V> oldMap,
                                                Map<K, V> newMap,
                                                ChangeInspector<V> changeInspector,
                                                Callback<Map.Entry<K, V>> onDeleteCallback,
                                                Callback<Map.Entry<K, V>> onAddedCallback,
                                                Callback<Map.Entry<K, V>> onModifyCallback) {
        boolean hasChanges = false;

        for (Map.Entry<K, V> existEntry: new HashMap<>(oldMap).entrySet()) {
            V value = newMap.get(existEntry.getKey());
            if (value == null) {
                onDeleteCallback.call(existEntry);
                hasChanges = true;
            }
        }

        for (Map.Entry<K, V> newEntry: newMap.entrySet()) {
            V existValue = oldMap.get(newEntry.getKey());
            if (existValue == null) {
                onAddedCallback.call(newEntry);
                hasChanges = true;
            } else if (changeInspector.hasChanges(newEntry.getValue(), existValue)) {
                onModifyCallback.call(newEntry);
                hasChanges = true;
            }
        }
        return hasChanges;
    }

    public static <K, V> boolean processChanges(Map<K, V> oldMap,
                                                Map<K, V> newMap,
                                                ChangeInspector<V> changeInspector,
                                                Callback<V> onDeleteCallback,
                                                Callback<V> onAddedCallback,
                                                Callback<V> onModifyCallback) {
        boolean hasChanges = false;

        for (Map.Entry<K, V> existEntry: oldMap.entrySet()) {
            V value = newMap.get(existEntry.getKey());
            if (value == null) {
                onDeleteCallback.call(existEntry.getValue());
                hasChanges = true;
            }
        }

        for (Map.Entry<K, V> newEntry: newMap.entrySet()) {
            V existValue = oldMap.get(newEntry.getKey());
            V newValue = newEntry.getValue();
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
