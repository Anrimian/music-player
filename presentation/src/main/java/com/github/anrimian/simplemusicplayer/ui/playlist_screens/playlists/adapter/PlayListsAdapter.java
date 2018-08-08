package com.github.anrimian.simplemusicplayer.ui.playlist_screens.playlists.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.github.anrimian.simplemusicplayer.domain.models.playlist.PlayList;
import com.github.anrimian.simplemusicplayer.ui.utils.OnItemClickListener;
import com.github.anrimian.simplemusicplayer.ui.utils.views.recycler_view.diff_utils.SimpleDiffCallback;

import java.util.List;

import static android.support.v7.util.DiffUtil.calculateDiff;
import static com.github.anrimian.simplemusicplayer.domain.models.utils.PlayListHelper.hasChanges;

public class PlayListsAdapter extends RecyclerView.Adapter<PlayListViewHolder> {

    private final List<PlayList> playLists;

    private OnItemClickListener<PlayList> onItemClickListener;

    public PlayListsAdapter(List<PlayList> playLists) {
        this.playLists = playLists;
    }

    @Override
    public PlayListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new PlayListViewHolder(LayoutInflater.from(parent.getContext()),
                parent,
                onItemClickListener);
    }

    @Override
    public void onBindViewHolder(PlayListViewHolder holder, int position) {
        PlayList playList = playLists.get(position);
        holder.bind(playList);
    }

    @Override
    public int getItemCount() {
        return playLists.size();
    }

    public void setOnItemClickListener(OnItemClickListener<PlayList> onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public void updateList(List<PlayList> oldList, List<PlayList> sourceList) {
        calculateDiff(new SimpleDiffCallback<>(oldList, sourceList, this::areSourcedTheSame), false)
                .dispatchUpdatesTo(this);
    }

    private boolean areSourcedTheSame(PlayList oldSource, PlayList newSource) {
        return !hasChanges(oldSource, newSource);
    }
}
