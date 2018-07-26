package com.github.anrimian.simplemusicplayer.ui.player_screens.player_screen.view.adapter;

import android.content.Context;
import android.support.annotation.StringRes;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.github.anrimian.simplemusicplayer.R;
import com.github.anrimian.simplemusicplayer.domain.models.composition.Composition;
import com.github.anrimian.simplemusicplayer.ui.utils.OnItemClickListener;

import javax.annotation.Nonnull;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.support.v4.graphics.ColorUtils.setAlphaComponent;
import static com.github.anrimian.simplemusicplayer.ui.common.format.FormatUtils.formatCompositionAuthor;
import static com.github.anrimian.simplemusicplayer.ui.common.format.FormatUtils.formatCompositionName;
import static com.github.anrimian.simplemusicplayer.ui.common.format.FormatUtils.formatMilliseconds;
import static com.github.anrimian.simplemusicplayer.utils.AndroidUtils.getColorFromAttr;

/**
 * Created on 31.10.2017.
 */

class MusicViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.tv_composition_name)
    TextView tvMusicName;

    @BindView(R.id.tv_additional_info)
    TextView tvAdditionalInfo;

    @BindView(R.id.clickable_item)
    View clickableItem;

    private Composition composition;

    MusicViewHolder(View itemView, OnItemClickListener<Composition> onCompositionClickListener) {
        super(itemView);
        ButterKnife.bind(this, itemView);
        if (onCompositionClickListener != null) {
            clickableItem.setOnClickListener(v -> onCompositionClickListener.onItemClick(composition));
        }
    }

    void bind(@Nonnull Composition composition) {
        this.composition = composition;
        tvMusicName.setText(formatCompositionName(composition));
        showAdditionalInfo();

        int textColorAttr = composition.isCorrupted()? android.R.attr.textColorSecondary:
                android.R.attr.textColorPrimary;

        tvMusicName.setTextColor(getColorFromAttr(getContext(), textColorAttr));
    }

    void showAsPlayingComposition(boolean show) {
        int alpha = show? 20: 0;
        int color = setAlphaComponent(getColorFromAttr(getContext(), R.attr.colorPrimary), alpha);
        clickableItem.setBackgroundColor(color);
        clickableItem.setClickable(!show);
    }

    private void showAdditionalInfo() {
        StringBuilder sb = formatCompositionAuthor(composition, getContext());
        sb.append(" ● ");//TODO split problem
        sb.append(formatMilliseconds(composition.getDuration()));
        tvAdditionalInfo.setText(sb.toString());
    }

    private String getString(@StringRes int resId) {
        return getContext().getString(resId);
    }

    private Context getContext() {
        return itemView.getContext();
    }


}
