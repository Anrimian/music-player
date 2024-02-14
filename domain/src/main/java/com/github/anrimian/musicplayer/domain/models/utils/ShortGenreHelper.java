package com.github.anrimian.musicplayer.domain.models.utils;

import static com.github.anrimian.musicplayer.domain.Payloads.NAME;

import com.github.anrimian.musicplayer.domain.utils.Objects;

import java.util.LinkedList;
import java.util.List;

import javax.annotation.Nonnull;

public class ShortGenreHelper {

    public static boolean areSourcesTheSame(@Nonnull String first, @Nonnull String second) {
        return Objects.equals(first, second);
    }

    public static List<Object> getChangePayload(String first, String second) {
        List<Object> payloads = new LinkedList<>();
        if (!Objects.equals(first, second)) {
            payloads.add(NAME);
        }
        return payloads;
    }

}
