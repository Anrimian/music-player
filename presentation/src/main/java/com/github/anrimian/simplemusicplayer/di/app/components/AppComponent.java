package com.github.anrimian.simplemusicplayer.di.app.components;



import com.github.anrimian.simplemusicplayer.di.app.modules.AppModule;

import javax.inject.Singleton;

import dagger.Component;

/**
 * Created on 11.02.2017.
 */

@Singleton
@Component(modules = { AppModule.class })
public interface AppComponent {


}