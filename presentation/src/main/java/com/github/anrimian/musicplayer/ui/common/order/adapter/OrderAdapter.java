package com.github.anrimian.musicplayer.ui.common.order.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.github.anrimian.musicplayer.domain.models.composition.order.OrderType;
import com.github.anrimian.musicplayer.ui.utils.OnItemClickListener;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import static java.util.Arrays.asList;

public class OrderAdapter extends RecyclerView.Adapter<OrderViewHolder> {

    private final Set<OrderViewHolder> viewHolders = new HashSet<>();

    private List<OrderType> orderList = asList(OrderType.values());

    private OnItemClickListener<OrderType> onItemClickListener;

    private OrderType selectedOrder;

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
      LayoutInflater inflater = LayoutInflater.from(parent.getContext());
      return new OrderViewHolder(inflater, parent, onItemClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        viewHolders.add(holder);

        OrderType order = orderList.get(position);
        holder.bindView(order);
        holder.setSelected(order == selectedOrder);
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    @Override
    public void onViewRecycled(@NonNull OrderViewHolder holder) {
        super.onViewRecycled(holder);
        viewHolders.remove(holder);
    }

    public void setOnItemClickListener(OnItemClickListener<OrderType> onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public void setCheckedItem(OrderType selectedOrder) {
        this.selectedOrder = selectedOrder;
        for (OrderViewHolder holder: viewHolders) {
            holder.setSelected(holder.getOrder() == selectedOrder);
        }
    }
}
