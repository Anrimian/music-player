package com.github.anrimian.musicplayer.ui.library.common.order

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.anrimian.musicplayer.Constants
import com.github.anrimian.musicplayer.R
import com.github.anrimian.musicplayer.databinding.DialogOrderBinding
import com.github.anrimian.musicplayer.di.Components
import com.github.anrimian.musicplayer.domain.models.order.Order
import com.github.anrimian.musicplayer.domain.models.order.OrderType
import com.github.anrimian.musicplayer.ui.common.format.FormatUtils
import com.github.anrimian.musicplayer.ui.library.common.order.adapter.OrderAdapter
import com.github.anrimian.musicplayer.ui.utils.ViewUtils
import com.github.anrimian.musicplayer.ui.utils.getSerializableExtra
import moxy.MvpAppCompatDialogFragment
import moxy.ktx.moxyPresenter

class SelectOrderDialogFragment : MvpAppCompatDialogFragment(), SelectOrderView {

    companion object {

        fun newInstance(
            selectedOrder: Order,
            vararg orders: OrderType
        ): SelectOrderDialogFragment {
            return newInstance(selectedOrder, false, *orders)
        }

        fun newInstance(
            selectedOrder: Order,
            showFileNameSetting: Boolean = false,
            vararg orders: OrderType
        ) = SelectOrderDialogFragment().apply {
            arguments = Bundle().apply {
                putSerializable(Constants.Arguments.ORDER_ARG, selectedOrder)
                putSerializable(Constants.Arguments.ORDERS_ARG, orders)
                putBoolean(Constants.Arguments.FILE_NAME_SETTING_ARG, showFileNameSetting)
            }
        }

    }

    private val presenter by moxyPresenter {
        Components.getOrderComponent(getOrder()).selectOrderPresenter()
    }

    private lateinit var binding: DialogOrderBinding
    private lateinit var orderAdapter: OrderAdapter

    private var onCompleteListener: ((Order) -> Unit)? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogOrderBinding.inflate(LayoutInflater.from(requireContext()))
        val rvOrder = binding.rvOrder
        val view = binding.root

        val layoutManager = LinearLayoutManager(activity)
        rvOrder.layoutManager = layoutManager
        val orders = requireArguments().getSerializableExtra<Array<OrderType>>(Constants.Arguments.ORDERS_ARG)
        orderAdapter = OrderAdapter(orders, presenter::onOrderTypeSelected)
        rvOrder.adapter = orderAdapter

        val dialog = AlertDialog.Builder(activity)
            .setTitle(R.string.order)
            .setView(view)
            .setPositiveButton(android.R.string.ok, null)
            .setNegativeButton(R.string.cancel) { _, _ -> }
            .create()
        dialog.show()

        val btnOk = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
        btnOk.setOnClickListener { presenter.onCompleteButtonClicked() }

        binding.cbDesc.setOnCheckedChangeListener { _, isChecked ->
            presenter.onReverseTypeSelected(isChecked)
        }
        if (requireArguments().getBoolean(Constants.Arguments.FILE_NAME_SETTING_ARG)) {
            ViewUtils.onCheckChanged(binding.cbUseFileName) { checked ->
                presenter.onFileNameChecked(checked)
            }
        } else {
            binding.cbUseFileName.visibility = View.GONE
        }
        return dialog
    }

    override fun showSelectedOrder(orderType: OrderType) {
        orderAdapter.setCheckedItem(orderType)
        binding.cbDesc.setText(FormatUtils.getReversedOrderText(orderType))
    }

    override fun showReverse(selected: Boolean) {
        binding.cbDesc.isChecked = selected
    }

    override fun showFileNameEnabled(checked: Boolean) {
        ViewUtils.setChecked(binding.cbUseFileName, checked)
    }

    override fun close(order: Order) {
        if (this.getOrder() != order && onCompleteListener != null) {
            onCompleteListener!!(order)
        }
        dismissAllowingStateLoss()
    }

    fun setOnCompleteListener(onCompleteListener: (Order) -> Unit) {
        this.onCompleteListener = onCompleteListener
    }

    private fun getOrder() = requireArguments().getSerializableExtra<Order>(Constants.Arguments.ORDER_ARG)

}