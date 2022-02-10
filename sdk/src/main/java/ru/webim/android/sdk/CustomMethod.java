package ru.webim.android.sdk;

import static java.lang.annotation.ElementType.METHOD;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The method marked with this annotation is custom, which means that you need to contact support
 * to find out the terms of its use.
 */
@Retention(RetentionPolicy.SOURCE)
@Target(METHOD)
@Documented
public @interface CustomMethod {
}
