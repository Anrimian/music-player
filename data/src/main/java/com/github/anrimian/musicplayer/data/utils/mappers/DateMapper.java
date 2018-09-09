package com.github.anrimian.musicplayer.data.utils.mappers;

import java.util.Date;

/**
 * Created on 17.05.2017.
 */

public class DateMapper {

    public Date toDate(Long time) {
        if (time == null) {
            return null;
        }
        return new Date(time);
    }

    public Long toUnixSeconds(Date date) {
        if (date == null) {
            return null;
        }
        return date.getTime();
    }
}
