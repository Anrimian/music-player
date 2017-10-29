package com.github.anrimian.simplemusicplayer.di.app;


import com.github.anrimian.simplemusicplayer.di.library.LibraryComponent;
import com.github.anrimian.simplemusicplayer.di.library.LibraryModule;

import javax.inject.Singleton;

import dagger.Component;

/**
 * Created on 11.02.2017.
 */

@Singleton
@Component(modules = { AppModule.class, SchedulerModule.class, ErrorModule.class })
public interface AppComponent {

    LibraryComponent libraryComponent(LibraryModule libraryModule);

}