package com.github.anrimian.simplemusicplayer.ui.common.order;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.anrimian.simplemusicplayer.R;
import com.github.anrimian.simplemusicplayer.domain.models.composition.Order;
import com.github.anrimian.simplemusicplayer.ui.common.order.adapter.OrderAdapter;
import com.github.anrimian.simplemusicplayer.ui.utils.OnCompleteListener;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.github.anrimian.simplemusicplayer.Constants.Arguments.ORDER_ARG;

public class SelectOrderDialogFragment extends DialogFragment {

    @BindView(R.id.rv_order)
    RecyclerView rvOrder;

    private OnCompleteListener<Order> onCompleteListener;

    public static SelectOrderDialogFragment newInstance(Order order) {
        Bundle args = new Bundle();
        args.putSerializable(ORDER_ARG, order);
        SelectOrderDialogFragment fragment = new SelectOrderDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = View.inflate(getActivity(), R.layout.dialog_order, null);

        ButterKnife.bind(this, view);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        rvOrder.setLayoutManager(layoutManager);

        OrderAdapter orderAdapter = new OrderAdapter(getOrder());
        orderAdapter.setOnItemClickListener(this::onComplete);
        rvOrder.setAdapter(orderAdapter);

        return new AlertDialog.Builder(getActivity())
                .setTitle(R.string.order)
                .setView(view)
                .create();
    }

    public void setOnCompleteListener(OnCompleteListener<Order> onCompleteListener) {
        this.onCompleteListener = onCompleteListener;
    }

    private Order getOrder() {
        //noinspection ConstantConditions
        return (Order) getArguments().getSerializable(ORDER_ARG);
    }

    private void onComplete(Order order) {
        if (getOrder() != order && onCompleteListener != null) {
            onCompleteListener.onComplete(order);
        }
        dismiss();
    }
}
