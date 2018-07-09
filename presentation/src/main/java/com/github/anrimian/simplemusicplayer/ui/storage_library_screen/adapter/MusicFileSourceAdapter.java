package com.github.anrimian.simplemusicplayer.ui.storage_library_screen.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.anrimian.simplemusicplayer.R;
import com.github.anrimian.simplemusicplayer.domain.models.composition.Composition;
import com.github.anrimian.simplemusicplayer.domain.models.composition.folders.FileSource;
import com.github.anrimian.simplemusicplayer.domain.models.composition.folders.FolderFileSource;
import com.github.anrimian.simplemusicplayer.domain.models.composition.folders.MusicFileSource;
import com.github.anrimian.simplemusicplayer.utils.OnItemClickListener;
import com.github.anrimian.simplemusicplayer.ui.utils.views.recycler_view.OnTransitionItemClickListener;
import com.github.anrimian.simplemusicplayer.ui.utils.views.recycler_view.endless_scrolling.HeaderFooterRecyclerViewAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created on 31.10.2017.
 */

public class MusicFileSourceAdapter extends HeaderFooterRecyclerViewAdapter {

    private static final int TYPE_MUSIC = 1;
    private static final int TYPE_FILE = 2;

    private List<FileSource> musicList = new ArrayList<>();
    private OnItemClickListener<Composition> onCompositionClickListener;
    private OnTransitionItemClickListener<String> onFolderClickListener;

    public MusicFileSourceAdapter(List<FileSource> musicList) {
        this.musicList = musicList;
    }

    public void setOnCompositionClickListener(OnItemClickListener<Composition> onCompositionClickListener) {
        this.onCompositionClickListener = onCompositionClickListener;
    }

    public void setOnFolderClickListener(OnTransitionItemClickListener<String> onFolderClickListener) {
        this.onFolderClickListener = onFolderClickListener;
    }

    @Override
    public RecyclerView.ViewHolder createVH(ViewGroup parent, int type) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch (type) {
            case TYPE_MUSIC: {
                View view = inflater.inflate(R.layout.item_storage_music, parent, false);
                return new MusicViewHolder(view, onCompositionClickListener);
            }
            case TYPE_FILE: {
                View view = inflater.inflate(R.layout.item_storage_folder, parent, false);
                return new FolderViewHolder(view, onFolderClickListener);
            }
            default: throw new IllegalStateException("unexpected item type: " + type);
        }
    }

    @Override
    public void bindVH(RecyclerView.ViewHolder holder, int position) {
        FileSource fileSource = musicList.get(position);
        switch (holder.getItemViewType()) {
            case TYPE_MUSIC: {
                MusicViewHolder musicViewHolder = (MusicViewHolder) holder;
                MusicFileSource musicFileSource = (MusicFileSource) fileSource;
                musicViewHolder.bind(musicFileSource.getComposition());
                break;
            }
            case TYPE_FILE: {
                FolderViewHolder folderViewHolder = (FolderViewHolder) holder;
                FolderFileSource folderFileSource = (FolderFileSource) fileSource;
                folderViewHolder.bind(folderFileSource);
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
        FileSource source = musicList.get(position);
        if (source instanceof FolderFileSource) {
            return TYPE_FILE;
        } else {
            return TYPE_MUSIC;
        }
    }
}
