package com.github.anrimian.musicplayer.ui.library.albums.list.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.github.anrimian.musicplayer.R;
import com.github.anrimian.musicplayer.di.Components;
import com.github.anrimian.musicplayer.domain.models.albums.Album;
import com.github.anrimian.musicplayer.domain.utils.java.Callback;
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.BaseViewHolder;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.text.TextUtils.isEmpty;
import static com.github.anrimian.musicplayer.domain.Payloads.ARTIST;
import static com.github.anrimian.musicplayer.domain.Payloads.COMPOSITIONS_COUNT;
import static com.github.anrimian.musicplayer.domain.Payloads.NAME;
import static com.github.anrimian.musicplayer.ui.common.format.FormatUtils.formatCompositionsCount;
import static com.github.anrimian.musicplayer.ui.utils.ViewUtils.onLongClick;

public class AlbumViewHolder extends BaseViewHolder {

    @BindView(R.id.tv_album_name)
    TextView tvAlbumName;

    @BindView(R.id.tv_compositions_count)
    TextView tvCompositionsCount;

    @BindView(R.id.iv_music_icon)
    ImageView ivMusicIcon;

    @BindView(R.id.clickable_item)
    View clickableItem;

    private Album album;

    AlbumViewHolder(@NonNull ViewGroup parent,
                    Callback<Album> itemClickListener,
                    Callback<Album> longClickListener) {
        super(parent, R.layout.item_album);
        ButterKnife.bind(this, itemView);
        clickableItem.setOnClickListener(v -> itemClickListener.call(album));
        onLongClick(clickableItem, () -> longClickListener.call(album));
    }

    public void bind(Album album) {
        this.album = album;
        showAlbumName();
        showAdditionalInfo();
        showCover();
    }

    public void update(Album album, List<Object> payloads) {
        this.album = album;
        for (Object payload: payloads) {
            if (payload instanceof List) {
                //noinspection SingleStatementInBlock,unchecked
                update(album, (List) payload);
            }
            if (payload == NAME) {
                showAlbumName();
                continue;
            }
            if (payload == ARTIST) {
                showAdditionalInfo();
                continue;
            }
            if (payload == COMPOSITIONS_COUNT) {
                showAdditionalInfo();
            }
        }
    }

    private void showAlbumName() {
        tvAlbumName.setText(album.getName());
    }

    private void showAdditionalInfo() {
        StringBuilder sb = new StringBuilder();
        String artist = album.getArtist();
        if (!isEmpty(artist)) {
            sb.append(artist);
            sb.append(" ● ");//TODO split problem • ●
        }
        sb.append(formatCompositionsCount(
                getContext(),
                album.getCompositionsCount())
        );
        tvCompositionsCount.setText(sb.toString());
    }

    private void showCover() {
        Components.getAppComponent().imageLoader().displayImage(ivMusicIcon,
                album,
                R.drawable.ic_album_placeholder);
    }
}
