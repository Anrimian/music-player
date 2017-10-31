package com.github.anrimian.simplemusicplayer.ui.library.storage.view.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.anrimian.simplemusicplayer.R;
import com.github.anrimian.simplemusicplayer.domain.models.MusicFileSource;
import com.github.anrimian.simplemusicplayer.utils.recycler_view.HeaderFooterRecyclerViewAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created on 31.10.2017.
 */

public class MusicFileSourceAdapter extends HeaderFooterRecyclerViewAdapter {

    private static final int TYPE_MUSIC = 1;
    private static final int TYPE_FILE = 2;

    private List<MusicFileSource> musicList = new ArrayList<>();

    public void setMusicList(List<MusicFileSource> musicList) {
        this.musicList = musicList;
    }

    @Override
    public RecyclerView.ViewHolder createVH(ViewGroup parent, int type) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch (type) {
            case TYPE_MUSIC: {
                View view = inflater.inflate(R.layout.item_storage_music, parent, false);
                return new MusicViewHolder(view);
            }
            case TYPE_FILE: {
                View view = inflater.inflate(R.layout.item_storage_folder, parent, false);
                return new FolderViewHolder(view);
            }
            default: throw new IllegalStateException("unexpected item type: " + type);
        }
    }

    @Override
    public void bindVH(RecyclerView.ViewHolder holder, int position) {
        MusicFileSource musicFileSource = musicList.get(position);
        switch (holder.getItemViewType()) {
            case TYPE_MUSIC: {
                MusicViewHolder musicViewHolder = (MusicViewHolder) holder;
                musicViewHolder.bind(musicFileSource.getComposition());
                break;
            }
            case TYPE_FILE: {
                FolderViewHolder folderViewHolder = (FolderViewHolder) holder;
                folderViewHolder.bind(musicFileSource.getPath());
                break;
            }
        }
    }

    @Override
    public int getCount() {
        return musicList.size();
    }

    @Override
    protected int getItemType(int position) {
        MusicFileSource source = musicList.get(position);
        if (source.getComposition() == null) {
            return TYPE_FILE;
        } else {
            return TYPE_MUSIC;
        }
    }
}
