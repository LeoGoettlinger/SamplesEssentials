package de.oliver.cloud.annotations.exception;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
@API(
   status = Status.STABLE
)
public @interface ExceptionHandler {
   @NonNull
   Class<? extends Throwable> value();
}
