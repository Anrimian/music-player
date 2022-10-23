package com.github.anrimian.musicplayer.domain.models.utils;

import static com.github.anrimian.musicplayer.domain.Payloads.ALBUM;
import static com.github.anrimian.musicplayer.domain.Payloads.ARTIST;
import static com.github.anrimian.musicplayer.domain.Payloads.CORRUPTED;
import static com.github.anrimian.musicplayer.domain.Payloads.DATE_ADDED;
import static com.github.anrimian.musicplayer.domain.Payloads.DATE_MODIFIED;
import static com.github.anrimian.musicplayer.domain.Payloads.DURATION;
import static com.github.anrimian.musicplayer.domain.Payloads.FILE_EXISTS;
import static com.github.anrimian.musicplayer.domain.Payloads.SIZE;
import static com.github.anrimian.musicplayer.domain.Payloads.TITLE;
import static com.github.anrimian.musicplayer.domain.utils.FileUtils.formatFileName;
import static com.github.anrimian.musicplayer.domain.utils.TextUtils.isEmpty;

import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.composition.InitialSource;
import com.github.anrimian.musicplayer.domain.utils.Objects;

import java.util.LinkedList;
import java.util.List;

import javax.annotation.Nonnull;

public class CompositionHelper {

    public static boolean areSourcesTheSame(@Nonnull Composition first, @Nonnull Composition second) {
        return Objects.equals(first.getAlbum(), second.getAlbum())
                && Objects.equals(first.getArtist(), second.getArtist())
                && Objects.equals(first.getDateAdded(), second.getDateAdded())
                && Objects.equals(first.getDateModified(), second.getDateModified())
                && first.getDuration() == second.getDuration()
                && first.getSize() == second.getSize()
                && Objects.equals(first.getTitle(), second.getTitle())
                && first.getCorruptionType() == second.getCorruptionType()
                && first.isFileExists() == second.isFileExists();
    }

    public static boolean hasSourceChanges(@Nonnull Composition first, @Nonnull Composition second) {
        return first.getDuration() != second.getDuration() || first.getSize() != second.getSize();
    }

    public static List<Object> getChangePayload(Composition first, Composition second) {
        List<Object> payloads = new LinkedList<>();
        if (!Objects.equals(first.getAlbum(), second.getAlbum())) {
            payloads.add(ALBUM);
        }
        if (!Objects.equals(first.getArtist(), second.getArtist())) {
            payloads.add(ARTIST);
        }
        if (!Objects.equals(first.getDateAdded(), second.getDateAdded())) {
            payloads.add(DATE_ADDED);
        }
        if (!Objects.equals(first.getDateModified(), second.getDateModified())) {
            payloads.add(DATE_MODIFIED);
        }
        if (first.getDuration() != second.getDuration()) {
            payloads.add(DURATION);
        }
        if (first.getSize() != second.getSize()) {
            payloads.add(SIZE);
        }
        if (!Objects.equals(first.getTitle(), second.getTitle())) {
            payloads.add(TITLE);
        }
        if (first.getCorruptionType() != second.getCorruptionType()) {
            payloads.add(CORRUPTED);
        }
        if (first.isFileExists() != second.isFileExists()) {
            payloads.add(FILE_EXISTS);
        }
        return payloads;
    }

    public static String formatCompositionName(Composition composition) {
        return composition.getTitle();
    }

    public static String formatCompositionName(String title, String fileName) {
        if (isEmpty(title)) {
            return formatFileName(fileName);
        }
        return title;
    }

    public static boolean isCompositionFileRemote(Composition composition) {
        return !composition.isFileExists() && composition.getInitialSource() == InitialSource.REMOTE;
    }
}
