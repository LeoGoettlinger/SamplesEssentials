package net.luckperms.api.model;

import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jetbrains.annotations.Unmodifiable;

public interface PlayerSaveResult {
   @NonNull
   @Unmodifiable
   Set<PlayerSaveResult.Outcome> getOutcomes();

   default boolean includes(@NonNull PlayerSaveResult.Outcome outcome) {
      Objects.requireNonNull(outcome, "outcome");
      return this.getOutcomes().contains(outcome);
   }

   @Nullable
   String getPreviousUsername();

   @Nullable
   @Unmodifiable
   Set<UUID> getOtherUniqueIds();

   public static enum Outcome {
      CLEAN_INSERT,
      NO_CHANGE,
      USERNAME_UPDATED,
      OTHER_UNIQUE_IDS_PRESENT_FOR_USERNAME;
   }
}
