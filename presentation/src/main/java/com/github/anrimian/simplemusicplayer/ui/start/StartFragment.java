package com.github.anrimian.simplemusicplayer.ui.start;

import android.Manifest;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.arellomobile.mvp.MvpAppCompatFragment;
import com.arellomobile.mvp.presenter.InjectPresenter;
import com.github.anrimian.simplemusicplayer.R;
import com.github.anrimian.simplemusicplayer.ui.drawer.DrawerFragment;
import com.github.anrimian.simplemusicplayer.utils.wrappers.ProgressViewWrapper;
import com.tbruyelle.rxpermissions2.RxPermissions;

/**
 * Created on 19.10.2017.
 */

public class StartFragment extends MvpAppCompatFragment implements StartView {

    @InjectPresenter
    StartPresenter presenter;

    private ProgressViewWrapper progressViewWrapper;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_start, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        progressViewWrapper = new ProgressViewWrapper(view);
        progressViewWrapper.setTryAgainButtonOnClickListener(v -> presenter.onTryAgainButtonClicked());
    }

    @Override
    public void requestFilesPermissions() {
        new RxPermissions(getActivity())
                .request(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .subscribe(presenter::onFilesPermissionResult);
    }

    @Override
    public void showDeniedPermissionMessage() {
        progressViewWrapper.showMessage(R.string.can_not_work_without_file_permission, true);
    }

    @Override
    public void showStub() {
        progressViewWrapper.hideAll();
    }

    @Override
    public void goToMainScreen() {
        getFragmentManager()
                .beginTransaction()
                .setCustomAnimations(R.anim.anim_alpha_appear, R.anim.anim_alpha_disappear)
                .replace(R.id.main_activity_container, new DrawerFragment())
                .commit();
    }
}
