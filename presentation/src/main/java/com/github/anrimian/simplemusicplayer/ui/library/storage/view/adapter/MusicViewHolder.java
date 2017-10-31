package com.github.anrimian.simplemusicplayer.ui.library.storage.view.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.github.anrimian.simplemusicplayer.R;
import com.github.anrimian.simplemusicplayer.domain.models.Composition;

import javax.annotation.Nonnull;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created on 31.10.2017.
 */

class MusicViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.tv_music_name)
    TextView tvMusicName;

    MusicViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    void bind(@Nonnull Composition composition) {
        tvMusicName.setText(composition.getDisplayName());
    }
}
