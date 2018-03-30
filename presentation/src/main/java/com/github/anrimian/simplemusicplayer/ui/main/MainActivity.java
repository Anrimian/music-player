package com.github.anrimian.simplemusicplayer.ui.main;

import android.Manifest;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileObserver;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;

import com.github.anrimian.simplemusicplayer.R;
import com.github.anrimian.simplemusicplayer.data.storage.TestFileObserver;
import com.github.anrimian.simplemusicplayer.ui.player_screens.player_screen.PlayerFragment;
import com.github.anrimian.simplemusicplayer.ui.start.StartFragment;
import com.github.anrimian.simplemusicplayer.ui.utils.fragments.BackButtonListener;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (hasFilePermissions()) {
            goToMainScreen();
            testFolderObserver();
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

    private TestFileObserver testFileObserver1;
    private TestFileObserver testFileObserver2;

    private FileObserver fileObserver;

    private void testFolderObserver() {
        File root = Environment.getExternalStorageDirectory();
        File testDirectory = new File(root, "/test_directory/");
        testDirectory.mkdirs();
        testFileObserver1 = new TestFileObserver(testDirectory.getPath());
        testFileObserver1.getEventObservable().subscribe();
        testFileObserver2 = new TestFileObserver(root + "/test_directory/");
        testFileObserver2.getEventObservable().subscribe();

        fileObserver = new FileObserver(testDirectory.getPath()) {
            @Override
            public void onEvent(int event, @Nullable String path) {
                System.out.println("event: " + event);
            }
        };

        File file = new File(testDirectory, "test_file");
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        testDirectory.delete();
//        file.delete();
    }
}
