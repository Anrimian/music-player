package com.github.anrimian.musicplayer.ui.library.folders.adapter;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.composition.folders.CompositionFileSource2;
import com.github.anrimian.musicplayer.domain.models.composition.folders.FileSource2;
import com.github.anrimian.musicplayer.domain.models.composition.folders.FolderFileSource2;
import com.github.anrimian.musicplayer.domain.models.utils.FolderHelper;
import com.github.anrimian.musicplayer.ui.utils.OnPositionItemClickListener;
import com.github.anrimian.musicplayer.ui.utils.OnViewItemClickListener;
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.SelectableViewHolder;
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.diff_utils.SimpleDiffItemCallback;
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.diff_utils.adapter.DiffListAdapter;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.github.anrimian.musicplayer.domain.Payloads.ITEM_SELECTED;
import static com.github.anrimian.musicplayer.domain.Payloads.ITEM_UNSELECTED;

/**
 * Created on 31.10.2017.
 */

public class MusicFileSourceAdapter extends DiffListAdapter<FileSource2, FileViewHolder> {

    private static final int TYPE_MUSIC = 1;
    private static final int TYPE_FILE = 2;

    private final Set<FileViewHolder> viewHolders = new HashSet<>();
    private final HashSet<FileSource2> selectedItems;
    private final HashSet<FileSource2> selectedMoveItems;

    private OnPositionItemClickListener<CompositionFileSource2> onCompositionClickListener;
    private OnPositionItemClickListener<FolderFileSource2> onFolderClickListener;
    private OnPositionItemClickListener<FileSource2> onLongClickListener;
    private OnViewItemClickListener<FolderFileSource2> onFolderMenuClickListener;
    private OnPositionItemClickListener<Composition> compositionIconClickListener;

    @Nullable
    private Composition currentComposition;
    private boolean play;
    private boolean isCoversEnabled;

    public MusicFileSourceAdapter(RecyclerView recyclerView,
                                  HashSet<FileSource2> selectedItems,
                                  HashSet<FileSource2> selectedMoveItems) {
        super(recyclerView, new SimpleDiffItemCallback<>(
                FolderHelper::areSourcesTheSame,
                FolderHelper::getChangePayload)
        );
        this.selectedItems = selectedItems;
        this.selectedMoveItems = selectedMoveItems;
    }

