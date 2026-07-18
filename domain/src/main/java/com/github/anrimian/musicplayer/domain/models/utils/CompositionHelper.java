package com.github.anrimian.musicplayer.domain.models.utils;

import static com.github.anrimian.musicplayer.domain.Payloads.ALBUM;
import static com.github.anrimian.musicplayer.domain.Payloads.ARTIST;
import static com.github.anrimian.musicplayer.domain.Payloads.CORRUPTED;
import static com.github.anrimian.musicplayer.domain.Payloads.COVER_MODIFY_TIME;
import static com.github.anrimian.musicplayer.domain.Payloads.DATE_ADDED;
import static com.github.anrimian.musicplayer.domain.Payloads.DATE_MODIFIED;
import static com.github.anrimian.musicplayer.domain.Payloads.DURATION;
import static com.github.anrimian.musicplayer.domain.Payloads.FILE_EXISTS;
import static com.github.anrimian.musicplayer.domain.Payloads.SIZE;
import static com.github.anrimian.musicplayer.domain.Payloads.TITLE;
import static com.github.anrimian.musicplayer.domain.utils.TextUtils.isEmpty;

import com.github.anrimian.musicplayer.domain.Constants;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.composition.CompositionModel;
import com.github.anrimian.musicplayer.domain.models.composition.DeletedComposition;
import com.github.anrimian.musicplayer.domain.utils.FileUtils;
import com.github.anrimian.musicplayer.domain.utils.Objects;

import java.util.LinkedList;
import java.util.List;

import javax.annotation.Nonnull;

public class CompositionHelper {

    public static boolean areItemsTheSame(@Nonnull CompositionModel first, @Nonnull CompositionModel second) {
        return first.getId() == second.getId();
    }

    public static boolean areSourcesTheSame(@Nonnull CompositionModel first, @Nonnull CompositionModel second) {
        return Objects.equals(first.getAlbum(), second.getAlbum())
                && Objects.equals(first.getArtist(), second.getArtist())
                && Objects.equals(first.getAddedTime(), second.getAddedTime())
                && Objects.equals(first.getModifiedTime(), second.getModifiedTime())
                && Objects.equals(first.getCoverModifyTime(), second.getCoverModifyTime())
                && first.getDuration() == second.getDuration()
                && first.getSize() == second.getSize()
                && Objects.equals(first.getTitle(), second.getTitle())
                && first.getFileStatus() == second.getFileStatus()
                && first.getCorruptionType() == second.getCorruptionType()
                && first.isFileExists() == second.isFileExists();
    }

    public static boolean hasSourceChanges(@Nonnull Composition first, @Nonnull Composition second) {
        return first.getDuration() != second.getDuration()
                || first.getSize() != second.getSize()
                || first.getModifiedTime() != second.getModifiedTime();
    }

    public static List<Object> getChangePayload(CompositionModel first, CompositionModel second) {
        List<Object> payloads = new LinkedList<>();
        if (!Objects.equals(first.getAlbum(), second.getAlbum())) {
            payloads.add(ALBUM);
        }
        if (!Objects.equals(first.getArtist(), second.getArtist())) {
            payloads.add(ARTIST);
        }
        if (!Objects.equals(first.getAddedTime(), second.getAddedTime())) {
            payloads.add(DATE_ADDED);
        }
        if (!Objects.equals(first.getModifiedTime(), second.getModifiedTime())) {
            payloads.add(DATE_MODIFIED);
        }
        if (!Objects.equals(first.getCoverModifyTime(), second.getCoverModifyTime())) {
            payloads.add(COVER_MODIFY_TIME);
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
        if (first.isFileExists() != second.isFileExists() || first.getFileStatus() != second.getFileStatus()) {
            payloads.add(FILE_EXISTS);
        }
        return payloads;
    }

    public static String formatCompositionName(CompositionModel composition) {
        return composition.getTitle();
    }

    public static String formatCompositionName(DeletedComposition composition) {
        return composition.getTitle();
    }

    public static String formatCompositionName(String title, String fileName) {
        if (isEmpty(title)) {
            return FileUtils.formatFileName(fileName);
        }
        return title;
    }

    public static boolean isCompositionFileRemote(CompositionModel composition) {
        return CompositionHelperKt.isFileRemote(composition);
    }

    public static String[] splitGenres(String genres) {
        if (genres == null || genres.isEmpty()) {
            return new String[0];
        }
        return genres.split(Constants.GENRE_DIVIDER);
    }

}
