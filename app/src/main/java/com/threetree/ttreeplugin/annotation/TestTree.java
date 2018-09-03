package com.threetree.ttreeplugin.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by Administrator on 2018/9/3.
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.METHOD)
public @interface TestTree {
}
