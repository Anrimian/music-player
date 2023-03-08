package com.github.anrimian.musicplayer.ui.utils.moxy;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import moxy.viewstate.strategy.StateStrategyType;

/**
 * Same as OneExecution, but with replace previous calls
 */
@Target(value = {ElementType.TYPE, ElementType.METHOD})
@Retention(value = RetentionPolicy.RUNTIME)
@StateStrategyType(SingleOneExecutionStrategy.class)
public @interface SingleOneExecution {
}
