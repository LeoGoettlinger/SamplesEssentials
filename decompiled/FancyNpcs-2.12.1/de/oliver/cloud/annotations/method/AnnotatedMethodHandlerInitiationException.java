package de.oliver.cloud.annotations.method;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.Nullable;

@API(
   status = Status.INTERNAL
)
public final class AnnotatedMethodHandlerInitiationException extends RuntimeException {
   AnnotatedMethodHandlerInitiationException(@Nullable final Throwable cause) {
      super(cause);
   }
}
