package com.github.anrimian.musicplayer.domain.models.composition;

import static com.github.anrimian.musicplayer.domain.utils.TextUtils.isEmpty;

public class CompositionModelHelper {

    public static String formatCompositionName(Composition composition) {
        String title = composition.getTitle();
        if (isEmpty(title)) {
            String filePath = composition.getFilePath();
            if (isEmpty(filePath)) {
                return "";
            }
            filePath = getLastPathPart(composition.getFilePath());
            int cropIndex = filePath.lastIndexOf('.');
            if (cropIndex != -1) {
                return filePath.substring(0, cropIndex);
            }
            return filePath;
        }
        return title;
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
