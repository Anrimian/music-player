package com.github.anrimian.musicplayer.ui.utils.fragments.navigation;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

class FragmentMetaData {

    private static final String CLASS_NAME = "class_name";
    private static final String ARGUMENTS = "arguments";

    private final String fragmentClassName;

    @Nullable
    private final Bundle arguments;

    FragmentMetaData(Fragment fragment) {
        fragmentClassName = fragment.getClass().getCanonicalName();
        arguments = fragment.getArguments();
    }

    FragmentMetaData(Bundle bundle) {
        fragmentClassName = bundle.getString(CLASS_NAME);
        arguments = bundle.getBundle(ARGUMENTS);
    }

    private FragmentMetaData(String fragmentClassName, @Nullable Bundle arguments) {
        this.fragmentClassName = fragmentClassName;
        this.arguments = arguments;
    }

    String getFragmentClassName() {
        return fragmentClassName;
    }

    @Nullable
    Bundle getArguments() {
        return arguments;
    }

    Bundle toBundle() {
        Bundle bundle = new Bundle();
        bundle.putString(CLASS_NAME, fragmentClassName);
        bundle.putBundle(ARGUMENTS, arguments);
        return bundle;
    }

    @Override
    public String toString() {
        return "FragmentMetaData{" +
                "fragmentClassName='" + fragmentClassName + '\'' +
                ", arguments=" + arguments +
                '}';
    }
}
