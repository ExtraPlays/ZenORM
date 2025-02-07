package com.github.extraplays.ZenORM.database.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Varchar {
    int size() default 255;
}
