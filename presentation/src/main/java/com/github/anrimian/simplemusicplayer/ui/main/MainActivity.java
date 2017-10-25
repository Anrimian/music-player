package com.github.anrimian.simplemusicplayer.ui.main;

import android.Manifest;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;

import com.github.anrimian.simplemusicplayer.R;
import com.github.anrimian.simplemusicplayer.data.TestRepositoryImpl;
import com.github.anrimian.simplemusicplayer.data.repositories.music.MusicProviderRepositoryImpl;
import com.github.anrimian.simplemusicplayer.domain.business.music.MusicProviderInteractor;
import com.github.anrimian.simplemusicplayer.domain.business.music.MusicProviderInteractorImpl;
import com.github.anrimian.simplemusicplayer.domain.utils.PrintIndentedVisitor;
import com.github.anrimian.simplemusicplayer.ui.drawer.DrawerFragment;
import com.github.anrimian.simplemusicplayer.ui.start.StartFragment;
import com.tbruyelle.rxpermissions2.RxPermissions;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (hasFilePermissions()) {
            goToMainScreen();
        } else {
            goToStartScreen();
        }
        new MusicProviderInteractorImpl(new MusicProviderRepositoryImpl(this))
                .getAllMusicInPath(null)
                .subscribe(musicFileTree -> {
                    musicFileTree.accept(new PrintIndentedVisitor(0));
//                    compositions.forEach(System.out::println)
                }, Throwable::printStackTrace);
    }

    private boolean hasFilePermissions() {
        RxPermissions rxPermissions = new RxPermissions(this);
        return rxPermissions.isGranted(Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    private void goToStartScreen() {
        startFragment(new StartFragment());
    }

    private void goToMainScreen() {
        startFragment(new DrawerFragment());
    }

    private void startFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment existFragment = fragmentManager.findFragmentById(R.id.main_activity_container);
        if (existFragment == null || existFragment.getClass() != fragment.getClass()) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.main_activity_container, fragment)
                    .commit();
        }
    }
}
