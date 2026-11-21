package org.jdbi.v3.sqlobject.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.ANNOTATION_TYPE})
@Deprecated(
   since = "3.38.0",
   forRemoval = true
)
public @interface ConfiguringAnnotation {
   Class<? extends Configurer> value();
}
