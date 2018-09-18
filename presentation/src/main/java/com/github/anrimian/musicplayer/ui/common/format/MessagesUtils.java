package com.github.anrimian.musicplayer.ui.common.format;

import android.content.Context;

import com.github.anrimian.musicplayer.R;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.playlist.PlayList;

import java.util.List;

import static com.github.anrimian.musicplayer.ui.common.format.FormatUtils.formatCompositionName;

public class MessagesUtils {

    public static String getAddToPlayListCompleteMessage(Context context,
                                                         PlayList playList,
                                                         List<Composition> compositions) {
        if (compositions.size() == 1) {
            return context.getString(R.string.add_to_playlist_success_template,
                    formatCompositionName(compositions.get(0)),
                    playList.getName());
        } else {
            return context.getString(R.string.add_to_playlist_count_success_template,
                    compositions.size(),
                    playList.getName());
        }
    }

    public static String getDeleteCompleteMessage(Context context, List<Composition> compositions) {
        if (compositions.size() == 1) {
            return context.getString(R.string.delete_composition_success,
                    formatCompositionName(compositions.get(0)));
        } else {
            return context.getString(R.string.delete_compositions_success, compositions.size());
        }
    }
}
