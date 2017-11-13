package com.github.anrimian.simplemusicplayer.ui.library.main.view.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.github.anrimian.simplemusicplayer.R;
import com.github.anrimian.simplemusicplayer.domain.models.Composition;
import com.github.anrimian.simplemusicplayer.utils.OnItemClickListener;

import javax.annotation.Nonnull;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created on 31.10.2017.
 */

class MusicViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.tv_music_name)
    TextView tvMusicName;

    private Composition composition;

    MusicViewHolder(View itemView, OnItemClickListener<Composition> onCompositionClickListener) {
        super(itemView);
        ButterKnife.bind(this, itemView);
        if (onCompositionClickListener != null) {
            itemView.setOnClickListener(v -> onCompositionClickListener.onItemClick(composition));
        }
    }

    void bind(@Nonnull Composition composition) {
        this.composition = composition;
        tvMusicName.setText(composition.getDisplayName());
    }
}
