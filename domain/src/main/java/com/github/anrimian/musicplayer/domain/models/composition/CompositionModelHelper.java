package com.github.anrimian.musicplayer.domain.models.composition;

import static com.github.anrimian.musicplayer.domain.utils.TextUtils.isEmpty;

public class CompositionModelHelper {

    public static String formatCompositionName(Composition composition) {
        String name = composition.getDisplayName();
        if (isEmpty(name)) {
            String title = composition.getTitle();
            if (isEmpty(title)) {
                return getLastPathPart(composition.getFilePath());
            }
            return title;
        }
        int cropIndex = name.lastIndexOf('.');
        if (cropIndex != -1) {
            return name.substring(0, cropIndex);
        }
        return name;
    }

    public static String getLastPathPart(String path) {
        String displayPath = path;
        int lastSlashIndex = path.lastIndexOf('/');
        if (lastSlashIndex != -1) {
            displayPath = path.substring(++lastSlashIndex, path.length());
        }
        return displayPath;
    }
}
