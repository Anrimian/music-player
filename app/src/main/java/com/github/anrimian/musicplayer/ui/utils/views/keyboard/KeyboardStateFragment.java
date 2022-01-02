package com.github.anrimian.musicplayer.ui.utils.views.keyboard;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class KeyboardStateFragment extends Fragment {

    private boolean isKeyboardWasShown;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    boolean isKeyboardWasShown() {
        return isKeyboardWasShown;
    }

    void setKeyboardWasShown(boolean keyboardWasShown) {
        isKeyboardWasShown = keyboardWasShown;
    }
}
