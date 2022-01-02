package com.github.anrimian.musicplayer.lite;

import com.github.anrimian.musicplayer.App;
import com.github.anrimian.musicplayer.lite.di.LiteComponents;

public class LiteApp extends App {

    @Override
    protected void initComponents() {
        LiteComponents.init(getApplicationContext());
    }

}
