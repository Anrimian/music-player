package com.github.anrimian.musicplayer.ui.utils.dialogs.menu;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.github.anrimian.musicplayer.R;
import com.github.anrimian.musicplayer.ui.utils.OnCompleteListener;

import androidx.annotation.MenuRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.view.SupportMenuInflater;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;

public class MenuDialogFragment extends DialogFragment {

    private static final String MENU_ARG = "menu_arg";
    private static final String TITLE_ARG = "title_arg";

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    @Nullable
    private OnCompleteListener<MenuItem> onCompleteListener;

    public static MenuDialogFragment newInstance(@MenuRes int menuRes, String title) {
        Bundle args = new Bundle();
        args.putInt(MENU_ARG, menuRes);
        args.putString(TITLE_ARG, title);
        MenuDialogFragment fragment = new MenuDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = View.inflate(getActivity(), R.layout.dialog_menu, null);
        ButterKnife.bind(this, view);

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        MenuAdapter menuAdapter = new MenuAdapter(getMenu());
        menuAdapter.setOnItemClickListener(this::onMenuItemClicked);
        recyclerView.setAdapter(menuAdapter);

        return new AlertDialog.Builder(getActivity())
                .setTitle(getTitle())
                .setView(view)
                .create();
    }

    public void setOnCompleteListener(OnCompleteListener<MenuItem> onCompleteListener) {
        this.onCompleteListener = onCompleteListener;
    }

    private void onMenuItemClicked(MenuItem menuItem) {
        if (onCompleteListener != null) {
            onCompleteListener.onComplete(menuItem);
        }
        dismiss();
    }

    @SuppressLint("RestrictedApi")
    private Menu getMenu() {
        @MenuRes int menuRes = getArguments().getInt(MENU_ARG);
        PopupMenu p  = new PopupMenu(requireContext(), null);
        Menu menu = p.getMenu();
        new SupportMenuInflater(requireContext()).inflate(menuRes, menu);
        return menu;
    }

    private String getTitle() {
        return getArguments().getString(TITLE_ARG);
    }
}
