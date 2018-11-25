package com.github.anrimian.musicplayer.ui.utils.dialogs.menu;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.anrimian.musicplayer.R;
import com.github.anrimian.musicplayer.ui.utils.OnItemClickListener;

import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;

class MenuViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.tv_title)
    TextView tvTitle;

    private MenuItem menuItem;

    MenuViewHolder(LayoutInflater inflater,
                   ViewGroup parent,
                   OnItemClickListener<MenuItem> onItemClickListener) {
        super(inflater.inflate(R.layout.item_menu, parent, false));
        ButterKnife.bind(this, itemView);

        itemView.setOnClickListener(v -> onItemClickListener.onItemClick(menuItem));
    }

    void bind(MenuItem menuItem) {
        this.menuItem = menuItem;
        tvTitle.setText(menuItem.getTitle());
    }

}
