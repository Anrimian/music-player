package com.github.anrimian.musicplayer.ui.playlist_screens.playlists.adapter;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.github.anrimian.musicplayer.domain.models.playlist.PlayList;
import com.github.anrimian.musicplayer.ui.utils.OnItemClickListener;

import java.util.List;

public class PlayListsAdapter extends RecyclerView.Adapter<PlayListViewHolder> {

    private List<PlayList> playLists;

    private OnItemClickListener<PlayList> onItemClickListener;

    public PlayListsAdapter(List<PlayList> playLists) {
        this.playLists = playLists;
    }

    @NonNull
    @Override
    public PlayListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new PlayListViewHolder(LayoutInflater.from(parent.getContext()),
                parent,
                onItemClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull PlayListViewHolder holder, int position) {
        PlayList playList = playLists.get(position);
        holder.bind(playList);
    }

    @Override
    public int getItemCount() {
        return playLists.size();
    }

    public void setItems(List<PlayList> list) {
        playLists = list;
    }

    public void setOnItemClickListener(OnItemClickListener<PlayList> onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }
}


