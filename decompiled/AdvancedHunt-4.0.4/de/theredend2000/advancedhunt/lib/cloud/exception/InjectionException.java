package de.theredend2000.advancedhunt.lib.cloud.exception;

import org.checkerframework.checker.nullness.qual.NonNull;

public class InjectionException extends RuntimeException {
   public InjectionException(@NonNull final String message, @NonNull final Throwable cause) {
      super(message, cause);
   }
}
