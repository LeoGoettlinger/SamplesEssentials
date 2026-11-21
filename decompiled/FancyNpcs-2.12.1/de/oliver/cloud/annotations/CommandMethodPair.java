package de.oliver.cloud.annotations;

import java.lang.reflect.Method;
import org.checkerframework.checker.nullness.qual.NonNull;

final class CommandMethodPair {
   private final Method method;
   private final Command annotation;

   CommandMethodPair(@NonNull final Method method, @NonNull final Command annotation) {
      this.method = method;
      this.annotation = annotation;
   }

   @NonNull
   Method method() {
      return this.method;
   }

   @NonNull
   Command annotation() {
      return this.annotation;
   }
}
