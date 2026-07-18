package com.github.anrimian.musicplayer.ui.common.format;

import static java.lang.String.format;
import static java.util.concurrent.TimeUnit.HOURS;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.MINUTES;

import java.util.Locale;

public class TimeFormatUtils {

    public static String formatMilliseconds(long millis) {
        return formatMilliseconds(millis, true);
    }

    public static String formatMilliseconds(long millis, boolean cutZeroNumbers) {
        StringBuilder sb = new StringBuilder();

        long hours = MILLISECONDS.toHours(millis);
        if (hours != 0 || !cutZeroNumbers) {
            sb.append(format(Locale.getDefault(), "%02d", hours));
            sb.append(":");
        }

        long minutes = MILLISECONDS.toMinutes(millis) - HOURS.toMinutes(hours);
        if (minutes != 0 || hours != 0 || !cutZeroNumbers) {
            sb.append(format(Locale.getDefault(), "%02d", minutes));

        } else {
            sb.append("00");
        }
        sb.append(":");

        long seconds = MILLISECONDS.toSeconds(millis) - MINUTES.toSeconds(MILLISECONDS.toMinutes(millis));
        sb.append(format(Locale.getDefault(), "%02d", seconds));
        return sb.toString();
    }

}
