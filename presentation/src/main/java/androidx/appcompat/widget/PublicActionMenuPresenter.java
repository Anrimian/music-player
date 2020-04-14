package androidx.appcompat.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;

import androidx.appcompat.view.menu.MenuItemImpl;

import java.util.ArrayList;

public class PublicActionMenuPresenter extends ActionMenuPresenter {

    private OverflowMenuRunnable overflowMenuAction;

    public PublicActionMenuPresenter(Context context) {
        super(context);
    }

    public PublicActionMenuPresenter(Context context, OverflowMenuRunnable overflowMenuAction) {
        super(context);
        this.overflowMenuAction = overflowMenuAction;
    }

    @SuppressLint("RestrictedApi")
    @Override
    public boolean showOverflowMenu() {
        if (overflowMenuAction == null) {
            return super.showOverflowMenu();
        } else {
            overflowMenuAction.call(mOverflowButton, mMenu.getNonActionItems());
            return true;
        }
    }

    public interface OverflowMenuRunnable {
        void call(View anchorView, ArrayList<MenuItemImpl> items);
    }
}
