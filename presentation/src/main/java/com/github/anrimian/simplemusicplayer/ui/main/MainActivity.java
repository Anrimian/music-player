package com.github.anrimian.simplemusicplayer.ui.main;

import android.Manifest;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;

import com.github.anrimian.simplemusicplayer.R;
import com.github.anrimian.simplemusicplayer.infrastructure.service.MusicServiceManager;
import com.github.anrimian.simplemusicplayer.ui.player_screens.player_screen.PlayerFragment;
import com.github.anrimian.simplemusicplayer.ui.start.StartFragment;
import com.github.anrimian.simplemusicplayer.ui.utils.fragments.BackButtonListener;
import com.tbruyelle.rxpermissions2.RxPermissions;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (hasFilePermissions()) {
            MusicServiceManager.initialize();
            goToMainScreen();
        } else {
            goToStartScreen();
        }
    }

    @Override
    public void onBackPressed() {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.main_activity_container);
        if (fragment instanceof BackButtonListener && ((BackButtonListener) fragment).onBackPressed()) {
            return;
        }
        super.onBackPressed();
    }

    @Override
    public void setTitle(int titleId) {
        super.setTitle(titleId);
    }

    private boolean hasFilePermissions() {
        RxPermissions rxPermissions = new RxPermissions(this);
        return rxPermissions.isGranted(Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    private void goToStartScreen() {
        startFragment(new StartFragment());
    }

    private void goToMainScreen() {
        startFragment(new PlayerFragment());
    }

    private void startFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment existFragment = fragmentManager.findFragmentById(R.id.main_activity_container);
        if (existFragment == null || existFragment.getClass() != fragment.getClass()) {
            fragmentManager.beginTransaction()
                    .replace(R.id.main_activity_container, fragment)
                    .commit();
        }
    }
}
