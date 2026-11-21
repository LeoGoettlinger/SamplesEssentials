package de.oliver.cloud.annotations.string;

import org.checkerframework.checker.nullness.qual.NonNull;

@FunctionalInterface
public interface StringProcessor {
   @NonNull
   static StringProcessor noOp() {
      return new StringProcessor.NoOpStringProcessor();
   }

   @NonNull
   String processString(@NonNull String input);

   public static final class NoOpStringProcessor implements StringProcessor {
      @NonNull
      @Override
      public String processString(@NonNull final String input) {
         return input;
      }
   }
}
