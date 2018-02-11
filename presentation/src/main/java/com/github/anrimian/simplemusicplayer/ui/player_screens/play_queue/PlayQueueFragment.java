package com.github.anrimian.simplemusicplayer.ui.player_screens.play_queue;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.anrimian.simplemusicplayer.R;

/**
 * Created on 22.10.2017.
 */

public class PlayQueueFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_play_queue, container, false);
    }
}
