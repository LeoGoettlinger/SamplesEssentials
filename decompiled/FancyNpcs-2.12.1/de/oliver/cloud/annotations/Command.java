package de.oliver.cloud.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.checkerframework.checker.nullness.qual.NonNull;

@Repeatable(Commands.class)
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface Command {
   String ANNOTATION_PATH = "de.oliver.cloud.annotations.Command";

   @NonNull
   String value();

   @NonNull
   Class<?> requiredSender() default Object.class;
}
