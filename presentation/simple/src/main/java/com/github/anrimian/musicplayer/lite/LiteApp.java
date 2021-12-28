package com.github.anrimian.musicplayer.lite;

import com.github.anrimian.musicplayer.App;
import com.github.anrimian.musicplayer.lite.di.LiteComponents;

//TODO fix build proguard warning(qa mode)
//TODO di access from lite app module to app classes
//TODO rename modules to app-lite
public class LiteApp extends App {

    @Override
    protected void initComponents() {
        LiteComponents.init(getApplicationContext());
    }
}
