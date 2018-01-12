package com.github.anrimian.simplemusicplayer.ui.library.storage.view.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.github.anrimian.simplemusicplayer.R;
import com.github.anrimian.simplemusicplayer.domain.models.Composition;
import com.github.anrimian.simplemusicplayer.utils.OnItemClickListener;

import javax.annotation.Nonnull;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.text.TextUtils.isEmpty;
import static com.github.anrimian.simplemusicplayer.utils.format.FormatUtils.formatMilliseconds;

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
        tvMusicName.setText(composition.getTitle());
        showAdditionalInfo();
    }

    private void showAdditionalInfo() {
        String author = composition.getArtist();

        StringBuilder sb = new StringBuilder();
        if (!isEmpty(author) && !author.equals("<unknown>")) {
            sb.append(author);
            sb.append(" ● ");//TODO split problem
        }
        sb.append(formatMilliseconds(composition.getDuration()));
        tvAdditionalInfo.setText(sb.toString());
    }
}
