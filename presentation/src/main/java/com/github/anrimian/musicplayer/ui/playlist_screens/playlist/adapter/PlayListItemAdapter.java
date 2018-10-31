package com.github.anrimian.musicplayer.ui.playlist_screens.playlist.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.github.anrimian.musicplayer.domain.models.playlist.PlayListItem;
import com.github.anrimian.musicplayer.ui.utils.OnItemClickListener;
import com.github.anrimian.musicplayer.ui.utils.OnViewItemClickListener;

import java.util.List;

/**
 * Created on 31.10.2017.
 */

public class PlayListItemAdapter extends RecyclerView.Adapter<PlayListItemViewHolder> {

    private final List<PlayListItem> musicList;
    private OnItemClickListener<PlayListItem> onCompositionClickListener;
    private OnViewItemClickListener<PlayListItem> onMenuItemClickListener;

    public PlayListItemAdapter(List<PlayListItem> musicList) {
        this.musicList = musicList;
    }

    @NonNull
    @Override
    public PlayListItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new PlayListItemViewHolder(LayoutInflater.from(parent.getContext()),
                parent,
                onCompositionClickListener,
                onMenuItemClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull PlayListItemViewHolder holder, int position) {
        PlayListItem composition = musicList.get(position);
        holder.bind(composition);
    }

    @Override
    public int getItemCount() {
        return musicList.size();
    }

    public void setOnCompositionClickListener(OnItemClickListener<PlayListItem> onCompositionClickListener) {
        this.onCompositionClickListener = onCompositionClickListener;
    }

    public void setOnMenuItemClickListener(OnViewItemClickListener<PlayListItem> onMenuItemClickListener) {
        this.onMenuItemClickListener = onMenuItemClickListener;
    }
}
