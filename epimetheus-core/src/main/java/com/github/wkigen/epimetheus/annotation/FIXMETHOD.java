package com.github.wkigen.epimetheus.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Created by Dell on 2018/4/9.
 */

@Documented
@Target(METHOD)
@Retention(RUNTIME)
public @interface FIXMETHOD {
}
