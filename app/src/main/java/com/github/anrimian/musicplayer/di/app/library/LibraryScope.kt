package com.github.anrimian.musicplayer.di.app.library;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;

import javax.inject.Scope;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Created on 29.10.2017.
 */

@Scope
@Documented
@Retention(RUNTIME)
@interface LibraryScope {
}
