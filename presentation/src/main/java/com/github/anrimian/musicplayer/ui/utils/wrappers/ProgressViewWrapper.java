package com.github.anrimian.musicplayer.ui.utils.wrappers;

import android.support.annotation.DrawableRes;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.github.anrimian.musicplayer.R;


/**
 * Created on 21.02.2016.
 */
@SuppressWarnings("WeakerAccess")
public class ProgressViewWrapper {

    private static final int NO_DRAWABLE = -1;

    private ProgressBar progressBar;
    private TextView tvMessage;
    private View btnTryAgain;
    private ImageView ivEmpty;

    public ProgressViewWrapper(View view) {
        progressBar = view.findViewById(R.id.psv_progress_bar);
        tvMessage = view.findViewById(R.id.psv_tv_message);
        btnTryAgain = view.findViewById(R.id.psv_btn_action);
        ivEmpty = view.findViewById(R.id.psv_iv_empty);
    }

    public ProgressViewWrapper(ProgressBar progressBar, TextView tvMessage, View btnTryAgain, ImageView ivEmpty) {
        this.progressBar = progressBar;
        this.tvMessage = tvMessage;
        this.btnTryAgain = btnTryAgain;
        this.ivEmpty = ivEmpty;
    }

    public void setTryAgainButtonOnClickListener(View.OnClickListener listener) {
        btnTryAgain.setOnClickListener(listener);
    }

    public void hideAll() {
        progressBar.setVisibility(View.INVISIBLE);
        tvMessage.setVisibility(View.INVISIBLE);
        btnTryAgain.setVisibility(View.INVISIBLE);
        ivEmpty.setVisibility(View.INVISIBLE);
    }

    public void goneAll() {
        progressBar.setVisibility(View.GONE);
        tvMessage.setVisibility(View.GONE);
        btnTryAgain.setVisibility(View.GONE);
        ivEmpty.setVisibility(View.GONE);
    }

    public void showMessage(int messageId, boolean showTryAgainButton) {
        showMessage(messageId, NO_DRAWABLE, showTryAgainButton);
    }

    public void showMessage(int messageId, @DrawableRes int emptyImageRes, boolean showTryAgainButton) {
        String message = progressBar.getContext().getString(messageId);
        showMessage(message, emptyImageRes, showTryAgainButton);
    }

    public void showMessage(String message, boolean showTryAgainButton) {
        showMessage(message, NO_DRAWABLE, showTryAgainButton);
    }

    public void showMessage(String message, @DrawableRes int imageRes, boolean showTryAgainButton) {
        progressBar.setVisibility(View.GONE);
        tvMessage.setVisibility(View.VISIBLE);
        if (message != null) {
            tvMessage.setText(message);
        }
        if (imageRes != NO_DRAWABLE) {
            ivEmpty.setImageResource(imageRes);
            ivEmpty.setVisibility(View.VISIBLE);
        } else {
            ivEmpty.setVisibility(View.GONE);
        }
        if (showTryAgainButton) {
            btnTryAgain.setVisibility(View.VISIBLE);
        } else {
            btnTryAgain.setVisibility(View.GONE);
        }
    }

    public void showProgress() {
        progressBar.setIndeterminate(true);
        progressBar.setVisibility(View.VISIBLE);
        tvMessage.setVisibility(View.GONE);
        btnTryAgain.setVisibility(View.GONE);
        ivEmpty.setVisibility(View.GONE);
    }
}
