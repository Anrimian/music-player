package com.github.anrimian.musicplayer.ui.common.menu;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupWindow;

import androidx.annotation.MenuRes;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.anrimian.musicplayer.R;
import com.github.anrimian.musicplayer.domain.utils.java.Callback;
import com.github.anrimian.musicplayer.ui.utils.AndroidUtils;
import com.github.anrimian.musicplayer.ui.utils.dialogs.menu.MenuAdapter;

import java.util.concurrent.atomic.AtomicReference;

public class PopupMenuWindow {

    @Deprecated
    public static void showPopup(View anchorView,
                                 @MenuRes int menuResId,
                                 PopupMenu.OnMenuItemClickListener listener) {
        showPopup(anchorView, menuResId, (Callback<MenuItem>) listener::onMenuItemClick);
    }

    public static void showPopup(View anchorView,
                                 @MenuRes int menuResId,
                                 Callback<MenuItem> listener) {
        Context context = anchorView.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        @SuppressLint("InflateParams")
        View popupView = inflater.inflate(R.layout.menu_popup, null);

        RecyclerView recyclerView = popupView.findViewById(R.id.recycler_view);

        recyclerView.setLayoutManager(new LinearLayoutManager(context));

        AtomicReference<PopupWindow> popupWindow = new AtomicReference<>();

        Menu menu = AndroidUtils.createMenu(context, menuResId);
        MenuAdapter menuAdapter = new MenuAdapter(menu, R.layout.item_dialog_menu);
        menuAdapter.setOnItemClickListener(item -> {
            listener.call(item);
            popupWindow.get().dismiss();
        });
        recyclerView.setAdapter(menuAdapter);

        popupWindow.set(AppPopupMenu.showPopupWindow(anchorView, popupView));
    }
}
