package com.github.anrimian.simplemusicplayer.ui.playlist_screens.playlists.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.anrimian.simplemusicplayer.R;
import com.github.anrimian.simplemusicplayer.domain.models.playlist.PlayList;
import com.github.anrimian.simplemusicplayer.ui.utils.OnItemClickListener;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.github.anrimian.simplemusicplayer.ui.common.format.FormatUtils.formatMilliseconds;

public class PlayListViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.tv_play_list_name)
    TextView tvPlayListName;

    @BindView(R.id.tv_additional_info)
    TextView tvAdditionalInfo;

    private PlayList playList;

    PlayListViewHolder(LayoutInflater inflater,
                       ViewGroup parent,
                       OnItemClickListener<PlayList> onItemClickListener) {
        super(inflater.inflate(R.layout.item_play_list, parent, false));
        ButterKnife.bind(this, itemView);
        if (onItemClickListener != null) {
            itemView.setOnClickListener(v -> onItemClickListener.onItemClick(playList));
        }
    }

    void bind(PlayList playList) {
        this.playList = playList;
        tvPlayListName.setText(playList.getName());
        showAdditionalInfo();
    }

    private void showAdditionalInfo() {
        int compositionsCount = playList.getCompositionsCount();
        StringBuilder sb = new StringBuilder(getContext().getResources().getQuantityString(
                R.plurals.compositions_count,
                compositionsCount,
                compositionsCount));
        sb.append(" ● ");//TODO split problem
        sb.append(formatMilliseconds(playList.getTotalDuration()));
        tvAdditionalInfo.setText(sb.toString());
    }

    private Context getContext() {
        return itemView.getContext();
    }
}
