package com.github.anrimian.musicplayer.ui.main;

import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class SimplePlayerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Uri uriToPlay = getIntent().getData();

        Toast.makeText(this, "uri to play: " + uriToPlay, Toast.LENGTH_LONG).show();

        finish();
    }
}
