package com.github.anrimian.musicplayer.ui.library.folders.adapter;

import android.os.Parcelable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.anrimian.musicplayer.R;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.composition.folders.FileSource;
import com.github.anrimian.musicplayer.domain.models.composition.folders.FolderFileSource;
import com.github.anrimian.musicplayer.domain.models.composition.folders.MusicFileSource;
import com.github.anrimian.musicplayer.domain.models.utils.CompositionHelper;
import com.github.anrimian.musicplayer.domain.models.utils.FolderHelper;
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.diff_utils.SimpleDiffCallback;
import com.github.anrimian.musicplayer.ui.utils.OnItemClickListener;
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.OnTransitionItemClickListener;
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.endless_scrolling.HeaderFooterRecyclerViewAdapter;

import java.util.List;

import io.reactivex.Scheduler;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

import static android.support.v7.util.DiffUtil.calculateDiff;

/**
 * Created on 31.10.2017.
 */

public class MusicFileSourceAdapter extends HeaderFooterRecyclerViewAdapter {

    private static final int TYPE_MUSIC = 1;
    private static final int TYPE_FILE = 2;

    private final List<FileSource> musicList;
    private OnItemClickListener<Composition> onCompositionClickListener;
    private OnItemClickListener<Composition> onDeleteCompositionClickListener;
    private OnItemClickListener<Composition> onAddToPlaylistClickListener;
    private OnItemClickListener<String> onFolderClickListener;
    private OnItemClickListener<String> onAddFolderToPlaylistClickListener;
    private OnItemClickListener<FolderFileSource> onDeleteFolderClickListener;

    public MusicFileSourceAdapter(List<FileSource> musicList) {
        this.musicList = musicList;
    }

    @Override
    public RecyclerView.ViewHolder createVH(ViewGroup parent, int type) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch (type) {
            case TYPE_MUSIC: {
                return new MusicViewHolder(inflater,
                        parent,
                        onCompositionClickListener,
                        onDeleteCompositionClickListener,
                        onAddToPlaylistClickListener);
            }
            case TYPE_FILE: {
                return new FolderViewHolder(inflater,
                        parent,
                        onFolderClickListener,
                        onDeleteFolderClickListener,
                        onAddFolderToPlaylistClickListener);
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

    public void setOnCompositionClickListener(OnItemClickListener<Composition> onCompositionClickListener) {
        this.onCompositionClickListener = onCompositionClickListener;
    }

    public void setOnFolderClickListener(OnItemClickListener<String> onFolderClickListener) {
        this.onFolderClickListener = onFolderClickListener;
    }

    public void setOnDeleteCompositionClickListener(OnItemClickListener<Composition> onDeleteCompositionClickListener) {
        this.onDeleteCompositionClickListener = onDeleteCompositionClickListener;
    }

    public void setOnAddToPlaylistClickListener(OnItemClickListener<Composition> onAddToPlaylistClickListener) {
        this.onAddToPlaylistClickListener = onAddToPlaylistClickListener;
    }

    public void setOnDeleteFolderClickListener(OnItemClickListener<FolderFileSource> onDeleteFolderClickListener) {
        this.onDeleteFolderClickListener = onDeleteFolderClickListener;
    }

    public void setOnAddFolderToPlaylistClickListener(OnItemClickListener<String> onAddFolderToPlaylistClickListener) {
        this.onAddFolderToPlaylistClickListener = onAddFolderToPlaylistClickListener;
    }
}
