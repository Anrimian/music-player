package com.github.anrimian.musicplayer.ui.utils.dialogs.menu;

import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.LayoutRes;
import androidx.recyclerview.widget.RecyclerView;

import com.github.anrimian.musicplayer.R;
import com.github.anrimian.musicplayer.ui.utils.OnItemClickListener;

class MenuViewHolder extends RecyclerView.ViewHolder {

    private final TextView tvTitle;
    private final ImageView ivIcon;

    private MenuItem menuItem;

    MenuViewHolder(LayoutInflater inflater,
                   ViewGroup parent,
                   @LayoutRes int menuViewRes,
                   OnItemClickListener<MenuItem> onItemClickListener) {
        super(inflater.inflate(menuViewRes, parent, false));
        tvTitle = itemView.findViewById(R.id.tv_title);
        ivIcon = itemView.findViewById(R.id.iv_icon);

        itemView.setOnClickListener(v -> onItemClickListener.onItemClick(menuItem));
    }

    void bind(MenuItem menuItem) {
        this.menuItem = menuItem;
        tvTitle.setText(menuItem.getTitle());

        Drawable icon = menuItem.getIcon();
        ivIcon.setVisibility(icon == null? View.GONE: View.VISIBLE);
        ivIcon.setColorFilter(tvTitle.getCurrentTextColor());
        ivIcon.setImageDrawable(icon);
        ivIcon.setContentDescription(menuItem.getTitle());
    }

}
