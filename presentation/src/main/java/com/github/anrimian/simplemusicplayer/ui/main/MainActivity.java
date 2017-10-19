package com.github.anrimian.simplemusicplayer.ui.main;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.github.anrimian.simplemusicplayer.R;
import com.github.anrimian.simplemusicplayer.TestPresenter;
import com.github.anrimian.simplemusicplayer.data.TestRepositoryImpl;
import com.github.anrimian.simplemusicplayer.domain.TestInteractor;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        new TestPresenter(new TestInteractor(new TestRepositoryImpl()));
    }
}
