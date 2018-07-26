package com.github.anrimian.simplemusicplayer.domain.models.composition;

import java.util.HashMap;
import java.util.Map;

public enum Order {
    ALPHABETICAL(1),
    ALPHABETICAL_DESC(2),
    ADD_TIME(3),
    ADD_TIME_DESC(4);

    private static Map<Integer, Order> map = new HashMap<>();
    private int id;

    static {
        for (Order pageType : Order.values()) {
            map.put(pageType.id, pageType);
        }
    }

    Order(int id) {
        this.id = id;
    }

    public static Order fromId(int id) {
        return map.get(id);
    }

    public int getId() {
        return id;
    }
}
