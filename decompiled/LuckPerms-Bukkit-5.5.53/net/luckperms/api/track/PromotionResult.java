package net.luckperms.api.track;

import java.util.Optional;
import net.luckperms.api.util.Result;
import org.checkerframework.checker.nullness.qual.NonNull;

public interface PromotionResult extends Result {
   @NonNull
   PromotionResult.Status getStatus();

   @Override
   default boolean wasSuccessful() {
      return this.getStatus().wasSuccessful();
   }

   @NonNull
   Optional<String> getGroupFrom();

   @NonNull
   Optional<String> getGroupTo();

   public static enum Status implements Result {
      SUCCESS(true),
      ADDED_TO_FIRST_GROUP(true),
      MALFORMED_TRACK(false),
      END_OF_TRACK(false),
      AMBIGUOUS_CALL(false),
      UNDEFINED_FAILURE(false);

      private final boolean success;

      private Status(boolean success) {
         this.success = success;
      }

      @Override
      public boolean wasSuccessful() {
         return this.success;
      }
   }
}
