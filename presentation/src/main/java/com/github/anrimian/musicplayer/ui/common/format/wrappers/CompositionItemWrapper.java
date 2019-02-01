package com.github.anrimian.musicplayer.ui.common.format.wrappers;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.anrimian.musicplayer.R;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.ui.common.format.ImageFormatUtils;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;
import butterknife.BindView;
import butterknife.ButterKnife;

import static androidx.core.graphics.ColorUtils.setAlphaComponent;
import static com.github.anrimian.musicplayer.ui.common.format.FormatUtils.formatCompositionAuthor;
import static com.github.anrimian.musicplayer.ui.common.format.FormatUtils.formatCompositionName;
import static com.github.anrimian.musicplayer.ui.common.format.FormatUtils.formatMilliseconds;
import static com.github.anrimian.musicplayer.ui.utils.AndroidUtils.getColorFromAttr;
import static com.github.anrimian.musicplayer.ui.utils.ViewUtils.animateColor;

public class CompositionItemWrapper {

    @BindView(R.id.tv_composition_name)
    TextView tvMusicName;

    @BindView(R.id.tv_additional_info)
    TextView tvAdditionalInfo;

    @BindView(R.id.clickable_item)
    FrameLayout clickableItem;

    @Nullable
    @BindView(R.id.iv_music_icon)
    ImageView ivMusicIcon;

    private Drawable foregroundDrawable;

    private Composition composition;

    private boolean isPlaying;

    public CompositionItemWrapper(View itemView) {
        ButterKnife.bind(this, itemView);
    }

    public void bind(Composition composition) {
        this.composition = composition;
        String compositionName = formatCompositionName(composition);
        tvMusicName.setText(compositionName);
        clickableItem.setContentDescription(compositionName);
        showAdditionalInfo();

        int textColorAttr = composition.isCorrupted()? android.R.attr.textColorSecondary:
                android.R.attr.textColorPrimary;

        tvMusicName.setTextColor(getColorFromAttr(getContext(), textColorAttr));

        if (ivMusicIcon != null) {
            ImageFormatUtils.displayImage(ivMusicIcon, composition);
        }
    }

//    public void showNumber(int number) {//good idea
//        tvAdditionalInfo.setText(String.valueOf(number) +" ● " + tvAdditionalInfo.getText());
//    }

    public void showAsPlayingComposition(boolean isPlaying) {
        if (this.isPlaying != isPlaying) {
            this.isPlaying = isPlaying;
            int unselectedColor = getSelectionColor(0);
            int selectedColor = getSelectionColor(20);
            int startColor = isPlaying ? unselectedColor : selectedColor;
            int endColor = isPlaying ? selectedColor : unselectedColor;
            animateColor(startColor, endColor, color -> clickableItem.setBackgroundColor(color));

            clickableItem.setClickable(!isPlaying);
        }
    }

    private void showAdditionalInfo() {
        StringBuilder sb = formatCompositionAuthor(composition, getContext());
        sb.append(" ● ");//TODO split problem • ●
        sb.append(formatMilliseconds(composition.getDuration()));
        tvAdditionalInfo.setText(sb.toString());
    }

    @ColorInt
    private int getSelectionColor(int alpha) {
        return setAlphaComponent(getColorFromAttr(getContext(), R.attr.colorPrimary), alpha);
    }

    private Context getContext() {
        return clickableItem.getContext();
    }
}
