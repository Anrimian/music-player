package com.github.anrimian.musicplayer.ui.common.order;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.View;

import com.github.anrimian.musicplayer.R;
import com.github.anrimian.musicplayer.domain.models.composition.Order;
import com.github.anrimian.musicplayer.ui.common.order.adapter.OrderAdapter;
import com.github.anrimian.musicplayer.ui.utils.OnCompleteListener;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.github.anrimian.musicplayer.Constants.Arguments.ORDER_ARG;

public class SelectOrderDialogFragment extends DialogFragment {

    @BindView(R.id.rv_order)
    RecyclerView rvOrder;

    @Nullable
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

    public void setOnCompleteListener(@Nullable OnCompleteListener<Order> onCompleteListener) {
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
