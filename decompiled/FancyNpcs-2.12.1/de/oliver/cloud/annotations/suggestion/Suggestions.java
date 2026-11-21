package de.oliver.cloud.annotations.suggestion;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.checkerframework.checker.nullness.qual.NonNull;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Suggestions {
   @NonNull
   String value();
}
