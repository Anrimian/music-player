package com.github.anrimian.musicplayer.ui.widgets.providers;

import android.content.Context;
import android.widget.RemoteViews;

import com.github.anrimian.musicplayer.R;
import com.github.anrimian.musicplayer.ui.common.format.ImageFormatUtils;


public class WidgetProviderMedium extends BaseWidgetProvider {

    @Override
    protected void applyViewLogic(RemoteViews widgetView,
                                  Context context,
                                  boolean play,
                                  String compositionName,
                                  String compositionAuthor,
                                  String compositionFile,
                                  long compositionId,
                                  int queueSize,
                                  boolean enabled,
                                  boolean showCovers) {
        super.applyViewLogic(widgetView,
                context,
                play,
                compositionName,
                compositionAuthor,
                compositionFile,
                compositionId,
                queueSize,
                enabled,
                showCovers);
        widgetView.setBoolean(R.id.iv_shuffle_mode, "setEnabled", enabled);
        widgetView.setBoolean(R.id.iv_repeat_mode, "setEnabled", enabled);

        if (showCovers) {
            ImageFormatUtils.displayImage(widgetView, R.id.iv_cover, compositionFile, compositionId);
        } else {
            widgetView.setImageViewResource(R.id.iv_cover, R.drawable.ic_music_placeholder);
        }
    }

    @Override
    protected int getRemoteViewId() {
        return R.layout.widget_medium;
    }
}
