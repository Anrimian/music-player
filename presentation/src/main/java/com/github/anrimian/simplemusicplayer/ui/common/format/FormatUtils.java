package com.github.anrimian.simplemusicplayer.ui.common.format;

import android.content.Context;

import com.github.anrimian.simplemusicplayer.R;
import com.github.anrimian.simplemusicplayer.domain.models.composition.Composition;
import com.github.anrimian.simplemusicplayer.domain.models.composition.Order;

import java.util.Locale;

import static android.text.TextUtils.isEmpty;
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

    public static StringBuilder formatCompositionAuthor(Composition composition, Context context) {
        String author = composition.getArtist();

        StringBuilder sb = new StringBuilder();
        if (!isEmpty(author)) {
            sb.append(author);
        } else {
            sb.append(context.getString(R.string.unknown_author));
        }
        return sb;
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

    public static int getOrderTitle(Order order) {
        switch (order) {
            case ALPHABETICAL: return R.string.alphabetical_order;
            case ALPHABETICAL_DESC: return R.string.alphabetical_desc_order;
            case ADD_TIME: return R.string.add_date_order;
            case ADD_TIME_DESC: return R.string.add_date_desc_order;
            default: throw new IllegalStateException("can not find title for order: " + order);
        }
    }
}
