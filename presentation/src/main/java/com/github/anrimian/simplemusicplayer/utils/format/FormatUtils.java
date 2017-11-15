package com.github.anrimian.simplemusicplayer.utils.format;

import java.util.Locale;

import static java.util.concurrent.TimeUnit.HOURS;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.MINUTES;

/**
 * Created on 15.11.2017.
 */

public class FormatUtils {

    public static String formatMilliseconds(long millis) {
        return String.format(Locale.getDefault(), "%02d:%02d:%02d",
                MILLISECONDS.toHours(millis),
                MILLISECONDS.toMinutes(millis) - HOURS.toMinutes(MILLISECONDS.toHours(millis)),
                MILLISECONDS.toSeconds(millis) - MINUTES.toSeconds(MILLISECONDS.toMinutes(millis)));
    }
}
