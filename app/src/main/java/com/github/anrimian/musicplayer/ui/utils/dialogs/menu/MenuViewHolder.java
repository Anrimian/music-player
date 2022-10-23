package com.github.anrimian.musicplayer.ui.utils.dialogs.menu;

import static com.github.anrimian.musicplayer.ui.utils.AndroidUtils.getColorFromAttr;

import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.LayoutRes;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.github.anrimian.musicplayer.R;
import com.github.anrimian.musicplayer.ui.common.compat.CompatUtils;
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
        tvTitle = itemView.findViewById(R.id.tvTitle);
        ivIcon = itemView.findViewById(R.id.iv_icon);

        itemView.setOnClickListener(v -> onItemClickListener.onItemClick(menuItem));
    }

    void bind(MenuItem menuItem) {
        this.menuItem = menuItem;

        itemView.setEnabled(menuItem.isEnabled());

        tvTitle.setText(menuItem.getTitle());
        tvTitle.setEnabled(menuItem.isEnabled());
        if (menuItem.isChecked()) {
            tvTitle.setTextColor(getColorFromAttr(itemView.getContext(), R.attr.colorAccent));
        } else {
            tvTitle.setTextColor(ContextCompat.getColorStateList(itemView.getContext(), R.color.color_text_primary));
            CompatUtils.setColorTextPrimaryColor(tvTitle);
        }

        Drawable icon = menuItem.getIcon();
        ivIcon.setVisibility(icon == null? View.GONE: View.VISIBLE);
        ivIcon.setColorFilter(tvTitle.getCurrentTextColor());
        ivIcon.setImageDrawable(icon);
        ivIcon.setContentDescription(menuItem.getTitle());
    }

}
