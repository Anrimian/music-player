package com.github.anrimian.musicplayer.ui.utils.wrappers;

import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.github.anrimian.musicplayer.R;

import androidx.annotation.DrawableRes;

import static android.view.View.GONE;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;


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
    private View progressStateContainer;

    public ProgressViewWrapper(View view) {
        progressBar = view.findViewById(R.id.psv_progress_bar);
        tvMessage = view.findViewById(R.id.psv_tv_message);
        btnTryAgain = view.findViewById(R.id.psv_btn_action);
        ivEmpty = view.findViewById(R.id.psv_iv_empty);
        progressStateContainer = view.findViewById(R.id.progress_state_container);
    }

    public void setTryAgainButtonOnClickListener(View.OnClickListener listener) {
        btnTryAgain.setOnClickListener(listener);
    }

    public void hideAll() {
        progressStateContainer.setVisibility(INVISIBLE);
    }

    public void goneAll() {
        progressStateContainer.setVisibility(GONE);
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
        progressStateContainer.setVisibility(VISIBLE);
        progressBar.setVisibility(GONE);
        tvMessage.setVisibility(VISIBLE);
        progressStateContainer.setContentDescription(message);

        if (message != null) {
            tvMessage.setText(message);
        }
        if (imageRes != NO_DRAWABLE) {
            ivEmpty.setImageResource(imageRes);
            ivEmpty.setVisibility(VISIBLE);
        } else {
            ivEmpty.setVisibility(GONE);
        }
        if (showTryAgainButton) {
            btnTryAgain.setVisibility(VISIBLE);
        } else {
            btnTryAgain.setVisibility(GONE);
        }
    }

    public void showProgress() {
//        progressStateContainer.setContentDescription(...loading...);
        progressBar.setIndeterminate(true);
        progressBar.setVisibility(VISIBLE);
        tvMessage.setVisibility(GONE);
        btnTryAgain.setVisibility(GONE);
        ivEmpty.setVisibility(GONE);
    }
}
