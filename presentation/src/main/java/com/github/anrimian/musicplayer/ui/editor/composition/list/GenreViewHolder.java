package com.github.anrimian.musicplayer.ui.editor.composition.list;

import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.github.anrimian.musicplayer.R;
import com.github.anrimian.musicplayer.domain.models.genres.ShortGenre;
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.BaseViewHolder;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.github.anrimian.musicplayer.domain.Payloads.NAME;

class GenreViewHolder extends BaseViewHolder {

    @BindView(R.id.tv_genre)
    TextView tvGenre;

    private ShortGenre genre;

    GenreViewHolder(@NonNull ViewGroup parent) {
        super(parent, R.layout.item_genre_chip);
        ButterKnife.bind(this, itemView);
    }

    void bind(ShortGenre genre) {
        this.genre = genre;
        showName();
    }

    void update(ShortGenre genre, List<Object> payloads) {
        this.genre = genre;
        for (Object payload: payloads) {
            if (payload instanceof List) {
                //noinspection SingleStatementInBlock,unchecked
                update(genre, (List) payload);
            }
            if (payload == NAME) {
                showName();
            }
        }
    }

    private void showName() {
        tvGenre.setText(genre.getName());
    }
}
