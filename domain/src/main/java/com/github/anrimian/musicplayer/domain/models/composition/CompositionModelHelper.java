package com.github.anrimian.musicplayer.domain.models.composition;

import static com.github.anrimian.musicplayer.domain.utils.FileUtils.formatFileName;
import static com.github.anrimian.musicplayer.domain.utils.TextUtils.isEmpty;

public class CompositionModelHelper {

    public static String formatCompositionName(Composition composition) {
        String title = composition.getTitle();
        if (isEmpty(title)) {
            return formatFileName(composition.getFilePath());
        }
        return title;
    }
}
