package com.github.anrimian.musicplayer.ui.common.format.wrappers;

import android.content.Context;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.anrimian.musicplayer.R;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.ui.common.format.ImageFormatUtils;

import androidx.annotation.Nullable;
import butterknife.BindView;
import butterknife.ButterKnife;

import static com.github.anrimian.musicplayer.ui.common.format.ColorFormatUtils.getItemDragColor;
import static com.github.anrimian.musicplayer.ui.common.format.ColorFormatUtils.getPlayingCompositionColor;
import static com.github.anrimian.musicplayer.ui.common.format.FormatUtils.formatCompositionAuthor;
import static com.github.anrimian.musicplayer.ui.common.format.FormatUtils.formatCompositionName;
import static com.github.anrimian.musicplayer.ui.common.format.FormatUtils.formatMilliseconds;
import static com.github.anrimian.musicplayer.ui.utils.AndroidUtils.getColorFromAttr;
import static com.github.anrimian.musicplayer.ui.utils.ViewUtils.animateBackgroundColor;
import static com.github.anrimian.musicplayer.ui.utils.ViewUtils.animateVisibility;

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

    @BindView(R.id.btn_actions_menu)
    View btnActionsMenu;

    @BindView(R.id.divider)
    View divider;

    private Composition composition;

    private boolean isPlaying;
    private boolean isDragging;

    public CompositionItemWrapper(View itemView) {
        ButterKnife.bind(this, itemView);
    }

    public void bind(Composition composition) {
        this.composition = composition;
        String compositionName = formatCompositionName(composition);
        tvMusicName.setText(compositionName);
        clickableItem.setContentDescription(compositionName);
        btnActionsMenu.setContentDescription(getContext().getString(
                R.string.content_description_menu_template, compositionName));
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

    public void showAsDraggingItem(boolean dragging) {
        if (this.isDragging != dragging) {
            this.isDragging = dragging;

            animateVisibility(divider, dragging? View.INVISIBLE: View.VISIBLE);
            if (!dragging && isPlaying) {
                showAsPlaying(true);
            } else {
                showAsDragging(dragging);
            }
        }
    }

    public void showAsPlayingComposition(boolean isPlaying) {
        if (this.isPlaying != isPlaying) {
            this.isPlaying = isPlaying;
            if (!isPlaying && isDragging) {
                showAsDragging(true);
                return;
            }
            showAsPlaying(isPlaying);
        }
    }

    private void showAsDragging(boolean dragging) {
        int endColor = getItemDragColor(getContext(), dragging? 20: 0);
        animateBackgroundColor(clickableItem, endColor);
    }

    private void showAsPlaying(boolean isPlaying) {
        int endColor = getPlayingCompositionColor(getContext(), isPlaying? 20: 0);
        animateBackgroundColor(clickableItem, endColor);

        clickableItem.setClickable(!isPlaying);
    }

    private void showAdditionalInfo() {
        StringBuilder sb = formatCompositionAuthor(composition, getContext());
        sb.append(" ● ");//TODO split problem • ●
        sb.append(formatMilliseconds(composition.getDuration()));
        tvAdditionalInfo.setText(sb.toString());
    }

    private Context getContext() {
        return clickableItem.getContext();
    }
}
