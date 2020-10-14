package de.evilcodez.config.serialization.object;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface SkipFieldSerialization {

    boolean skipSerialization() default true;
    boolean skipDeserialization() default true;
}
