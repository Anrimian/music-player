package com.github.anrimian.musicplayer.domain.models.order;

import java.util.HashMap;
import java.util.Map;


public enum OrderType {
    NAME(1),
    ADD_TIME(3),
    COMPOSITION_COUNT(5),
    DURATION(7),
    SIZE(9);

    private static final Map<Integer, OrderType> map = new HashMap<>();

    /**
     * only odd values
     */
    private final int id;

    static {
        for (OrderType pageType : OrderType.values()) {
            map.put(pageType.id, pageType);
        }
    }

    OrderType(int id) {
        this.id = id;
    }

    public static OrderType fromId(int id) {
        return map.get(id);
    }

    public int getId() {
        return id;
    }
}
