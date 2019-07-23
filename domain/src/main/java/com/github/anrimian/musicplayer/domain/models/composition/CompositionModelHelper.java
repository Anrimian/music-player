package com.github.anrimian.musicplayer.domain.models.composition;

import static com.github.anrimian.musicplayer.domain.utils.TextUtils.isEmpty;

public class CompositionModelHelper {

    public static String formatCompositionName(Composition composition) {
        String title = composition.getTitle();
        if (isEmpty(title)) {
            return formatFileName(composition.getFilePath());
        }
        return title;
    }

    public static String formatFileName(String filePath) {
        return formatFileName(filePath, false);
    }

    public static String formatFileName(String filePath, boolean showExtension) {
        if (isEmpty(filePath)) {
            return "";
        }
        String fileName = getLastPathPart(filePath);
        if (!showExtension) {
            int cropIndex = fileName.lastIndexOf('.');
            if (cropIndex != -1) {
                return fileName.substring(0, cropIndex);
            }
        }
        return fileName;
    }

    public static String getLastPathPart(String path) {
        String displayPath = path;
        int lastSlashIndex = path.lastIndexOf('/');
        if (lastSlashIndex != -1) {
            displayPath = path.substring(++lastSlashIndex);
        }
        return displayPath;
    }
}
