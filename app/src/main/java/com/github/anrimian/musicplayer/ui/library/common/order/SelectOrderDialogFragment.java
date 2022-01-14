package com.github.anrimian.musicplayer.ui.library.common.order;

import static com.github.anrimian.musicplayer.Constants.Arguments.FILE_NAME_SETTING_ARG;
import static com.github.anrimian.musicplayer.Constants.Arguments.ORDERS_ARG;
import static com.github.anrimian.musicplayer.Constants.Arguments.ORDER_ARG;
import static com.github.anrimian.musicplayer.ui.common.format.FormatUtils.getReversedOrderText;
import static com.github.anrimian.musicplayer.ui.utils.ViewUtils.onCheckChanged;
import static com.github.anrimian.musicplayer.ui.utils.ViewUtils.setChecked;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.anrimian.musicplayer.R;
import com.github.anrimian.musicplayer.databinding.DialogOrderBinding;
import com.github.anrimian.musicplayer.di.Components;
import com.github.anrimian.musicplayer.domain.models.order.Order;
import com.github.anrimian.musicplayer.domain.models.order.OrderType;
import com.github.anrimian.musicplayer.ui.library.common.order.adapter.OrderAdapter;
import com.github.anrimian.musicplayer.ui.utils.OnCompleteListener;

import moxy.MvpAppCompatDialogFragment;
import moxy.presenter.InjectPresenter;
import moxy.presenter.ProvidePresenter;

public class SelectOrderDialogFragment extends MvpAppCompatDialogFragment implements SelectOrderView {

    @InjectPresenter
    SelectOrderPresenter presenter;

    private DialogOrderBinding viewBinding;

    private OrderAdapter orderAdapter;

    @Nullable
    private OnCompleteListener<Order> onCompleteListener;

    @ProvidePresenter
    SelectOrderPresenter providePresenter() {
        return Components.getLibraryComponent().selectOrderPresenter();
    }

    public static SelectOrderDialogFragment newInstance(Order selectedOrder,
                                                        OrderType... orders) {
        return SelectOrderDialogFragment.newInstance(selectedOrder, false, orders);
    }

    public static SelectOrderDialogFragment newInstance(Order selectedOrder,
                                                        boolean showFileNameSetting,
                                                        OrderType... orders) {
        Bundle args = new Bundle();
        args.putSerializable(ORDER_ARG, selectedOrder);
        args.putSerializable(ORDERS_ARG, orders);
        args.putBoolean(FILE_NAME_SETTING_ARG, showFileNameSetting);
        SelectOrderDialogFragment fragment = new SelectOrderDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            presenter.setOrder(getOrder());
        }

        viewBinding = DialogOrderBinding.inflate(LayoutInflater.from(requireContext()));
        RecyclerView rvOrder = viewBinding.rvOrder;
        View view = viewBinding.getRoot();

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        rvOrder.setLayoutManager(layoutManager);

        //noinspection ConstantConditions
        OrderType[] orders = (OrderType[]) getArguments().getSerializable(ORDERS_ARG);

        orderAdapter = new OrderAdapter(orders, presenter::onOrderTypeSelected);
        rvOrder.setAdapter(orderAdapter);

        AlertDialog dialog = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.order)
                .setView(view)
                .setPositiveButton(android.R.string.ok, null)
                .setNegativeButton(R.string.cancel, (dialog1, which) -> {})
                .create();
        dialog.show();

        Button btnOk = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        btnOk.setOnClickListener(v -> presenter.onCompleteButtonClicked());

        viewBinding.cbDesc.setOnCheckedChangeListener((buttonView, isChecked) ->
                presenter.onReverseTypeSelected(isChecked)
        );
        if (requireArguments().getBoolean(FILE_NAME_SETTING_ARG)) {
            onCheckChanged(viewBinding.cbUseFileName, presenter::onFileNameChecked);
        } else {
            viewBinding.cbUseFileName.setVisibility(View.GONE);
        }
        return dialog;

    }

    @Override
    public void showSelectedOrder(OrderType orderType) {
        orderAdapter.setCheckedItem(orderType);
        viewBinding.cbDesc.setText(getReversedOrderText(orderType));
    }

    @Override
    public void showReverse(boolean selected) {
        viewBinding.cbDesc.setChecked(selected);
    }

    @Override
    public void showFileNameEnabled(boolean checked) {
        setChecked(viewBinding.cbUseFileName, checked);
    }

    @Override
    public void close(Order order) {
        if (!getOrder().equals(order) && onCompleteListener != null) {
            onCompleteListener.onComplete(order);
        }
        dismissAllowingStateLoss();
    }

    public void setOnCompleteListener(@Nullable OnCompleteListener<Order> onCompleteListener) {
        this.onCompleteListener = onCompleteListener;
    }

    private Order getOrder() {
        //noinspection ConstantConditions
        return (Order) getArguments().getSerializable(ORDER_ARG);
    }
}
