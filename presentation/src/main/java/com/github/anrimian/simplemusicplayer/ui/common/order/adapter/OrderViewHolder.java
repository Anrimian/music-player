package com.github.anrimian.simplemusicplayer.ui.common.order.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.RadioButton;

import com.github.anrimian.simplemusicplayer.R;
import com.github.anrimian.simplemusicplayer.domain.models.composition.Order;
import com.github.anrimian.simplemusicplayer.ui.utils.OnItemClickListener;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.github.anrimian.simplemusicplayer.ui.common.format.FormatUtils.getOrderTitle;

public class OrderViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.rb_order)
    RadioButton rbOrder;

    private Order order;

    OrderViewHolder(LayoutInflater inflater,
                    ViewGroup parent,
                    OnItemClickListener<Order> onItemClickListener) {
        super(inflater.inflate(R.layout.item_order, parent, false));
        ButterKnife.bind(this, itemView);

        if (onItemClickListener != null) {
            itemView.setOnClickListener(v -> onItemClickListener.onItemClick(order));
        }
    }

    void bindView(Order order, boolean selected) {
        this.order = order;
        rbOrder.setText(getOrderTitle(order));
        rbOrder.setChecked(selected);
    }
}
