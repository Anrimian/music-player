package com.github.anrimian.simplemusicplayer.ui.common.format;

import com.github.anrimian.simplemusicplayer.domain.models.Composition;

import java.util.Locale;

import static java.lang.String.format;
import static java.util.concurrent.TimeUnit.HOURS;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.MINUTES;

/**
 * Created on 15.11.2017.
 */

public class FormatUtils {

    public static String formatCompositionName(Composition composition) {
        String name = composition.getDisplayName();
        return name.substring(0, name.lastIndexOf('.'));
    }

    public static String formatMilliseconds(long millis) {
        StringBuilder sb = new StringBuilder();

        long hours = MILLISECONDS.toHours(millis);
        if (hours != 0) {
            sb.append(format(Locale.getDefault(), "%02d", hours));
            sb.append(":");
        }

        long minutes = MILLISECONDS.toMinutes(millis) - HOURS.toMinutes(hours);
        if (minutes != 0 || hours != 0) {
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
