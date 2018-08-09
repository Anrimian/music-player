package com.github.anrimian.simplemusicplayer.ui.utils.slidr;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.ViewGroup;

import com.r0adkll.slidr.model.SlidrConfig;
import com.r0adkll.slidr.model.SlidrInterface;
import com.r0adkll.slidr.widget.SliderPanel;

public class SlidrFragment {

    @NonNull
    public static SlidrInterface replace(@NonNull final Fragment fragment,
                                         @NonNull final View oldScreen,
                                         @NonNull final SlidrConfig config) {
        ViewGroup parent = (ViewGroup) oldScreen.getParent();
        ViewGroup.LayoutParams params = oldScreen.getLayoutParams();
        parent.removeView(oldScreen);

        // Setup the slider panel and attach it
        final SliderPanel panel = new SliderPanel(oldScreen.getContext(), oldScreen, config);
        panel.setId(com.r0adkll.slidr.R.id.slidable_panel);
        oldScreen.setId(com.r0adkll.slidr.R.id.slidable_content);

        panel.addView(oldScreen);
        parent.addView(panel, 0, params);

        // Set the panel slide listener for when it becomes closed or opened
        panel.setOnPanelSlideListener(new FragmentPanelSlideListener(fragment, config));

        // Return the lock interface
        return panel.getDefaultInterface();
    }
}
