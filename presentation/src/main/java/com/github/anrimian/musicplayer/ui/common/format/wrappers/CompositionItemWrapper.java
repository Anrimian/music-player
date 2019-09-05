package com.github.anrimian.musicplayer.ui.common.format.wrappers;

import android.content.Context;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.github.anrimian.musicplayer.R;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.utils.java.Callback;
import com.github.anrimian.musicplayer.ui.common.format.ImageFormatUtils;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.github.anrimian.musicplayer.domain.Payloads.ARTIST;
import static com.github.anrimian.musicplayer.domain.Payloads.CORRUPTED;
import static com.github.anrimian.musicplayer.domain.Payloads.DURATION;
import static com.github.anrimian.musicplayer.domain.Payloads.PATH;
import static com.github.anrimian.musicplayer.domain.Payloads.TITLE;
import static com.github.anrimian.musicplayer.domain.models.composition.CompositionModelHelper.formatCompositionName;
import static com.github.anrimian.musicplayer.ui.common.format.ColorFormatUtils.getItemDragColor;
import static com.github.anrimian.musicplayer.ui.common.format.ColorFormatUtils.getPlayingCompositionColor;
import static com.github.anrimian.musicplayer.ui.common.format.FormatUtils.formatCompositionAuthor;
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
    @BindView(R.id.iv_play)
    ImageView ivPlay;

    @Nullable
    @BindView(R.id.iv_music_icon)
    ImageView ivMusicIcon;

    @BindView(R.id.btn_actions_menu)
    View btnActionsMenu;

    @BindView(R.id.divider)
    View divider;

    private Composition composition;

    private boolean isCurrent;
    private boolean isDragging;

    public CompositionItemWrapper(View itemView, Callback<Composition> onIconClickListener) {
        ButterKnife.bind(this, itemView);
        if (ivMusicIcon != null) {
            ivMusicIcon.setOnClickListener(v -> onIconClickListener.call(composition));
        }
    }

    public void bind(Composition composition, boolean showCovers) {
        this.composition = composition;
        showCompositionName();
        showAdditionalInfo();
        showCorrupted();
        showCompositionImage(showCovers);

        showAsPlaying(false);
    }

    public void update(Composition composition, List<Object> payloads) {
        this.composition = composition;
        for (Object payload: payloads) {
            if (payload instanceof List) {
                //noinspection SingleStatementInBlock,unchecked
                update(composition, (List) payload);
            }
            if (payload == PATH || payload == TITLE) {
                showCompositionName();
            }
            if (payload == ARTIST || payload == DURATION) {
                showAdditionalInfo();
            }
            if (payload == CORRUPTED) {
                showCorrupted();
            }
        }
    }

    public void showCompositionImage(boolean showCovers) {
        if (ivMusicIcon != null) {
            if (showCovers) {
                ImageFormatUtils.displayImage(ivMusicIcon, composition);
            } else {
                ivMusicIcon.setImageResource(R.drawable.ic_music_placeholder);
            }
        }
    }

//    public void showNumber(int number) {//good idea
//        tvAdditionalInfo.setText(String.valueOf(number) +" ● " + tvAdditionalInfo.getText());
//    }

    public void showAsDraggingItem(boolean dragging) {
        if (this.isDragging != dragging) {
            this.isDragging = dragging;

            animateVisibility(divider, dragging? View.INVISIBLE: View.VISIBLE);
            if (!dragging && isCurrent) {
                showAsCurrentCompositionInternal(true);
            } else {
                showAsDragging(dragging);
            }
        }
    }

    public void showAsCurrentComposition(boolean isCurrent) {
        if (this.isCurrent != isCurrent) {
            this.isCurrent = isCurrent;
            showAsCurrentCompositionInternal(isCurrent);
        }
    }

    public void showAsPlaying(boolean isPlaying) {
        if (ivPlay != null) {
            ivPlay.setImageResource(isPlaying ? R.drawable.ic_pause : R.drawable.ic_play);
        }
    }

    private void showAsCurrentCompositionInternal(boolean isPlaying) {
        if (!isPlaying && isDragging) {
            showAsDragging(true);
            return;
        }
        int endColor = getPlayingCompositionColor(getContext(), isPlaying? 20: 0);
        animateBackgroundColor(clickableItem, endColor);
        clickableItem.setClickable(!isPlaying);
    }

    private void showCompositionName() {
        String compositionName = formatCompositionName(composition);
        tvMusicName.setText(compositionName);
        clickableItem.setContentDescription(compositionName);
        btnActionsMenu.setContentDescription(getContext().getString(
                R.string.content_description_menu_template, compositionName));
    }

    private void showCorrupted() {
        int textColorAttr = composition.isCorrupted()? android.R.attr.textColorSecondary:
                android.R.attr.textColorPrimary;
        tvMusicName.setTextColor(getColorFromAttr(getContext(), textColorAttr));
    }


    private void showAsDragging(boolean dragging) {
        int endColor = getItemDragColor(getContext(), dragging? 20: 0);
        animateBackgroundColor(clickableItem, endColor);
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
