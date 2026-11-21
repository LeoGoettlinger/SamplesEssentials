package de.oliver.cloud.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;

@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface Flag {
   @NonNull
   String value();

   String[] aliases() default {""};

   @NonNull
   String parserName() default "";

   @NonNull
   String suggestions() default "";

   @NonNull
   String description() default "";

   @NonNull
   String permission() default "";

   @API(
      status = Status.STABLE
   )
   boolean repeatable() default false;
}
