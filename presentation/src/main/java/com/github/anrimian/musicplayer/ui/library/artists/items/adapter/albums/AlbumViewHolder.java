package com.github.anrimian.musicplayer.ui.library.artists.items.adapter.albums;

import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.github.anrimian.musicplayer.R;
import com.github.anrimian.musicplayer.domain.models.albums.Album;
import com.github.anrimian.musicplayer.domain.utils.java.Callback;
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.BaseViewHolder;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.text.TextUtils.isEmpty;
import static com.github.anrimian.musicplayer.domain.Payloads.COMPOSITIONS_COUNT;
import static com.github.anrimian.musicplayer.domain.Payloads.NAME;
import static com.github.anrimian.musicplayer.ui.common.format.FormatUtils.formatCompositionsCount;

public class AlbumViewHolder extends BaseViewHolder {

    @BindView(R.id.tv_album_name)
    TextView tvAlbumName;

    @BindView(R.id.tv_compositions_count)
    TextView tvCompositionsCount;

    @BindView(R.id.iv_music_icon)
    ImageView ivMusicIcon;

    private Album album;

    AlbumViewHolder(@NonNull ViewGroup parent,
                    Callback<Album> itemClickListener) {
        super(parent, R.layout.item_album_horizontal);
        ButterKnife.bind(this, itemView);
        itemView.setOnClickListener(v -> itemClickListener.call(album));
    }

    public void bind(Album album) {
        this.album = album;
        showAlbumName();
        showCompositionsCount();
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
            if (payload == COMPOSITIONS_COUNT) {
                showCompositionsCount();
            }
        }
    }

    private void showAlbumName() {
        tvAlbumName.setText(album.getName());
    }

    private void showCompositionsCount() {
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
//        CoverImageLoader.getInstance().displayImage();
//        Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");
//        Uri uri = ContentUris.withAppendedId(sArtworkUri, album.getStorageId());
//        ContentResolver res = getContext().getContentResolver();
//        InputStream in = null;
//        try {
//            in = res.openInputStream(uri);
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }
//        Bitmap artwork = BitmapFactory.decodeStream(in);
//        ivMusicIcon.setImageBitmap(artwork);
    }
}
