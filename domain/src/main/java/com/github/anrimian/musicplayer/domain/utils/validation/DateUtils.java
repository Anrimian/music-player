package com.github.anrimian.musicplayer.domain.utils.validation;

import java.util.Date;

public class DateUtils {

    public static boolean isBefore(Date first, Date second) {
        if (first == null || second == null) {
            return false;
        }
        return first.compareTo(second) <= 0;
    }

    public static boolean isAfter(Date first, Date second) {
        if (first == null || second == null) {
            return false;
        }
        return first.compareTo(second) > 0;
    }
}
