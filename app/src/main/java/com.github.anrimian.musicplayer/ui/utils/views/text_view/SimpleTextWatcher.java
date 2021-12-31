package com.github.anrimian.musicplayer.ui.utils.views.text_view;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;


public class SimpleTextWatcher implements TextWatcher {

    private TextChangeListener textChangeListener;
    private final boolean trimSpaces;

    public static void onTextChanged(EditText editText, TextChangeListener textChangeListener) {
        editText.addTextChangedListener(new SimpleTextWatcher(textChangeListener));
    }

    public static void onTextChanged(EditText editText,
                                     TextChangeListener textChangeListener,
                                     boolean trimSpaces) {
        editText.addTextChangedListener(new SimpleTextWatcher(textChangeListener, trimSpaces));
    }

    public SimpleTextWatcher(TextChangeListener textChangeListener) {
        this(textChangeListener, true);
    }

    public SimpleTextWatcher(TextChangeListener textChangeListener, boolean trimSpaces) {
        this.textChangeListener = textChangeListener;
        this.trimSpaces = trimSpaces;
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
            String text = s.toString();
            if (trimSpaces) {
                text = text.trim();
            }
            textChangeListener.onTextChanged(text);
        }
    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    public interface TextChangeListener {
        void onTextChanged(String text);
    }
}

