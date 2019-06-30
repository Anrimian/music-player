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
                                  int queueSize) {
        super.applyViewLogic(widgetView,
                context,
                play,
                compositionName,
                compositionAuthor,
                compositionFile,
                compositionId,
                queueSize);

        //TODO cover setting
        //TODO image loading issue(loaded here but not in activity)
        ImageFormatUtils.displayImage(widgetView, R.id.iv_cover, compositionFile, compositionId);
    }

    @Override
    protected int getRemoveViewId() {
        return R.layout.widget_medium;
    }
}
