package com.github.anrimian.musicplayer.data.database.utils;

import static com.github.anrimian.musicplayer.domain.utils.TextUtils.isEmpty;

public class DatabaseUtils {

    public static String[] getSearchArgs(String arg, int count) {
        if (isEmpty(arg)) {
            arg = null;
        }

        String[] result = new String[count];
        for (int i = 0; i < count; i++) {
            if (i == 0) {
                result[i] = arg;
            } else {
                result[i] = arg == null? null : "%" + arg + "%";
            }
        }
        return result;
    }

    public static Object[] toArgs(Object arg, int times) {
        Object[] result = new Object[times];
        for (int i = 0; i < times; i++) {
            result[i] = arg;
        }
        return result;
    }
}
