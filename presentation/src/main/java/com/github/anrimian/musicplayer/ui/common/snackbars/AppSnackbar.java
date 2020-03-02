package com.github.anrimian.musicplayer.ui.common.snackbars;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.core.view.ViewCompat;

import com.github.anrimian.musicplayer.R;
import com.google.android.material.snackbar.BaseTransientBottomBar;

public class AppSnackbar extends BaseTransientBottomBar<AppSnackbar>  {

    public static AppSnackbar make(ViewGroup parent,
                                   String text,
                                   @StringRes int actionText,
                                   Runnable listener) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.snackbar_view, parent, false);

        ContentViewCallback callback = new ContentViewCallback(view);
        AppSnackbar appSnackbar = new AppSnackbar(parent, view, callback);
        appSnackbar.getView().setPadding(0, 0, 0, 0);

        TextView textView = appSnackbar.getView().findViewById(R.id.snackbar_text);
        textView.setText(text);

        Button actionView = appSnackbar.getView().findViewById(R.id.snackbar_action);
        actionView.setText(actionText);
        actionView.setVisibility(View.VISIBLE);
        actionView.setOnClickListener(v -> {
            listener.run();
            appSnackbar.dismiss();
        });

        appSnackbar.setDuration(2000);
        return appSnackbar;
    }

    protected AppSnackbar(@NonNull ViewGroup parent,
                          @NonNull View content,
                          @NonNull ContentViewCallback contentViewCallback) {
        super(parent, content, contentViewCallback);
    }

    public AppSnackbar setText(CharSequence text) {
        TextView textView = getView().findViewById(R.id.snackbar_text);
        textView.setText(text);
        return this;
    }

    public AppSnackbar setAction(CharSequence text, final Runnable  listener) {
        Button actionView = getView().findViewById(R.id.snackbar_action);
        actionView.setText(text);
        actionView.setVisibility(View.VISIBLE);
        actionView.setOnClickListener(view -> {
            listener.run();
            dismiss();
        });
        return this;
    }

    private static class ContentViewCallback implements com.google.android.material.snackbar.ContentViewCallback {

        private View content;

        public ContentViewCallback(View content) {
            this.content = content;
        }

        @Override
        public void animateContentIn(int delay, int duration) {
            ViewCompat.setScaleY(content, 0f);
            ViewCompat.animate(content)
                    .scaleY(1f).setDuration(duration)
                    .setStartDelay(delay);
        }

        @Override
        public void animateContentOut(int delay, int duration) {
            ViewCompat.setScaleY(content, 1f);
            ViewCompat.animate(content)
                    .scaleY(0f)
                    .setDuration(duration)
                    .setStartDelay(delay);
        }
    }

}
