package com.github.anrimian.simplemusicplayer.ui.main;

import android.Manifest;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;

import com.github.anrimian.simplemusicplayer.R;
import com.github.anrimian.simplemusicplayer.ui.drawer.DrawerFragment;
import com.github.anrimian.simplemusicplayer.ui.start.StartFragment;
import com.tbruyelle.rxpermissions2.RxPermissions;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            if (hasFilePermissions()) {//TODO maybe some unexpected behavior when user revokes permission
                goToMainScreen();
            } else {
                goToStartScreen();
            }
        }
    }

    private boolean hasFilePermissions() {
        RxPermissions rxPermissions = new RxPermissions(this);
        return rxPermissions.isGranted(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                && !rxPermissions.isRevoked(Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    private void goToStartScreen() {
        startFragment(new StartFragment());
    }

    private void goToMainScreen() {
        startFragment(new DrawerFragment());
    }

    private void startFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.main_activity_container, fragment)
                .commit();
    }
}
