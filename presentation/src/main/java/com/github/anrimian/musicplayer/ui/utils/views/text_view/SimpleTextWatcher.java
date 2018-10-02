package com.github.anrimian.musicplayer.ui.utils.views.text_view;

import android.text.Editable;
import android.text.TextWatcher;

public class SimpleTextWatcher implements TextWatcher {

    private TextChangeListener textChangeListener;

    public SimpleTextWatcher(TextChangeListener textChangeListener) {
        this.textChangeListener = textChangeListener;
    }

    public SimpleTextWatcher() {
    }

    public void setTextChangeListener(TextChangeListener textChangeListener) {
        this.textChangeListener = textChangeListener;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (textChangeListener != null) {
            textChangeListener.onTextChanged(s.toString());
        }
    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    public interface TextChangeListener {
        void onTextChanged(String text);
    }
}
