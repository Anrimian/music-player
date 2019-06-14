package com.github.anrimian.musicplayer.ui.main;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.github.anrimian.musicplayer.R;
import com.github.anrimian.musicplayer.di.Components;
import com.github.anrimian.musicplayer.ui.player_screen.PlayerFragment;
import com.github.anrimian.musicplayer.ui.start.StartFragment;
import com.github.anrimian.musicplayer.ui.utils.AndroidUtils;
import com.github.anrimian.musicplayer.ui.utils.fragments.BackButtonListener;
import com.tbruyelle.rxpermissions2.RxPermissions;

import static com.github.anrimian.musicplayer.Constants.Arguments.OPEN_PLAY_QUEUE_ARG;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Components.getAppComponent().themeController().applyCurrentTheme(this);
        setContentView(R.layout.activity_main);

        if (hasFilePermissions()) {
            goToMainScreen();
        } else {
            goToStartScreen();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (getOpenPlayQueueArg(intent)) {
            Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.main_activity_container);
            if (fragment instanceof PlayerFragment) {
                ((PlayerFragment) fragment).openPlayQueue();//non-smooth update, why...
            }
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
    protected void onStop() {
        super.onStop();
        AndroidUtils.hideKeyboard(getWindow().getDecorView());
    }

    private boolean hasFilePermissions() {
        RxPermissions rxPermissions = new RxPermissions(this);
        return rxPermissions.isGranted(Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    private void goToStartScreen() {
        startFragment(new StartFragment());
    }

    private void goToMainScreen() {
        boolean openPlayQueue = getOpenPlayQueueArg(getIntent());
        startFragment(PlayerFragment.newInstance(openPlayQueue));
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

    private boolean getOpenPlayQueueArg(Intent intent) {
        boolean openPlayQueue = intent.getBooleanExtra(OPEN_PLAY_QUEUE_ARG, false);
        getIntent().removeExtra(OPEN_PLAY_QUEUE_ARG);
        return openPlayQueue;
    }
}