    @NonNull
    @Override
    public FileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int type) {
        switch (type) {
            case TYPE_MUSIC: {
                return new MusicFileViewHolder(parent,
                        onCompositionClickListener,
                        onLongClickListener,
                        compositionIconClickListener);
            }
            case TYPE_FILE: {
                return new FolderViewHolder(parent,
                        onFolderClickListener,
                        onFolderMenuClickListener,
                        onLongClickListener);
            }
            default: throw new IllegalStateException("unexpected item type: " + type);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull FileViewHolder holder, int position) {
        viewHolders.add(holder);

        FileSource2 fileSource = getItem(position);

        boolean selected = selectedItems.contains(fileSource);
        holder.setSelected(selected);

        boolean selectedToMove = selectedMoveItems.contains(fileSource);
        holder.setSelectedToMove(selectedToMove);

        switch (holder.getItemViewType()) {
            case TYPE_MUSIC: {
                MusicFileViewHolder musicViewHolder = (MusicFileViewHolder) holder;
                CompositionFileSource2 musicFileSource = (CompositionFileSource2) fileSource;
                musicViewHolder.bind(musicFileSource, isCoversEnabled);

                Composition composition = musicFileSource.getComposition();
                boolean isCurrentComposition = composition.equals(currentComposition);
                musicViewHolder.showAsCurrentComposition(isCurrentComposition);
                musicViewHolder.showAsPlaying(isCurrentComposition && play);
                break;
            }
            case TYPE_FILE: {
                FolderViewHolder folderViewHolder = (FolderViewHolder) holder;
                FolderFileSource2 folderFileSource = (FolderFileSource2) fileSource;
                folderViewHolder.bind(folderFileSource);
                break;
            }
        }
    }

    @Override
    public void onBindViewHolder(@NonNull FileViewHolder holder,
                                 int position,
                                 @NonNull List<Object> payloads) {
        if (payloads.isEmpty()) {
            onBindViewHolder(holder, position);
            return;
        }
        FileSource2 fileSource = getItem(position);
        switch (holder.getItemViewType()) {
            case TYPE_MUSIC: {
                MusicFileViewHolder musicViewHolder = (MusicFileViewHolder) holder;
                CompositionFileSource2 musicFileSource = (CompositionFileSource2) fileSource;
                musicViewHolder.update(musicFileSource, payloads);
                break;
            }
            case TYPE_FILE: {
                FolderViewHolder folderViewHolder = (FolderViewHolder) holder;
                FolderFileSource2 folderFileSource = (FolderFileSource2) fileSource;
                folderViewHolder.update(folderFileSource, payloads);
                break;
            }
        }
    }

    @Override
    public void onViewRecycled(@NonNull FileViewHolder holder) {
        super.onViewRecycled(holder);
        viewHolders.remove(holder);
    }

    @Override
    public int getItemViewType(int position) {
        FileSource2 source = getItem(position);
        if (source instanceof FolderFileSource2) {
            return TYPE_FILE;
        } else {
            return TYPE_MUSIC;
        }
    }

    public void setItemSelected(int position) {
        notifyItemChanged(position, ITEM_SELECTED);
    }

    public void setItemUnselected(int position) {
        notifyItemChanged(position, ITEM_UNSELECTED);
    }

    public void setItemsSelected(boolean selected) {
        for (SelectableViewHolder holder: viewHolders) {
            holder.setSelected(selected);
        }
    }

    public void setOnCompositionClickListener(OnPositionItemClickListener<CompositionFileSource2> onCompositionClickListener) {
        this.onCompositionClickListener = onCompositionClickListener;
    }

    public void setOnFolderClickListener(OnPositionItemClickListener<FolderFileSource2> onFolderClickListener) {
        this.onFolderClickListener = onFolderClickListener;
    }

    public void setOnLongClickListener(OnPositionItemClickListener<FileSource2> onLongClickListener) {
        this.onLongClickListener = onLongClickListener;
    }

    public void setOnFolderMenuClickListener(OnViewItemClickListener<FolderFileSource2> onFolderMenuClickListener) {
        this.onFolderMenuClickListener = onFolderMenuClickListener;
    }

    public void setCompositionIconClickListener(OnPositionItemClickListener<Composition> compositionIconClickListener) {
        this.compositionIconClickListener = compositionIconClickListener;
    }

    public void showCurrentComposition(Composition currentComposition) {
        this.currentComposition = currentComposition;
        for (RecyclerView.ViewHolder holder: viewHolders) {
            if (holder instanceof MusicFileViewHolder) {
                MusicFileViewHolder musicViewHolder = (MusicFileViewHolder) holder;

                Composition composition = musicViewHolder.getComposition();
                boolean isCurrentComposition = composition.equals(currentComposition);
                musicViewHolder.showAsCurrentComposition(isCurrentComposition);
                musicViewHolder.showAsPlaying(isCurrentComposition && play);
            }
        }
    }

    public void showPlaying(boolean play) {
        this.play = play;
        for (RecyclerView.ViewHolder holder: viewHolders) {
            if (holder instanceof MusicFileViewHolder) {
                MusicFileViewHolder musicViewHolder = (MusicFileViewHolder) holder;
                Composition composition = musicViewHolder.getComposition();
                boolean isCurrentComposition = composition.equals(currentComposition);
                musicViewHolder.showAsCurrentComposition(isCurrentComposition);
                musicViewHolder.showAsPlaying(isCurrentComposition && play);
            }
        }
    }

    public void setCoversEnabled(boolean isCoversEnabled) {
        this.isCoversEnabled = isCoversEnabled;
        for (RecyclerView.ViewHolder holder: viewHolders) {
            if (holder instanceof MusicFileViewHolder) {
                MusicFileViewHolder musicViewHolder = (MusicFileViewHolder) holder;
                musicViewHolder.setCoversVisible(isCoversEnabled);
            }
        }
    }

    public void updateItemsToMove() {
        for (FileViewHolder holder: viewHolders) {
            boolean selectedToMove = selectedMoveItems.contains(holder.getFileSource());
            holder.setSelectedToMove(selectedToMove);
        }
    }
}
