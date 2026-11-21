package de.oliver.cloud.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.checkerframework.checker.nullness.qual.NonNull;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER})
public @interface Argument {
   @NonNull
   String value() default "__INFERRED_ARGUMENT_NAME__";

   @NonNull
   String parserName() default "";

   @NonNull
   String suggestions() default "";

   @NonNull
   String description() default "";
}
